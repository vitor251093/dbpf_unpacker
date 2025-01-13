package sporemodder;

import java.util.logging.*;

public class LoggerManager {
    private static final Logger rootLogger = Logger.getLogger("");
    private static Level currentLevel = Level.INFO;

    public static void initialize(boolean debug) {
        // Define o nível baseado no modo debug
        currentLevel = debug ? Level.FINE : Level.WARNING;
        
        // Remove handlers existentes do root logger
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }
        
        // Configura o novo handler
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(currentLevel);
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("[%s] %s%n", 
                    record.getLevel(), 
                    record.getMessage());
            }
        });
        
        rootLogger.setLevel(currentLevel);
        rootLogger.addHandler(handler);
        
        // Configura todos os loggers existentes
        LogManager.getLogManager().getLoggerNames().asIterator().forEachRemaining(name -> {
            Logger logger = LogManager.getLogManager().getLogger(name);
            if (logger != null) {
                logger.setLevel(currentLevel);
                // Remove handlers específicos
                for (Handler h : logger.getHandlers()) {
                    logger.removeHandler(h);
                }
            }
        });
    }

    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        logger.setLevel(currentLevel);
        return logger;
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }
}