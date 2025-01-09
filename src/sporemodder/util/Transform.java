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
package sporemodder.util;

import java.io.IOException;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.file.filestructures.StructureUnsigned;

/**
 * A class that represents a 3D transformation: it can store the same information as a 4x4
 * matrix, but it's easier to use. This object stores the location, scale and rotation 
 * (as a 3x3 matrix) separately, making it easy to modify.
 */
public class Transform {

	private static final int FLAG_SCALE = 1;

	@StructureUnsigned(16) private int flags;  // short
	@StructureUnsigned(16) private int transformCount = 1;  // short
	private final Vector3 offset = new Vector3();
	private float scale = 1.0f;
	private final Matrix rotation = Matrix.getIdentity();

	public float getScale() {
		flags |= FLAG_SCALE;
		transformCount++;
		return scale;
	}
	
	public void setScale(float scale) {
		this.scale = scale;
	}

	public void read(StreamReader in) throws IOException {
		flags = in.readUShort();
		scale = in.readFloat();
		rotation.readLE(in);
		offset.readLE(in);
	}
	
	public void write(StreamWriter out) throws IOException {
		out.writeUShort(flags);
		out.writeFloat(scale);
		rotation.writeLE(out);
		offset.writeLE(out);
	}
	
	public void readComplete(StreamReader in) throws IOException {
		flags = in.readLEUShort();
		transformCount = in.readLEShort();
		offset.readLE(in);
		scale = in.readLEFloat();
		rotation.readLE(in);
	}
	
	public void writeComplete(StreamWriter out) throws IOException {
		out.writeLEUShort(flags);
		out.writeLEShort(transformCount);
		offset.writeLE(out);
		out.writeLEFloat(scale);
		rotation.writeLE(out);
	}

	public void copy(Transform other) {
		scale = other.scale;
		offset.copy(other.offset);
		rotation.copy(other.rotation);
	}
}
