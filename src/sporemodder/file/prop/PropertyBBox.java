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
import java.util.List;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.BoundingBox;

public class PropertyBBox extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0039;
	public static final String KEYWORD = "bbox";
	public static final int ARRAY_SIZE = 24;

	private BoundingBox[] values;
	
	public PropertyBBox() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyBBox(BoundingBox value) {
		// BoundingBox properties are only supported as arrays
		this(new BoundingBox[] {value});
	}
	
	public PropertyBBox(BoundingBox ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyBBox(List<BoundingBox> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = values.toArray(new BoundingBox[values.size()]);
	}
	
	public BoundingBox[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new BoundingBox[itemCount];
		for (int i = 0; i < itemCount; i++) {
			BoundingBox value = new BoundingBox();
			value.read(stream);
			values[i] = value;
		}
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (BoundingBox value : values) {
			value.write(stream);
		}
	}
}
