/****************************************************************************
* Copyright (C) 2018 Eric Mor
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

import java.io.*;
import java.util.*;

import sporemodder.PathManager;

public class Project {

	public enum PackageSignature {
		NONE {
			@Override public String toString() {
				return "None";
			}
			@Override public String getFileName() {
				return null;
			}
			@Override public InputStream getInputStream() {
				return null;
			}
		},
		PATCH51 {
			@Override public String toString() {
				return "GA Patch 5.1";
			}
			@Override public String getFileName() {
				return "ExpansionPack1";
			}
			@Override public InputStream getInputStream() {
				return Project.class.getResourceAsStream("/sporemodder/resources/ExpansionPack1.prop");
			}
		},
		BOT_PARTS {
			@Override public String toString() {
				return "Bot Parts";
			}
			@Override public String getFileName() {
				return "BoosterPack2";
			}
			@Override public InputStream getInputStream() {
				return Project.class.getResourceAsStream("/sporemodder/resources/BoosterPack2.prop");
			}
		};
		
		public abstract String getFileName();
		public abstract InputStream getInputStream();
	}

	/** The name of the project, which is taken from the folder name. */
	private String name;
	
	/** The name of the DBPF file generated when packing. */
	private String packageName;

	/** The folder that contains the data of the project. */
	private File folder;
	/** External projects have a file in the Projects folder that links to the real path. */
	private File externalLink;
	
	private final Set<Project> references = new LinkedHashSet<>();

	/** The embedded 'editorPackages~' file that represents the package signature. */
	private PackageSignature packageSignature = PackageSignature.NONE;

	private final Properties settings = new Properties();

	private long lastTimeUsed = -1;
	
	
	public Project(String name) {
		this(name, new File(PathManager.get().getProjectsFolder(), name), null);
	}
	
	public Project(String name, File folder, File externalLink) {
		this.name = name;
		this.folder = folder;
		this.externalLink = externalLink;
		
		onNameChanged(null);
	}
	
	public Set<Project> getReferences() {
		return references;
	}

	/**
	 * Loads the project settings from the configuration file inside the project folder.
	 */
	public void loadSettings() {

	}
	
	public static String getDefaultPackageName(String name) {
		return name.replaceAll("\\s", "_") + ".package";
	}

	/**
	 * Returns the object that is used to load/store the project settings.
	 * @return
	 */
	public Properties getSettings() {
		return settings;
	}

	/**
	 * Returns the last time this project was active in the program, in milliseconds.
	 * @return
	 */
	public long getLastTimeUsed() {
		return lastTimeUsed;
	}

	public void setLastTimeUsed(long time) {
		lastTimeUsed = time;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	// This method assumes that the name is valid and there aren't any other projects with the name
	private void onNameChanged(String oldName) {
		if (oldName != null && !oldName.equalsIgnoreCase(name)) {
			if (externalLink != null) {
				File newFile = new File(externalLink.getParentFile(), name);
				if (newFile.exists()) {
					throw new IllegalArgumentException("File " + newFile.getAbsolutePath() + " already exists");
				}
				if (!externalLink.renameTo(newFile)) {
					throw new IllegalArgumentException("Could not rename project external link file");
				}
				externalLink = newFile;
			}
			else {
				File newFolder = new File(PathManager.get().getProjectsFolder(), name);
				if (newFolder.exists()) {
					throw new IllegalArgumentException("Folder " + newFolder.getAbsolutePath() + " already exists");
				}
				if (!folder.renameTo(newFolder)) {
					throw new IllegalArgumentException("Could not rename project folder");
				}
				folder.renameTo(newFolder);
				folder = newFolder;
			}
		}
		
		// Keep the package name if it was not generated automatically
		if (oldName == null || packageName == getDefaultPackageName(oldName)) {
			packageName = getDefaultPackageName(name);
		}
	}
	
	/** Gets the name of the project, which is taken from the folder name. */
	public String getName() {
		return name;
	}
	
	/** Changes the name of this project. This also renames the project folder or external link file. */
	public void setName(String name) {
		String oldName = this.name;
		this.name = name;
		onNameChanged(oldName);
	}

	public File getFolder() {
		return folder;
	}
	
	/**
	 * Returns the output file (which might not exist) where the project will be packed as a DBPF file.
	 * @return
	 */
	public File getOutputPackage() {
		return null;
	} 
	
	public String getPackageName() {
		return packageName;
	}

	public PackageSignature getPackageSignature() {
		return packageSignature;
	}

	public void setPackageSignature(PackageSignature packageSignature) {
		this.packageSignature = packageSignature;
	}

}
