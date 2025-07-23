package org.anonventions.globalPost;

import org.anonventions.globalPost.commands.PostCommand;
import org.anonventions.globalPost.config.ConfigManager;
import org.anonventions.globalPost.database.DatabaseManager;
import org.anonventions.globalPost.gui.MessageInputHandler;
import org.anonventions.globalPost.listeners.PlayerListener;
import org.anonventions.globalPost.managers.ItemBlacklistManager;
import org.anonventions.globalPost.managers.MailboxManager;
import org.anonventions.globalPost.managers.SoundManager;
import org.anonventions.globalPost.messaging.PluginMessageHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class GlobalPost extends JavaPlugin {

    private static GlobalPost instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private MailboxManager mailboxManager;
    private ItemBlacklistManager blacklistManager;
    private PluginMessageHandler messageHandler;
    private SoundManager soundManager;
    private MessageInputHandler messageInputHandler;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Initialize database
        databaseManager = new DatabaseManager(this);
        if (!databaseManager.initialize()) {
            getLogger().severe("Failed to initialize database! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize managers
        blacklistManager = new ItemBlacklistManager(this);
        mailboxManager = new MailboxManager(this);
        soundManager = new SoundManager(this);
        messageInputHandler = new MessageInputHandler(this);

        // Initialize plugin messaging
        messageHandler = new PluginMessageHandler(this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "globalpost:main");
        getServer().getMessenger().registerIncomingPluginChannel(this, "globalpost:main", messageHandler);

        // Register commands
        getCommand("post").setExecutor(new PostCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("GlobalPost has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }

        // Unregister plugin messaging
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getServer().getMessenger().unregisterIncomingPluginChannel(this);

        getLogger().info("GlobalPost has been disabled.");
    }

    public static GlobalPost getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public MailboxManager getMailboxManager() {
        return mailboxManager;
    }

    public ItemBlacklistManager getBlacklistManager() {
        return blacklistManager;
    }

    public PluginMessageHandler getMessageHandler() {
        return messageHandler;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public MessageInputHandler getMessageInputHandler() {
        return messageInputHandler;
    }
}