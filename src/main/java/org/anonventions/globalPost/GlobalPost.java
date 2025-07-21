package org.anonventions.globalPost;

import org.anonventions.globalPost.commands.PostCommand;
import org.anonventions.globalPost.config.ConfigManager;
import org.anonventions.globalPost.database.DatabaseManager;
import org.anonventions.globalPost.listeners.PlayerListener;
import org.anonventions.globalPost.managers.ItemBlacklistManager;
import org.anonventions.globalPost.managers.MailboxManager;
import org.anonventions.globalPost.messaging.PluginMessageHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class GlobalPost extends JavaPlugin {

    private static GlobalPost instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private MailboxManager mailboxManager;
    private ItemBlacklistManager blacklistManager;
    private PluginMessageHandler messageHandler;
    
    // Global item tracking to prevent duplication across multiple mail sessions
    private final Map<UUID, Map<String, Long>> playerItemTracking = new ConcurrentHashMap<>();

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

    /**
     * Track an item being used in mail to prevent duplication
     */
    public boolean trackItemForMail(UUID playerUUID, ItemStack item) {
        if (item == null) return false;
        
        String itemSignature = createItemSignature(item);
        Map<String, Long> playerItems = playerItemTracking.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());
        
        // Check if item is already being tracked
        if (playerItems.containsKey(itemSignature)) {
            return false; // Already tracked
        }
        
        // Track the item with current timestamp
        playerItems.put(itemSignature, System.currentTimeMillis());
        return true;
    }

    /**
     * Release an item from tracking
     */
    public void releaseItemTracking(UUID playerUUID, ItemStack item) {
        if (item == null) return;
        
        String itemSignature = createItemSignature(item);
        Map<String, Long> playerItems = playerItemTracking.get(playerUUID);
        if (playerItems != null) {
            playerItems.remove(itemSignature);
            if (playerItems.isEmpty()) {
                playerItemTracking.remove(playerUUID);
            }
        }
    }

    /**
     * Clear all tracking for a player (when they log out or mail session ends)
     */
    public void clearPlayerTracking(UUID playerUUID) {
        playerItemTracking.remove(playerUUID);
    }

    /**
     * Create a unique signature for an item based on its properties
     */
    private String createItemSignature(ItemStack item) {
        StringBuilder signature = new StringBuilder();
        signature.append(item.getType().name()).append(":");
        signature.append(item.getAmount()).append(":");
        
        if (item.hasItemMeta()) {
            signature.append(item.getItemMeta().toString());
        }
        
        return signature.toString();
    }
}