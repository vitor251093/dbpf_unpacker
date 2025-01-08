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

import sporemodder.file.filestructures.Stream.StringEncoding;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.DocumentException;
import sporemodder.file.LocalizedText;

public class PropertyText extends BaseProperty {
	
	public static final int TYPE_CODE = 0x0022;
	public static final String KEYWORD = "text";
	public static final int ARRAY_SIZE = 520;

	private LocalizedText[] values;
	
	public PropertyText() {
		super(TYPE_CODE, 0);
	}
	
	public PropertyText(LocalizedText value) {
		// Text properties are only supported as arrays
		this(new LocalizedText[] {value});
	}
	
	public PropertyText(LocalizedText ... values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.length);
		this.values = values;
	}
	
	public PropertyText(List<LocalizedText> values) {
		super(TYPE_CODE, 0, ARRAY_SIZE, values.size());
		this.values = values.toArray(new LocalizedText[values.size()]);
	}
	
	public LocalizedText[] getValues() {
		return values;
	}
	
	@Override
	public void read(StreamReader stream, int itemCount) throws IOException {
		values = new LocalizedText[itemCount];
		for (int i = 0; i < itemCount; i++) {
			LocalizedText value = new LocalizedText();
			
			value.setTableID(stream.readLEInt());
			value.setInstanceID(stream.readLEInt());
			
			long position = stream.getFilePointer();
			value.setText(stream.readCString(StringEncoding.UTF16LE));
			stream.seek(position + 512);
			
			values[i] = value;
		}
	}
	
	@Override
	public void write(StreamWriter stream) throws IOException {
		for (LocalizedText value : values) {
			stream.writeLEInt(value.getTableID());
			stream.writeLEInt(value.getInstanceID());
			long position = stream.getFilePointer();
			stream.writeCString(value.getText(), StringEncoding.UTF16LE);
			stream.writePadding((int) (512 - (stream.getFilePointer() - position)));
		}
	}
	
	public static void fastConvertXML(StreamWriter stream, Attributes attributes, String text, List<String> autoLocaleStrings, String autoLocaleName) throws IOException {
		int instanceID = 0;
		int tableID = 0;
		
		HashManager hasher = HashManager.get();
		
		String value = attributes.getValue("tableid");
		if (value == null) attributes.getValue("tableID");
		if (value != null && value.length() > 0) tableID = hasher.getFileHash(value);
		
		value = attributes.getValue("instanceid");
		if (value == null) attributes.getValue("instanceID");
		if (value != null && value.length() > 0) instanceID = hasher.getFileHash(value);
		
		// add autolocales if necessary
		if (autoLocaleStrings != null && instanceID == 0 && tableID == 0) {
			
			tableID = hasher.fnvHash(autoLocaleName);
			instanceID = 1 + autoLocaleStrings.size();
			
			autoLocaleStrings.add(text);
		}
		
		stream.writeLEInt(tableID);
		stream.writeLEInt(instanceID);
		byte[] arr = text.getBytes("UTF-16LE");
		stream.write(arr);
		if (arr.length < 512) {
			stream.writePadding(512 - arr.length);
		}
		
	}

}
