package sporemodder.file.dbpf;

import java.io.File;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.Converter;
import sporemodder.file.ResourceKey;

public class DBPFConverter implements Converter {
	
	public static final int TYPE_ID = 0x06EFC6AA;
	
	private DBPFUnpackingTask createUnpackTask(StreamReader stream, File outputFile) throws Exception {
		if (outputFile.exists() && outputFile.isDirectory()) {
			for (File file : outputFile.listFiles()) {
				file.delete();
			}
		}
		outputFile.mkdir();
		
		stream.setBaseOffset(stream.getFilePointer());
		DBPFUnpackingTask task = new DBPFUnpackingTask(stream, outputFile);

		return task;
	}
	
	@Override
	public boolean decode(StreamReader stream, File outputFolder, ResourceKey key) throws Exception {
		long oldBase = stream.getBaseOffset();
		
		DBPFUnpackingTask task = createUnpackTask(stream, Converter.getOutputFile(key, outputFolder, "unpacked"));
		task.call();
		
		stream.setBaseOffset(oldBase);
		
		// Always return true, even if there were errors?
		return true;
		
	}

	@Override
	public boolean isDecoder(ResourceKey key) {
		return key.getTypeID() == TYPE_ID;
	}

	@Override
	public String getName() {
		return "Localization Package (." + HashManager.get().getTypeName(TYPE_ID) + ")";
	}

}
