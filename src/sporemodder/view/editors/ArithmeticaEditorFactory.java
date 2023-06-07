package sporemodder.view.editors;

import javafx.scene.Node;
import sporemodder.util.ProjectItem;

public class ArithmeticaEditorFactory implements EditorFactory {

	@Override
	public ItemEditor createInstance() {
		return new ArithmeticaEditor();
	}

	@Override
	public boolean isSupportedFile(ProjectItem item) {
		return !item.isFolder() && item.getName().endsWith(".arth_t");
	}
	
	@Override
	public Node getIcon(ProjectItem item) {
		return null;
	}
}