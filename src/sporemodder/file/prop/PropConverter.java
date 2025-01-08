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
package sporemodder.file.prop;

import java.io.File;
import java.io.IOException;

import sporemodder.HashManager;
import sporemodder.file.Converter;
import sporemodder.file.ResourceKey;
import sporemodder.file.filestructures.StreamReader;


public class PropConverter implements Converter {
	
	private boolean decode(StreamReader stream, File outputFile) throws IOException {
		PropertyList list = new PropertyList();
		list.read(stream);
		return true;
	}

	@Override
	public boolean decode(StreamReader stream, File outputFolder, ResourceKey key) throws IOException {
		return decode(stream, Converter.getOutputFile(key, outputFolder, "prop_t"));
	}

	@Override
	public boolean isDecoder(ResourceKey key) {
		// prop | audioProp | submix | mode | children
		return key.getTypeID() == 0x00B1B104 || 
		key.getTypeID() == 0x02B9F662 || 
		key.getTypeID() == 0x02C9EFF2 || 
		key.getTypeID() == 0x0497925E || 
		key.getTypeID() == 0x03F51892;
	}

	@Override
	public String getName() {
		return "Properties File (." +
		HashManager.get().getTypeName(0x00B1B104) + ", ." +
		HashManager.get().getTypeName(0x02B9F662) + ", ." +
		HashManager.get().getTypeName(0x02C9EFF2) + ", ." +
		HashManager.get().getTypeName(0x0497925E) + ", ." +
		HashManager.get().getTypeName(0x03F51892) + ")";
	}

}
