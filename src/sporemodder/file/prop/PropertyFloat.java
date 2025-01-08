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
import sporemodder.file.DocumentException;

public class PropertyFloat extends BaseProperty {
	
	public static final int TYPE_CODE = 0x000D;
	public static final String KEYWORD = "float";
	public static final int ARRAY_SIZE = 4;

	private float[] values;
	
	public PropertyFloat() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyFloat(float value) {
		super(TYPE_CODE, 0);
		this.values = new float[] {value};
	}
	
	public PropertyFloat(float ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyFloat(List<Float> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = new float[values.size()];
		for (int i = 0; i < this.values.length; i++) {
			this.values[i] = values.get(i);
		}
	}
	
	public float[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new float[itemCount];
		stream.readFloats(values);
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (float value : values) {
			stream.writeFloats(value);
		}
	}
	
	public static void fastConvertXML(StreamWriter stream, Attributes attributes, String text) throws IOException {
		stream.writeFloat(Float.parseFloat(text));
	}
}
