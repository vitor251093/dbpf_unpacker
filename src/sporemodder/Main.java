package sporemodder;

import sporemodder.file.Converter;
import sporemodder.file.dbpf.DBPFConverter;
import sporemodder.file.dbpf.DBPFUnpacker;

import java.io.File;
import java.util.List;
import java.util.logging.*;

public class Main {

    public static String version = "1.0.0";

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {

        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        rootLogger.addHandler(handler);

        boolean debug = false;

        // Verifica o argumento de depuração
        if (args.length > 0 && (args[0].equals("-d") || args[0].equals("--debug"))) {
            debug = true;
            System.out.println("Debug mode enabled");

            // Configuração do logger para modo debug
            configureLogger(Level.FINE);
        } else {
            // Configuração do logger para modo normal
            configureLogger(Level.INFO);
        }

        // Ajusta os índices dependendo se o modo debug está ativo
        int fileArgIndex = debug ? 1 : 0;

        if (args.length != (fileArgIndex + 2)) {
            System.err.println("dbpf_unpacker v" + version);
            if (args.length == 0 || (debug && args.length == 1)) {
                System.err.println("  error: no input file provided");
            } else if (args.length == (fileArgIndex + 1)) {
                System.err.println("  error: not enough arguments");
            } else {
                System.err.println("  error: too many arguments");
            }
            System.err.println("  usage: java -jar dbpf_unpacker.jar [-d|--debug] <file> <destination>");
            System.exit(1);
        }

        File inputFile = new File(args[fileArgIndex]);
        File outputFile = new File(args[fileArgIndex + 1]);

        if (!inputFile.exists()) {
            System.err.println("dbpf_unpacker v" + version);
            System.err.println("  error: input file does not exist: " + inputFile.getAbsolutePath());
            System.exit(1);
        }

        if (!outputFile.exists()) {
            if (!outputFile.mkdirs()) {
                System.err.println("dbpf_unpacker v" + version);
                System.err.println("  error: could not create output folder");
                System.exit(1);
            }
        }

        if (debug) {
            System.out.println("Input file: " + inputFile.getAbsolutePath());
            System.out.println("Output directory: " + outputFile.getAbsolutePath());
        }

        logger.info("Starting unpacking process...");
        logger.fine("Fine level log message for testing");
        logger.info("Input file: " + inputFile.getAbsolutePath());
        logger.info("Output directory: " + outputFile.getAbsolutePath());

        List<Converter> converters = List.of(new DBPFConverter());
        try {
            logger.info("Creating DBPFUnpacker...");
            var unpacker = new DBPFUnpacker(inputFile, outputFile, converters);

            logger.info("Starting unpacking process...");
            unpacker.call();
            logger.info("Unpacking completed successfully.");
        } catch (Exception e) {
            logger.severe("An error occurred during unpacking: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        logger.info("Unpacking process finished.");
    }

    private static void configureLogger(Level level) {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(level);

        // Remove todos os handlers existentes
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // Adiciona um novo ConsoleHandler
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(level);

        // Define um formatador simples
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("[%s] %s%n", record.getLevel(), record.getMessage());
            }
        });

        rootLogger.addHandler(handler);

        logger.info("Logger configured with level: " + level);
    }
}
