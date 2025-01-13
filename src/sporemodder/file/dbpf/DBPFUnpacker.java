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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.MemoryStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.HashManager;
import sporemodder.file.Converter;
import sporemodder.file.ResourceKey;

public class DBPFUnpacker {
	private static final Logger logger = Logger.getLogger(DBPFUnpacker.class.getName());

	@FunctionalInterface
	public static interface DBPFItemFilter {
		public boolean filter(DBPFItem item);
	}

	private static final double INDEX_PROGRESS = 0.15;

	private final List<File> inputFiles = new ArrayList<File>();
	private final StreamReader inputStream;
	private final List<File> failedDBPFs = new ArrayList<File>();
	private File outputFolder;
	private final HashMap<DBPFItem, Exception> exceptions = new HashMap<DBPFItem, Exception>();
	private final List<Converter> converters;
	private DBPFItemFilter itemFilter;

	public DBPFUnpacker(File inputFile, File outputFolder, List<Converter> converters) {
		logger.info("Initializing DBPFUnpacker with input file: " + inputFile.getAbsolutePath());
		this.inputFiles.add(inputFile);
		this.outputFolder = outputFolder;
		this.converters = converters;
		this.inputStream = null;
	}

	private static void findNamesFile(List<DBPFItem> items, StreamReader in, HashManager hasher) throws IOException {
		logger.fine("Searching for names file...");
		int group = hasher.getFileHash("sporemaster");
		int name = hasher.getFileHash("names");

		for (DBPFItem item : items) {
			if (item.name.getGroupID() == group && item.name.getInstanceID() == name) {
				logger.info("Names file found. Reading project registry...");
				try (ByteArrayInputStream arrayStream = new ByteArrayInputStream(item.processFile(in).getRawData());
					 BufferedReader reader = new BufferedReader(new InputStreamReader(arrayStream))) {
					hasher.getProjectRegistry().read(reader);
				}
				logger.info("Project registry read successfully.");
				return;
			}
		}
		logger.info("Names file not found.");
	}

	private void loadRegistry(HashManager hasher) {
		logger.info("Attempting to load registry file...");

		// Tenta carregar do diretório de execução
		File externalFile = new File("reg_file.txt");
		if (externalFile.exists()) {
			try {
				logger.info("Loading registry from external file: " + externalFile.getAbsolutePath());
				hasher.getProjectRegistry().read(Files.newBufferedReader(externalFile.toPath()));
				logger.info("Registry loaded successfully from external file.");
				return;
			} catch (IOException e) {
				logger.warning("Failed to load external registry file: " + e.getMessage());
			}
		}

		// Se falhar, tenta carregar do recurso interno
		try (InputStream is = DBPFUnpacker.class.getResourceAsStream("/reg_file.txt")) {
			if (is != null) {
				logger.info("Loading registry from internal resource.");
				hasher.getProjectRegistry().read(new BufferedReader(new InputStreamReader(is)));
				logger.info("Registry loaded successfully from internal resource.");
			} else {
				logger.severe("Registry file not found as internal resource.");
			}
		} catch (IOException e) {
			logger.severe("Failed to load registry file: " + e.getMessage());
		}
	}

