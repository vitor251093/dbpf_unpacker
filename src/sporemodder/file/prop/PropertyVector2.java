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
import sporemodder.util.Vector2;

public class PropertyVector2 extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0030;
	public static final String KEYWORD = "vector2";
	public static final int ARRAY_SIZE = 8;

	private Vector2[] values;
	
	public PropertyVector2() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyVector2(Vector2 value) {
		super(TYPE_CODE, 0);
		this.values = new Vector2[] {value};
	}
	
	public PropertyVector2(Vector2 ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyVector2(List<Vector2> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = values.toArray(new Vector2[values.size()]);
	}
	
	public Vector2[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new Vector2[itemCount];
		
		for (int i = 0; i < itemCount; i++) {
			Vector2 value = new Vector2();
			value.readLE(stream);
			values[i] = value;
			
			if (!isArray) stream.skip(8);
		}
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (Vector2 value : values) {
			value.writeLE(stream);
			
			if (!isArray) stream.writePadding(8);
		}
	}
}
