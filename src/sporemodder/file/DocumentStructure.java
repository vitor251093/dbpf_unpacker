/****************************************************************************
* Copyright (C) 2018 Eric Mor
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

package sporemodder.file;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to keep track of the multiple fragments that make up a document. It allows nesting of fragments.
 * <p>
 * Each fragment consists of a start and end position, and a text that describes it.
 */
public class DocumentStructure {

	private String text;
	private final DocumentFragment rootFragment = new DocumentFragment(this);
	private final List<DocumentFragment> fragments = new ArrayList<DocumentFragment>();

	public DocumentStructure(String description, String text) {
		this.text = text;
		
		rootFragment.setDescription(description);
	}
	
	/**
	 * Returns the text this structure represents. The fragments in the structure will use this text.
	 * @return
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Sets the text this structure represents. The fragments in the structure will use this text.
	 * @param text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Adds a fragment into this structure. The fragment will be added at the end of the list, so its positions are
	 * expected to come after the rest of fragments in this structure.
	 * @param fragment
	 */
	public void add(DocumentFragment fragment) {
		fragments.add(fragment);
	}
}
