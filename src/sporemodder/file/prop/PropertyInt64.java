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

public class PropertyInt64 extends BaseProperty {
	
	public static final int TYPE_CODE = 0x000B;
	public static final String KEYWORD = "int64";
	public static final int ARRAY_SIZE = 8;
	
	public static final long MIN_VALUE = Long.MIN_VALUE;
	public static final long MAX_VALUE = Long.MAX_VALUE;

	private long[] values;
	
	public PropertyInt64() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyInt64(long value) {
		super(TYPE_CODE, 0);
		this.values = new long[] {value};
	}
	
	public PropertyInt64(long ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyInt64(List<Long> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = new long[values.size()];
		for (int i = 0; i < this.values.length; i++) {
			this.values[i] = values.get(i);
		}
	}
	
	public long[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new long[itemCount];
		stream.readLongs(values);
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (long value : values) {
			stream.writeLong(value);
		}
	}
	
	public static void fastConvertXML(StreamWriter stream, Attributes attributes, String text) throws IOException {
		stream.writeLong(HashManager.get().int64(text));
	}

}
