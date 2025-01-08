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

import sporemodder.file.filestructures.Stream.StringEncoding;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.DocumentError;

public class PropertyString8 extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0012;
	public static final String KEYWORD = "string8";
	public static final int ARRAY_SIZE = 16;

	private String[] values;
	
	public PropertyString8() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyString8(String value) {
		super(TYPE_CODE, 0);
		this.values = new String[] {value};
	}
	
	public PropertyString8(String ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyString8(List<String> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = values.toArray(new String[values.size()]);
	}
	
	public String[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new String[itemCount];
		for (int i = 0; i < itemCount; i++) {
			values[i] = stream.readString(StringEncoding.ASCII, stream.readInt());
		}
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (String value : values) {
			stream.writeInt(value.length());
			stream.writeString(value, StringEncoding.ASCII);
		}
	}
	
	public static void fastConvertXML(StreamWriter stream, Attributes attributes, String text) throws IOException {
		stream.writeInt(text.length());
		stream.write(text.getBytes("US-ASCII"));
	}

}
