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

package sporemodder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;

/**
 * A small class used to control the path where the program is, and merge relative paths accordingly.
 */
public class PathManager {

	/** The folder where the program files are. */
	private File programFolder;

	/**
	 * Returns the current instance of the PathManager class.
	 */
	public static PathManager get() {
		return null;
	}
	
	public void initialize(Properties properties) {
		String protocol = PathManager.class.getResource("PathManager.class").getProtocol();
		if (Objects.equals(protocol, "jar")) {
		    // run in jar
			// First we try to get the program folder from two different sources
			//programFolder = new File(System.getProperty("user.dir"));
			try {
				programFolder = new File(PathManager.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (programFolder == null || !programFolder.exists())
			{
				try {
					programFolder = new File(ClassLoader.getSystemClassLoader().getResource(".").toURI().getPath());
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (programFolder == null || !programFolder.exists()) {
				programFolder = new File(System.getProperty("user.dir"));
			}
		} else if (Objects.equals(protocol, "file")) {
		    // run in ide
			programFolder = new File(System.getProperty("user.dir"));
		}
	}
	
	/**
	 * Returns the file that corresponds to the given relative path, in the program folder. For example, if the program is at
	 * "C:\SporeModder" and you provide the relative path "WinMerge\WinMerge.exe", the file "C:\SporeModder\WinMerge\WinMerge.exe"
	 * will be returned.
	 * @param relativePath The path of the file, relative to the program folder.
	 */
	public File getProgramFile(String relativePath) {
		return new File(programFolder, relativePath);
	}

}
