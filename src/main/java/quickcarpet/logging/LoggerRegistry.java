package quickcarpet.logging;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DyeColor;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LoggerRegistry
{
    private static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
    // Map from logger names to loggers.
    private static Map<String, Logger> loggerRegistry = new HashMap<>();
    // Map from player names to the set of names of the logs that player is subscribed to.
    private static Map<String, Map<String, String>> playerSubscriptions = new HashMap<>();
    //statics to quickly asses if its worth even to call each one
    public static boolean __tnt;
    // public static boolean __projectiles;
    // public static boolean __fallingBlocks;
    // public static boolean __kills;
    public static boolean __tps;
    public static boolean __counter;
    public static boolean __mobcaps;
    // public static boolean __damage;
    public static boolean __packets;
    // public static boolean __weather;
    // public static boolean __tileTickLimit;
    public static boolean __banner;

    public static void initLoggers()
    {
        registerLogger("tnt", new Logger("tnt", "brief", new String[]{"brief", "full"}, LogHandler.CHAT));
        // registerLogger("projectiles", new Logger("projectiles", "full",  new String[]{"brief", "full"}, LogHandler.CHAT));
        // registerLogger("fallingBlocks",new Logger("fallingBlocks", "brief", new String[]{"brief", "full"}, LogHandler.CHAT));
        // registerLogger("kills", new Logger("kills", null, null, LogHandler.CHAT));
        // registerLogger("damage", new Logger("damage", "all", new String[]{"all","players","me"}, LogHandler.CHAT));
        // registerLogger("weather", new Logger("weather", null, null, LogHandler.CHAT));
        // registerLogger("tileTickLimit", new Logger("tileTickLimit", null, null, LogHandler.CHAT));
        registerLogger("banner", new Logger("banner", null, null, LogHandler.CHAT));

        registerLogger("tps", new Logger("tps", null, null, LogHandler.HUD));
        registerLogger("packets", new Logger("packets", null, null, LogHandler.HUD));
        registerLogger("counter",new Logger("counter","white", Arrays.stream(DyeColor.values()).map(Object::toString).toArray(String[]::new), LogHandler.HUD));
        registerLogger("mobcaps", new Logger("mobcaps", "dynamic",new String[]{"dynamic", "overworld", "nether","end"}, LogHandler.HUD));
    }

    /**
     * Gets the logger with the given name. Returns null if no such logger exists.
     */
    public static Logger getLogger(String name) { return loggerRegistry.get(name); }

    /**
     * Gets the set of logger names.
     */
    public static Set<String> getLoggerNames() { return loggerRegistry.keySet(); }

    /**
     * Subscribes the player with name playerName to the log with name logName.
     */
    public static void subscribePlayer(String playerName, String logName, String option, LogHandler handler)
    {
        if (!playerSubscriptions.containsKey(playerName)) playerSubscriptions.put(playerName, new HashMap<>());
        Logger log = loggerRegistry.get(logName);
        if (option == null) option = log.getDefault();
        playerSubscriptions.get(playerName).put(logName,option);
        log.addPlayer(playerName, option, handler);
    }

    /**
     * Unsubscribes the player with name playerName from the log with name logName.
     */
    public static void unsubscribePlayer(String playerName, String logName)
    {
        if (playerSubscriptions.containsKey(playerName))
        {
            Map<String,String> subscriptions = playerSubscriptions.get(playerName);
            subscriptions.remove(logName);
            loggerRegistry.get(logName).removePlayer(playerName);
            if (subscriptions.size() == 0) playerSubscriptions.remove(playerName);
        }
    }

    /**
     * If the player is not subscribed to the log, then subscribe them. Otherwise, unsubscribe them.
     */
    public static boolean togglePlayerSubscription(String playerName, String logName, LogHandler handler)
    {
        if (playerSubscriptions.containsKey(playerName) && playerSubscriptions.get(playerName).containsKey(logName))
        {
            unsubscribePlayer(playerName, logName);
            return false;
        }
        else
        {
            subscribePlayer(playerName, logName, null, handler);
            return true;
        }
    }

    /**
     * Get the set of logs the current player is subscribed to.
     */
    public static Map<String,String> getPlayerSubscriptions(String playerName)
    {
        if (playerSubscriptions.containsKey(playerName))
        {
            return playerSubscriptions.get(playerName);
        }
        return null;
    }

    protected static void setAccess(Logger logger)
    {
        String name = logger.getLogName();
        boolean value = logger.hasOnlineSubscribers();
        try
        {
            Field f = LoggerRegistry.class.getDeclaredField("__"+name);
            f.setBoolean(null, value);
        }
        catch (IllegalAccessException e)
        {
            LOGGER.error("Cannot change logger quick access field");
        }
        catch (NoSuchFieldException e)
        {
            LOGGER.error("Wrong logger name");
        }
    }
    /**
     * Called when the server starts. Creates the logs used by Carpet mod.
     */
    private static void registerLogger(String name, Logger logger)
    {
        loggerRegistry.put(name, logger);
        setAccess(logger);
    }

    public static void playerConnected(PlayerEntity player)
    {
        for(Logger log: loggerRegistry.values() )
        {
            log.onPlayerConnect(player);
        }

    }
    public static void playerDisconnected(PlayerEntity player)
    {
        for(Logger log: loggerRegistry.values() )
        {
            log.onPlayerDisconnect(player);
        }
    }



}
