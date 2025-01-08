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
package sporemodder.file;

import java.io.File;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;


public interface Converter {

	/**
	 * Converts the given data from the original format into a user-friendly format. If there is an error, it will be thrown in an exception.
	 * The program must return whether the data was converted or not. If false is returned, the program will try other converters.
	 * <p>
	 * This method must create the output file in the specified folder. The ResourceKey of the original file is given, so the method must use it to give a
	 * different and appropriate name to the output file.
	 * @param stream The data stream to be converted.
	 * @param outputFolder The output folder, where the converted file will be written.
	 * @param key The resource key of the original file.
	 * @return Whether the data was converted (true) or a different converter should be used (false).
	 * @throws Exception
	 */
	public boolean decode(StreamReader stream, File outputFolder, ResourceKey key) throws Exception;

	/**
	 * Whether this converter is a valid decoder (converting FROM the format) for the given package resource.
	 * @param key
	 * @return True if the {@link #decode(StreamReader, File, ResourceKey)} method can be called for this resource, false otherwise.
	 */
	public boolean isDecoder(ResourceKey key);

	/**
	 * Returns a name for this decoder, such as "Properties File (.prop)"
	 * @return
	 */
	public String getName();

	public static File getOutputFile(ResourceKey key, File folder, String extraExtension) {
		HashManager hasher = HashManager.get();
		
		return new File(folder, hasher.getFileName(key.getInstanceID()) + "." + hasher.getTypeName(key.getTypeID()) + "." + extraExtension);
	}

	default void reset() {
		
	}
}
