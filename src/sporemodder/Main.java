package sporemodder;

import sporemodder.file.Converter;
import sporemodder.file.dbpf.DBPFConverter;
import sporemodder.file.dbpf.DBPFUnpacker;

import java.io.File;
import java.util.List;

public class Main {

    public static String version = "1.0.0";

    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            System.err.println("dbpf_unpacker v" + version);
            if(args.length == 0) {
                System.err.println("  error: no input file provided");
            } else if(args.length == 1) {
                System.err.println("  error: not enough arguments");
            } else {
                System.err.println("  error: too many arguments");
            }
            System.err.println("  usage: java -jar dbpf_unpacker.jar <file> <destination>");
            System.exit(1);
        } else {
            File inputFile = new File(args[0]);
            File outputFile = new File(args[1]);
            if (!outputFile.exists()) {
                if (outputFile.mkdirs()) {
                    System.err.println("dbpf_unpacker v" + version);
                    System.err.println("  error: could not create output folder");
                    return;
                }
            }

            List<Converter> converters = List.of(new DBPFConverter());
            var unpacker = new DBPFUnpacker(inputFile, outputFile, converters);
            unpacker.call();

            System.exit(0);
        }
    }
}
