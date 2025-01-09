/****************************************************************************
* Copyright (C) 2019 Eric Mor
*
* This file is part of SporeModder FX.
*
* SporeModder FX is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
****************************************************************************/
package sporemodder.file.dbpf;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;

import sporemodder.HashManager;
import sporemodder.file.ResourceKey;
import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.MemoryStream;
import sporemodder.file.filestructures.StreamReader;

public class DBPFUnpackingTask {
	
	@FunctionalInterface
	public static interface DBPFItemFilter {
		public boolean filter(DBPFItem item);
	}
	
	/** The estimated progress (in [0, 1]) that reading the index takes. */ 
	private static final double INDEX_PROGRESS = 0.05;
	/** The estimated progress (in [0, 1]) that clearing the folder takes. */ 
	private static final double CLEAR_FOLDER_PROGRESS = 0.10;
	
	// Cannot use getProgress() as it throws thread exception
	private double progress = 0;
	
	/** The list of input DBPF files, in order of priority. */
	private final List<File> inputFiles = new ArrayList<File>();
	
	/** Alternative input, an StreamReader. */
	private final StreamReader inputStream;
	
	/** The list of input DBPF files that could not be unpacked (because they didn't exist). */
	private final List<File> failedDBPFs = new ArrayList<File>();
	
	/** The folder where all the contents will be written. */
	private File outputFolder;
	
	/** We will keep all files that couldn't be converted here, so that we can keep unpacking the DBPF. */
	private final Map<DBPFItem, Exception> exceptions = new HashMap<>();
	
	/** How much time the operation took, in milliseconds. */
	private long ellapsedTime;
	
	/** An optional filter that defines which items should be unpacked (true) and which shouldn't (false). */
	private DBPFItemFilter itemFilter;
	
	private boolean setPackageSignature;
	
	//TODO it's faster, but apparently it causes problems; I can't reproduce the bug
	private boolean isParallel = true;
	
	private boolean noJavaFX = false;
	private Consumer<Double> noJavaFXProgressListener;

	public DBPFUnpackingTask(StreamReader inputStream, File outputFolder) {
		this.inputStream = inputStream;
		this.outputFolder = outputFolder;
	}

	/**
	 * Returns the project that is being unpacked. This might be null if a file is being unpacked directly.
	 * @return
	 */
	public Object getProject() {
		return null;
	}


	private static void findNamesFile(List<DBPFItem> items, StreamReader in) throws IOException {
		HashManager hasher = HashManager.get();
		int group = hasher.getFileHash("sporemaster");
		int name = hasher.getFileHash("names");
		
		for (DBPFItem item : items) {
			if (item.name.getGroupID() == group && item.name.getInstanceID() == name) {
				try (ByteArrayInputStream arrayStream = new ByteArrayInputStream(item.processFile(in).getRawData());
						BufferedReader reader = new BufferedReader(new InputStreamReader(arrayStream))) {
					hasher.getProjectRegistry().read(reader);
				}
			}
		}
	}

