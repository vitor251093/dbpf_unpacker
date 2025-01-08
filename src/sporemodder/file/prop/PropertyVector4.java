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

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.util.Vector4;

public class PropertyVector4 extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0033;
	public static final String KEYWORD = "vector4";
	public static final int ARRAY_SIZE = 16;

	private Vector4[] values;
	
	public PropertyVector4() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyVector4(Vector4 value) {
		super(TYPE_CODE, 0);
		this.values = new Vector4[] {value};
	}
	
	public PropertyVector4(Vector4 ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyVector4(List<Vector4> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = values.toArray(new Vector4[values.size()]);
	}
	
	public Vector4[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new Vector4[itemCount];
		
		for (int i = 0; i < itemCount; i++) {
			Vector4 value = new Vector4();
			value.readLE(stream);
			values[i] = value;
		}
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (Vector4 value : values) {
			value.writeLE(stream);
		}
	}
}
