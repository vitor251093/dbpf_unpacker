package sporemodder;

import sporemodder.file.Converter;
import sporemodder.file.dbpf.DBPFConverter;
import sporemodder.file.dbpf.DBPFUnpacker;

import java.io.File;
import java.util.List;

public class Main {

    public static String version = "1.0.0";

    private static final Logger logger = LoggerManager.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        boolean debug = false;

        // Verifica o argumento de depuração
        if (args.length > 0 && (args[0].equals("-d") || args[0].equals("--debug"))) {
            debug = true;
            System.out.println("Debug mode enabled");
        }

        // Inicializa o logger globalmente
        LoggerManager.initialize(debug);

        // Ajusta os índices dependendo se o modo debug está ativo
        int fileArgIndex = debug ? 1 : 0;

        if (args.length != (fileArgIndex + 2)) {
            System.err.println("DBPF Unpacker"); // + version
            if (args.length == 0 || (debug && args.length == 1)) {
                System.err.println("  error: no input file provided");
            } else if (args.length == (fileArgIndex + 1)) {
                System.err.println("  error: not enough arguments");
            } else {
                System.err.println("  error: too many arguments");
            }
            System.err.println("  usage: dbpf_unpacker [-d|--debug] <file> <destination>");
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
            logger.fine("Input file: " + inputFile.getAbsolutePath());
            logger.fine("Output directory: " + outputFile.getAbsolutePath());
        }

        logger.fine("Starting unpacking process...");
        logger.fine("Fine level log message for testing");
        logger.fine("Input file: " + inputFile.getAbsolutePath());
        logger.fine("Output directory: " + outputFile.getAbsolutePath());

        List<Converter> converters = List.of(new DBPFConverter());
        try {
            logger.fine("Creating DBPFUnpacker...");
            var unpacker = new DBPFUnpacker(inputFile, outputFile, converters);

            logger.fine("Starting unpacking process...");
            unpacker.call();
            logger.fine("Unpacking completed successfully.");
        } catch (Exception e) {
            logger.severe("An error occurred during unpacking: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        logger.fine("Unpacking process finished.");
    }

    private static void configureLogger(Level level) {
        // Configura o nível do logger root e remove handlers existentes
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(level);
        
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // Configura o novo handler com o nível especificado
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(level);
        
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                // Só exibe logs se estiverem no nível correto
                if (record.getLevel().intValue() >= level.intValue()) {
                    return String.format("[%s] %s%n", record.getLevel(), record.getMessage());
                }
                return ""; // Retorna string vazia para outros níveis
            }
        });
        
        rootLogger.addHandler(handler);
        
        // Configura todos os loggers existentes para usar o mesmo nível
        LogManager.getLogManager().getLoggerNames().asIterator().forEachRemaining(name -> {
            Logger logger = LogManager.getLogManager().getLogger(name);
            if (logger != null) {
                logger.setLevel(level);
                // Remove handlers específicos de outros loggers
                for (Handler h : logger.getHandlers()) {
                    logger.removeHandler(h);
                }
            }
        });

        // Só exibe essa mensagem se estivermos em modo debug
        if (level == Level.FINE) {
            logger.fine("Logger configured with level: " + level);
        }
    }
}