	private void unpackStream(StreamReader packageStream, Map<Integer, Set<ResourceKey>> writtenFiles, double progressFraction) throws IOException, InterruptedException {
		HashManager hasher = HashManager.get();
			
		DatabasePackedFile header = new DatabasePackedFile();
		header.readHeader(packageStream);
		header.readIndex(packageStream);
		
		DBPFIndex index = header.index;
		index.readItems(packageStream, header.indexCount, header.isDBBF);
		
		incProgress(INDEX_PROGRESS * progressFraction);
		// How much each file adds to the progress
		double inc = (1.0 - INDEX_PROGRESS) * progressFraction / header.indexCount;
		
		//First search sporemaster/names.txt, and use it if it exists
		hasher.getProjectRegistry().clear();
		findNamesFile(index.items, packageStream);
		
		// Sometimes, reading the file data goes faster than the tasks, so we end up
		// loading almost all the package into memory and that causes an OutOfMemory exception
		// We must limit how many tasks we want running simultaneously.
		int maxTasks = ForkJoinPool.getCommonPoolParallelism();
		
		int itemIndex = -1;
		CountDownLatch latch = new CountDownLatch(index.items.size());
		for (DBPFItem item : index.items) {
			++itemIndex;
			
			if (itemFilter != null && !itemFilter.filter(item)) {
				latch.countDown();
				incProgress(inc);
				continue;
			}
			
			int groupID = item.name.getGroupID();
			int instanceID = item.name.getInstanceID();
			
			// Skip files if they have already been written by higher priority packages
			if (writtenFiles != null) {
				Set<ResourceKey> groupSet = writtenFiles.get(groupID);
				if (groupSet != null) {
					if (groupSet.contains(item.name)) {
						latch.countDown();
						incProgress(inc);
						continue;
					}
				}
			}
			
			String fileName = hasher.getFileName(instanceID);
			
			// skip autolocale files
			if (groupID == 0x02FABF01 && fileName.startsWith("auto_")) {
				latch.countDown();
				incProgress(inc);
				continue;
			}
			
			
			File folder = new File(outputFolder, hasher.getFileName(groupID));
			folder.mkdir();
			
			FileConvertAction action = new FileConvertAction(item, folder, item.processFile(packageStream), inc, latch);
			if (isParallel) {
				if (itemIndex == index.items.size() - 1 || ForkJoinPool.commonPool().getQueuedSubmissionCount() >= maxTasks) {
					// Execute in same thread if it's the last item or if we have many tasks waiting
					ForkJoinPool.commonPool().invoke(action);
				}
				else {
					ForkJoinPool.commonPool().execute(action);
				}
			} else {
				action.compute();
			}
				
			if (writtenFiles != null) {
				Set<ResourceKey> groupSet = writtenFiles.get(groupID);
				if (groupSet == null) {
					groupSet = new HashSet<>();
					writtenFiles.put(groupID, groupSet);
				}
				groupSet.add(item.name);
			}
		}
		
		// Await for all files to finish writing
		latch.await();
		
		// Remove the extra names; if they need to be used, loading the project will load them as well
		hasher.getProjectRegistry().clear();
	}
	
	public Exception call() throws Exception {
		
		long initialTime = System.currentTimeMillis();
		
		if (inputStream != null) {
			unpackStream(inputStream, null, 1.0);
		}
		else {
			double progressFactor = 1.0;
			
			final HashMap<Integer, Set<ResourceKey>> writtenFiles = new HashMap<>();
			boolean checkFiles = inputFiles.size() > 1;  // only check already existing files if we are unpacking more than one package at once
			
			long[] fileSizes = new long[inputFiles.size()];
			long totalFileSize = 0;
			for (int i = 0; i < fileSizes.length; ++i) {
				if (inputFiles.get(i).exists()) {
					fileSizes[i] = Files.size(inputFiles.get(i).toPath());
					totalFileSize += fileSizes[i];
				}
			}

			int i = 0;
			for (File inputFile : inputFiles) {
				double projectProgress = progressFactor * (double)fileSizes[i] / totalFileSize;
				
				if (!inputFile.exists()) {
					failedDBPFs.add(inputFile);
					continue;
				}
				
				try (StreamReader packageStream = new FileStream(inputFile, "r"))  {
					unpackStream(packageStream, checkFiles ? writtenFiles : null, projectProgress);
				}
				catch (Exception e) {
					return e;
				}
				++i;
			}
		}
		
		ellapsedTime = System.currentTimeMillis() - initialTime;

		return null;
	}

	private void incProgress(double increment) {
		progress += increment;
	}

	private class FileConvertAction extends RecursiveAction {
		final DBPFItem item;
		final File folder;
		final MemoryStream dataStream;
		final double inc;
		final CountDownLatch latch;
		
		FileConvertAction(DBPFItem item, File folder, MemoryStream dataStream, double inc, CountDownLatch latch) {
			this.item = item;
			this.folder = folder;
			this.dataStream = dataStream;
			this.inc = inc;
			this.latch = latch;
		}
		
		@Override public void compute() {
			try {
				HashManager hasher = HashManager.get();
				String name = hasher.getFileName(item.name.getInstanceID()) + "." + hasher.getTypeName(item.name.getTypeID());
				dataStream.writeToFile(new File(folder, name));
			}
			catch (Exception e) {
				exceptions.put(item, e);
			}
			finally {
				dataStream.close();
				incProgress(inc);
				latch.countDown();
			}
		}
	}
}
