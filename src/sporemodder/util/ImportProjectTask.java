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
package sporemodder.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedHashMap;

import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.MemoryStream;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.prop.PropertyList;
import sporemodder.file.prop.XmlPropParser;

public class ImportProjectTask extends ResumableTask<Void> {
	private Project destination;
	private File sourceFolder;
	
	private double progress = 0.0;
	private double fileInc = 0.0;
	private boolean mustIncFiles = false;
	
	private final HashMap<Path, Exception> failedFiles = new LinkedHashMap<Path, Exception>();
	private long ellapsedTime;
	
	private NameRegistry fileRegistry;
	private NameRegistry propRegistry;
	private NameRegistry typeRegistry;
	
	public ImportProjectTask(Project destination, File sourceFolder, NameRegistry fileRegistry, NameRegistry propRegistry, NameRegistry typeRegistry) {
		super();
		this.destination = destination;
		this.sourceFolder = sourceFolder;
		this.fileRegistry = fileRegistry;
		this.propRegistry = propRegistry;
		this.typeRegistry = typeRegistry;
	}

	@Override
	protected Void call() throws Exception {
		
		ellapsedTime = System.currentTimeMillis();
		
		final int foldersCount = sourceFolder.list().length;
		final double folderInc = 1.0 / foldersCount;
		
		final Path source = sourceFolder.toPath();
		final Path target = destination.getFolder().toPath();
		
		Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
			@Override
	        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				try {
					ensureRunning();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
	            // before visiting entries in a directory we copy the directory
	            // (okay if directory already exists).
	            Path newdir = target.resolve(source.relativize(dir));
	            newdir.toFile().mkdir();
	            
	            if (dir.getNameCount() == source.getNameCount() + 1) {
	            	mustIncFiles = true;
	            	fileInc = folderInc / dir.toFile().list().length;
	            	
	            	updateMessage("Importing \"" + dir.getFileName() + "\"");
	            }
	            
	            return FileVisitResult.CONTINUE;
	        }
			
			@Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

				Path dest = target.resolve(source.relativize(file));
				
				if (file.toString().endsWith(".prop.xml")) {
					// Read the file with its original registries
					HashManager.get().replaceRegistries(fileRegistry, propRegistry, typeRegistry);
					
					String name = dest.getFileName().toString();
					name = name.substring(0, name.length() - ".prop.xml".length()) + ".prop.prop_t";
					dest = dest.getParent().resolve(name);
					
					File f = file.toFile();
					try (InputStream in = new FileInputStream(f);
							StreamWriter out = new FileStream(dest.toFile(), "rw")) {
						
						MemoryStream stream = XmlPropParser.xmlToProp(in);
						stream.seek(0);
						
						// Write the file with the program registries
						HashManager.get().replaceRegistries(null, null, null);
						
						PropertyList list = new PropertyList();
						list.read(stream);
						//list.toArgScript().write(dest.toFile());
						
						stream.close();
					} 
					catch (Exception e) {
						failedFiles.put(file, e);
					}
				}
				else {
					Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
				}
				
				if (file.getNameCount() == source.getNameCount() + 2 && mustIncFiles) {
					progress += fileInc;
					updateProgress(progress, 1.0);
				}
				
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
				if (dir.getNameCount() == source.getNameCount() + 1) {
					// Don't add folderInc, as the increase has been done file per file
					// progress += folderInc;
					mustIncFiles = false;
				} else if (mustIncFiles) {
					progress += fileInc;
				}
				updateProgress(progress, 1.0);
				
				return FileVisitResult.CONTINUE;
			}
		});
		
		ellapsedTime = System.currentTimeMillis() - ellapsedTime;
		
		return null;
	}
	
	public void showProgressDialog() {

	}
	
	private void showErrorDialog(Path source) {

	}
}
