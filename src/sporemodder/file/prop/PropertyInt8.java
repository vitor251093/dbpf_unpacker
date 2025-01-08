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

public class PropertyInt8 extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0005;
	public static final String KEYWORD = "int8";
	public static final int ARRAY_SIZE = 1;
	
	public static final long MIN_VALUE = Byte.MIN_VALUE;
	public static final long MAX_VALUE = Byte.MAX_VALUE;
	
	private byte[] values;
	
	public PropertyInt8() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyInt8(byte value) {
		super(TYPE_CODE, 0);
		this.values = new byte[] {value};
	}
	
	public PropertyInt8(byte ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyInt8(List<Byte> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = new byte[values.size()];
		for (int i = 0; i < this.values.length; i++) {
			this.values[i] = values.get(i);
		}
	}
	
	public byte[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new byte[itemCount];
		stream.readBytes(values);
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (byte value : values) {
			stream.writeByte(value);
		}
	}
	
	public static void fastConvertXML(StreamWriter stream, Attributes attributes, String text) throws IOException {
		stream.writeByte(HashManager.get().int8(text));
	}
}
