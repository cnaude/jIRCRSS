package com.cnaude.jircrss;

import com.cnaude.jircrss.IRCListeners.ConnectListener;
import com.cnaude.jircrss.IRCListeners.JoinListener;
import com.cnaude.jircrss.IRCListeners.MessageListener;
import com.google.common.base.Joiner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Hello world!
 *
 */
public class IRC {

    private static PircBotX bot;
    private static String ircServer;
    private static int ircServerPort;
    private static String connectMessage;
    private static final Timer timer = new Timer();
    private static boolean debugMode;
    private static final ArrayList<ListenerAdapter> ircListeners = new ArrayList<>();
    private static String botNick;
    private static String botLogin;
    private static String botRealName;
    private static long chatDelay;
    private static String ircServerPass;    
    private static String botIdentPassword;
    private static boolean ssl = true;
    private static boolean trustAllCerts;
    private static String charSet;
    private static boolean autoConnect;
    public static String channel;    
    static ArrayList<RSSWatcher> rssWatchers = new ArrayList<>();
    static BotWatcher botWatcher;
    public static long rssRefreshRate;
    public static long botAliveCheckInterval;
    public static ArrayList<String> feeds = new ArrayList<>();

    public static void main(String[] args) {
        loadConfig();
        buildBot();

    }

