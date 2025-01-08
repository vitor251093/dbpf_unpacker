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
import sporemodder.util.Vector3;

public class PropertyVector3 extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0031;
	public static final String KEYWORD = "vector3";
	public static final int ARRAY_SIZE = 12;

	private Vector3[] values;
	
	public PropertyVector3() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyVector3(Vector3 value) {
		super(TYPE_CODE, 0);
		this.values = new Vector3[] {value};
	}
	
	public PropertyVector3(Vector3 ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyVector3(List<Vector3> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = values.toArray(new Vector3[values.size()]);
	}
	
	public Vector3[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new Vector3[itemCount];
		
		for (int i = 0; i < itemCount; i++) {
			Vector3 value = new Vector3();
			value.readLE(stream);
			values[i] = value;
			
			if (!isArray) stream.skip(4);
		}
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (Vector3 value : values) {
			value.writeLE(stream);
			
			if (!isArray) stream.writePadding(4);
		}
	}

}
