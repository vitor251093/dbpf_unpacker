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
import sporemodder.file.DocumentError;
import sporemodder.file.DocumentException;

public final class PropertyUInt32 extends BaseProperty {
	
	public static final int TYPE_CODE = 0x000A;
	public static final String KEYWORD = "uint32";
	public static final int ARRAY_SIZE = 4;
	
	public static final long MIN_VALUE = 0;
	public static final long MAX_VALUE = (long) Math.pow(2, 32);

	private long[] values;
	
	public PropertyUInt32() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyUInt32(long value) {
		super(TYPE_CODE, 0);
		this.values = new long[] {value};
	}
	
	public PropertyUInt32(long ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyUInt32(List<Long> values) {
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
		stream.readUInts(values);
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		stream.writeUInts(values);
	}
	
	public static void fastConvertXML(StreamWriter stream, Attributes attributes, String text) throws IOException {
		stream.writeUInt(HashManager.get().uint32(text));
	}

}
