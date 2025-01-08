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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.ResourceKey;

public class PropertyKey extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0020;
	public static final String KEYWORD = "key";
	public static final int ARRAY_SIZE = 12;

	private ResourceKey[] values;
	
	public PropertyKey() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyKey(ResourceKey value) {
		super(TYPE_CODE, 0);
		this.values = new ResourceKey[] {value};
	}
	
	public PropertyKey(ResourceKey ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyKey(List<ResourceKey> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = values.toArray(new ResourceKey[values.size()]);
	}
	
	public ResourceKey[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new ResourceKey[itemCount];
		
		for (int i = 0; i < itemCount; i++) {
			ResourceKey value = new ResourceKey();
			value.readLE(stream);
			values[i] = value;
			
			if (!isArray) stream.skip(4);
		}
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (ResourceKey value : values) {
			value.writeLE(stream);
			
			if (!isArray) stream.writePadding(4);
		}
	}
	
	public static void fastConvertXML(StreamWriter stream, Attributes attributes, String text, boolean bArray) throws IOException {
	
		HashManager hasher = HashManager.get();
		
		int[] values = new int[3];
		
		String str = attributes.getValue("groupid");
		if (str == null) str = attributes.getValue("groupID");
		if (str != null && str.length() > 0) {
			values[0] = hasher.getFileHash(str);
		}
		
		str = attributes.getValue("instanceid");
		if (str == null) str = attributes.getValue("instanceID");
		if (str != null && str.length() > 0) {
			values[1] = hasher.getFileHash(str);
		}
		
		str = attributes.getValue("typeid");
		if (str == null) str = attributes.getValue("typeID");
		if (str != null && str.length() > 0) {
			values[2] = hasher.getTypeHash(str);
		}
		
		stream.writeLEInt(values[1]);
		stream.writeLEInt(values[2]);
		stream.writeLEInt(values[0]);
		if (!bArray) {
			stream.writeLEInt(0);
		}
	}
}
