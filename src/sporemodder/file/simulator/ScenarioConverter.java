package sporemodder.file.simulator;

import java.io.File;
import java.io.PrintWriter;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sporemodder.HashManager;
import sporemodder.file.Converter;
import sporemodder.file.ResourceKey;
import sporemodder.file.dbpf.DBPFPacker;
import sporemodder.file.filestructures.FileStream;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;


public class ScenarioConverter implements Converter {
	
	private static final int TYPE_ID = 0x366A930D;
	private static String extension = null;
	
	private boolean decode(StreamReader stream, File outputFolder) throws Exception {
		cScenarioResource scenario = new cScenarioResource();
		scenario.read(stream);
		
		if (outputFolder.exists() && outputFolder.isDirectory()) {
			for (File file : outputFolder.listFiles()) {
				file.delete();
			}
		}
		
		if (!outputFolder.exists()) outputFolder.mkdir();
		
		scenario.propertyList.toArgScript().write(new File(outputFolder, "planet.prop.prop_t"));
		
		try (PrintWriter writer = new PrintWriter(new File(outputFolder, "data.xml"))) {
			writer.write(scenario.printDataXML());
		}
		
		return true;
	}
	
	@Override
	public boolean decode(StreamReader stream, File outputFolder, ResourceKey key) throws Exception {
		return decode(stream, Converter.getOutputFile(key, outputFolder, "unpacked"));
	}

	@Override
	public boolean encode(File input, StreamWriter output) throws Exception {
		return false;
	}

	@Override
	public boolean encode(File input, DBPFPacker packer, int groupID) throws Exception {
		return false;
	}

	@Override
	public boolean isDecoder(ResourceKey key) {
		return key.getTypeID() == TYPE_ID;
	}

	private void checkExtensions() {
		if (extension == null) {
			extension = HashManager.get().getTypeName(TYPE_ID);
		}
	}
	
	@Override
	public boolean isEncoder(File file) {
		return false;
	}

	@Override
	public String getName() {
		return "Scenario File (." + HashManager.get().getTypeName(TYPE_ID) + ")";
	}

	@Override
	public boolean isEnabledByDefault() {
		return false;
	}

	@Override
	public int getOriginalTypeID(String extension) {
		checkExtensions();
		return TYPE_ID;
	}

}