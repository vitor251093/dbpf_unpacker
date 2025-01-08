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

public class PropertyUInt16 extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0008;
	public static final String KEYWORD = "uint16";
	public static final int ARRAY_SIZE = 2;
	
	public static final long MIN_VALUE = 0;
	public static final long MAX_VALUE = (long) Math.pow(2, 16);

	private int[] values;
	
	public PropertyUInt16() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyUInt16(int value) {
		super(TYPE_CODE, 0);
		this.values = new int[] {value};
	}
	
	public PropertyUInt16(int ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyUInt16(List<Integer> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = new int[values.size()];
		for (int i = 0; i < this.values.length; i++) {
			this.values[i] = values.get(i);
		}
	}
	
	public int[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new int[itemCount];
		stream.readUShorts(values);
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (int value : values) {
			stream.writeUShort(value);
		}
	}
	
	public static void fastConvertXML(StreamWriter stream, Attributes attributes, String text) throws IOException {
		stream.writeUShort(HashManager.get().uint16(text));
	}

}