    public static void loadConfig() {
        File configFile = new File("config.yml");
        InputStream is;
        try {
            is = new FileInputStream(configFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IRC.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        Yaml config = new Yaml(new SafeConstructor());
        Map<String, Object> object = (Map<String, Object>) config.load(is);

        botNick = getYamlString(object, "nick", "AwesomeBot");
        botLogin = getYamlString(object, "login", "AwesomeBot");
        botRealName = getYamlString(object, "real-name", "So Awesome");
        botIdentPassword = getYamlString(object, "ident-password", "");
        ircServer = getYamlString(object, "server", "localhost");
        ircServerPort = getYamlInt(object, "port", 6667);
        ircServerPass = getYamlString(object, "server-password", "");
        channel = getYamlString(object, "channel", "#awesome-channel");
        ssl = getYamlBool(object, "ssl", false);
        trustAllCerts = getYamlBool(object, "trust-all-certs", true);
        chatDelay = getYamlLong(object, "chat-delay", 2000);
        charSet = getYamlString(object, "character-set", "");
        autoConnect = getYamlBool(object, "auto-connect", true);
        botAliveCheckInterval = getYamlLong(object, "bot-alive-check-interval", 60);
        rssRefreshRate = getYamlLong(object, "rss-feed-interval", 300);
        feeds = getYamlList(object, "feeds", "http://rss.slashdot.org/Slashdot/slashdot");        

        addListeners();
    }

    private static ArrayList<String> getYamlList(Map<String, Object> object, String key, String def) {
        ArrayList<String> l = new ArrayList<>();
        if (object.containsKey(key)) {
            if ((List) object.get(key) != null) {
                l.addAll((List) object.get(key));                
                return l;
            }
        }
        l.add(def);
        return l;
    }

    private static String getYamlString(Map<String, Object> object, String key, String def) {
        if (object.containsKey(key)) {
            if ((String) object.get(key) != null) {
                return (String) object.get(key);
            }
        }
        return def;
    }

    public static void startFeedWatcher() {
        logDebug("Starting feed watcher(s)");
        if (!feeds.isEmpty()) {
            for (String s : feeds) {
                rssWatchers.add(new RSSWatcher(bot, s));
            }
        }
    }

    public static void startBotWatcher() {
        logDebug("Starting bot watcher");
        if (botWatcher == null) {
            botWatcher = new BotWatcher(bot);
        }
    }

    private static Long getYamlLong(Map<String, Object> object, String key, long def) {
        if (object.containsKey(key)) {
            return new Long((Integer) object.get(key));
        } else {
            return def;
        }
    }

    private static int getYamlInt(Map<String, Object> object, String key, int def) {
        if (object.containsKey(key)) {
            return (Integer) object.get(key);
        } else {
            return def;
        }
    }

    private static Boolean getYamlBool(Map<String, Object> object, String key, Boolean def) {
        if (object.containsKey(key)) {
            return (Boolean) object.get(key);
        } else {
            return def;
        }
    }

    public static void buildBot() {

        Configuration.Builder configBuilder = new Configuration.Builder()
                .setName(botNick)
                .setLogin(botLogin)
                .setAutoNickChange(true)
                .setCapEnabled(true)
                .setMessageDelay(chatDelay)
                .setRealName(botRealName)
                .setAutoReconnect(autoConnect)
                .setServer(ircServer, ircServerPort, ircServerPass);
        addAutoJoinChannels(configBuilder);
        for (ListenerAdapter ll : ircListeners) {
            configBuilder.addListener(ll);
        }
        if (!botIdentPassword.isEmpty()) {
            logInfo("Setting IdentPassword ...");
            configBuilder.setNickservPassword(botIdentPassword);
        }
        if (ssl) {
            UtilSSLSocketFactory socketFactory = new UtilSSLSocketFactory();
            socketFactory.disableDiffieHellman();
            if (trustAllCerts) {
                logInfo("Enabling SSL and trusting all certificates ...");
                socketFactory.trustAllCertificates();
            } else {
                logInfo("Enabling SSL ...");
            }
            configBuilder.setSocketFactory(socketFactory);
        }
        if (charSet.isEmpty()) {
            logInfo("Using default character set: " + Charset.defaultCharset());
        } else {
            if (Charset.isSupported(charSet)) {
                logInfo("Using character set: " + charSet);
                configBuilder.setEncoding(Charset.forName(charSet));
            } else {
                logError("Invalid character set: " + charSet);
                logInfo("Available character sets: " + Joiner.on(", ").join(Charset.availableCharsets().keySet()));
                logInfo("Using default character set: " + Charset.defaultCharset());
            }
        }
        Configuration configuration = configBuilder.buildConfiguration();
        bot = new PircBotX(configuration);
        if (autoConnect) {
            asyncConnect();
        } else {
            logInfo("Auto-connect is disabled. To connect: /irc connect " + bot.getNick());
        }
    }

    private static void addListeners() {
        ircListeners.add(new MessageListener());
        ircListeners.add(new ConnectListener());
        ircListeners.add(new JoinListener());
    }

    public static void asyncConnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logInfo(connectMessage);
                    bot.startBot();
                } catch (IOException | IrcException ex) {
                    logError("Problem connecting to " + ircServer + " => "
                            + " as " + bot.getNick() + " [Error: " + ex.getMessage() + "]");
                }
            }
        }).start();
    }

    private static void addAutoJoinChannels(Configuration.Builder configBuilder) {
        configBuilder.addAutoJoinChannel(channel);
        /*
         for (String channelName : botChannels) {
         if (channelAutoJoin.containsKey(channelName)) {
         if (channelAutoJoin.get(channelName)) {
         if (channelPassword.get(channelName).isEmpty()) {
         configBuilder.addAutoJoinChannel(channelName);
         } else {
         configBuilder.addAutoJoinChannel(channelName, channelPassword.get(channelName));
         }
         }
         }
         }
         */
    }

    public static void shutdown() {
        logInfo("Stopping the bot...");
        botWatcher.cancel();
        bot.stopBotReconnect();
        bot.sendIRC().quitServer();
        timer.cancel();
        for (RSSWatcher rssWatcher : rssWatchers) {
            rssWatcher.cancel();
        }

    }

    /**
     *
     * @param message
     */
    public static void logInfo(String message) {
        //log.log(Level.INFO, String.format("%s %s", LOG_HEADER, message));
        System.out.println(message);
    }

    /**
     *
     * @param message
     */
    public static void logError(String message) {
        //log.log(Level.SEVERE, String.format("%s %s", LOG_HEADER, message));
        System.out.println(message);
    }

    /**
     *
     * @param message
     */
    public static void logDebug(String message) {
        if (debugMode) {
            System.out.println(message);
        }
        System.out.println(message);
    }

}
