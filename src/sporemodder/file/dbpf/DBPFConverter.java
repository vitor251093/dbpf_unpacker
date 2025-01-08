package sporemodder.file.dbpf;

import java.io.File;

import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;
import sporemodder.HashManager;
import sporemodder.file.Converter;
import sporemodder.file.ResourceKey;

public class DBPFConverter implements Converter {
	
	private static String extension = null;
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
	public boolean encode(File input, StreamWriter output) throws Exception {
		DBPFPackingTask task = new DBPFPackingTask(input, output);
		task.call();
		
		return true;
	}

	@Override
	public boolean encode(File input, DBPFPacker packer, int groupID) throws Exception {
		if (isEncoder(input)) {
			String[] splits = input.getName().split("\\.", 2);
			
			ResourceKey name = packer.getTemporaryName();
			name.setInstanceID(HashManager.get().getFileHash(splits[0]));
			name.setGroupID(groupID);
			name.setTypeID(TYPE_ID);  // audioProp or prop
			
			packer.writeFile(name, stream -> {
				DBPFPackingTask task = new DBPFPackingTask(input, stream);
				task.call();
				
				// The task will have disabled this, enable it again
				HashManager.get().setUpdateProjectRegistry(true);
			});
			
			return true;
		}
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
		checkExtensions();
		return file.isDirectory() && file.getName().endsWith("." + extension + ".unpacked");
	}

	@Override
	public String getName() {
		return "Localization Package (." + HashManager.get().getTypeName(TYPE_ID) + ")";
	}

	@Override
	public boolean isEnabledByDefault() {
		return false;
	}

	@Override
	public int getOriginalTypeID(String extension) {
		return TYPE_ID;
	}

}