	private void unpackStream(StreamReader packageStream, HashMap<Integer, List<ResourceKey>> writtenFiles) throws IOException, InterruptedException {
		logger.info("Starting to unpack stream...");
		HashManager hasher = new HashManager();
		hasher.initialize();

		loadRegistry(hasher);

		logger.info("Reading file index...");

		DatabasePackedFile header = new DatabasePackedFile();
		header.readHeader(packageStream);
		header.readIndex(packageStream);

		DBPFIndex index = header.index;
		index.readItems(packageStream, header.indexCount, header.isDBBF);

		logger.info("File index read. Total items: " + header.indexCount);
		logger.info("Unpacking files...");

		double inc = ((1.0 - INDEX_PROGRESS) / header.indexCount) / inputFiles.size();

		hasher.getProjectRegistry().clear();
		findNamesFile(index.items, packageStream, hasher);

		int processedItems = 0;
		int convertedItems = 0;
		int skippedItems = 0;

		for (DBPFItem item : index.items) {
			processedItems++;

			if (itemFilter != null && !itemFilter.filter(item)) {
				skippedItems++;
				continue;
			}

			int groupID = item.name.getGroupID();
			int instanceID = item.name.getInstanceID();

			if (writtenFiles != null) {
				List<ResourceKey> list = writtenFiles.get(groupID);
				if (list != null && list.stream().anyMatch(key -> key.isEquivalent(item.name))) {
					skippedItems++;
					continue;
				}
			}

			String fileName = hasher.getFileName(instanceID);

			if (groupID == 0x02FABF01 && fileName.startsWith("auto_")) {
				skippedItems++;
				continue;
			}

			File folder = new File(outputFolder, hasher.getFileName(groupID));
			folder.mkdir();

			try (MemoryStream dataStream = item.processFile(packageStream)) {
				boolean isConverted = false;

				if (groupID != 0x40404000 || item.name.getTypeID() != 0x00B1B104) {
					for (Converter converter : converters) {
						if (converter.isDecoder(item.name)) {
							logger.fine("Using converter: " + converter.getClass().getSimpleName() + " for item: " + item.name);
							if (converter.decode(dataStream, folder, item.name)) {
								isConverted = true;
								convertedItems++;
								logger.info("Converted file: " + item.name);
								break;
							}
						}
					}
				}

				if (!isConverted) {
					String name = hasher.getFileName(item.name.getInstanceID()) + "." + hasher.getTypeName(item.name.getTypeID());
					File outputFile = new File(folder, name);
					dataStream.writeToFile(outputFile);
					logger.info("Saved raw file: " + outputFile.getAbsolutePath());
				}

				if (writtenFiles != null) {
					writtenFiles.computeIfAbsent(groupID, k -> new ArrayList<>()).add(item.name);
				}
			}
			catch (Exception e) {
				logger.warning("Error processing item: " + item.name + ". Error: " + e.getMessage());
				exceptions.put(item, e);
			}

			if (processedItems % 100 == 0) {
				logger.info("Progress: " + processedItems + " / " + header.indexCount + " items processed");
			}
		}

		logger.info("Unpacking completed. Total items: " + header.indexCount +
				", Processed: " + processedItems +
				", Converted: " + convertedItems +
				", Skipped: " + skippedItems +
				", Errors: " + exceptions.size());

		hasher.getProjectRegistry().clear();
	}

	public Exception call() throws Exception {
		logger.info("Starting DBPFUnpacker.call()");
		long initialTime = System.currentTimeMillis();

		if (inputStream != null) {
			logger.info("Unpacking from input stream");
			unpackStream(inputStream, null);
		}
		else {
			logger.info("Unpacking from " + inputFiles.size() + " input files");
			final HashMap<Integer, List<ResourceKey>> writtenFiles = new HashMap<Integer, List<ResourceKey>>();
			boolean checkFiles = inputFiles.size() > 1;

			for (File inputFile : inputFiles) {
				if (!inputFile.exists()) {
					logger.warning("Input file does not exist: " + inputFile.getAbsolutePath());
					failedDBPFs.add(inputFile);
					continue;
				}

				logger.info("Processing file: " + inputFile.getAbsolutePath());
				for (Converter converter : converters) converter.reset();

				try (StreamReader packageStream = new FileStream(inputFile, "r"))  {
					unpackStream(packageStream, checkFiles ? writtenFiles : null);
				}
				catch (Exception e) {
					logger.severe("Error unpacking file: " + inputFile.getAbsolutePath() + ". Error: " + e.getMessage());
					return e;
				}
			}
		}

		long endTime = System.currentTimeMillis();
		logger.info("DBPFUnpacker.call() completed in " + (endTime - initialTime) + "ms");
		return null;
	}
}
