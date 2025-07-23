/*─────────────────────────────────────────────────────────────────────────────
 *  org/anonventions/globalPost/config/ConfigManager.java
 *───────────────────────────────────────────────────────────────────────────*/
package org.anonventions.globalPost.config;

import org.anonventions.globalPost.GlobalPost;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Loads, saves, and sanitises config.yml.
 * – All server names are trimmed + lower‑cased once on startup so comparisons
 *   across a proxy/cluster are always case‑insensitive.
 */
public class ConfigManager {

    private final GlobalPost     plugin;
    private       FileConfiguration config;
    private       File             configFile;

    public ConfigManager(GlobalPost plugin) { this.plugin = plugin; }

    /*------------------------------------------------------------------------*/
    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) plugin.saveDefaultConfig();

        config = YamlConfiguration.loadConfiguration(configFile);
        setDefaults();      // add missing keys
        sanitise();         // normalise names
        saveConfig();
    }

    private void setDefaults() {
        config.addDefault("database.type",            "sqlite");
        config.addDefault("database.sqlite.file",     "globalpost.db");
        config.addDefault("database.mysql.host",      "localhost");
        config.addDefault("database.mysql.port",      3306);
        config.addDefault("database.mysql.database",  "globalpost");
        config.addDefault("database.mysql.username",  "root");
        config.addDefault("database.mysql.password",  "password");

        config.addDefault("server.name", "server1");

        // Only set default channels if this is a fresh config
        // This allows single-server setups to work without channel configuration
        if (!config.contains("channels")) {
            config.addDefault("channels.server1", Arrays.asList("server2", "server3"));
            config.addDefault("channels.server2", Collections.singletonList("server1"));
        }

        config.addDefault("blacklist.items", Arrays.asList(
                "SHULKER_BOX","WHITE_SHULKER_BOX","ORANGE_SHULKER_BOX","MAGENTA_SHULKER_BOX",
                "LIGHT_BLUE_SHULKER_BOX","YELLOW_SHULKER_BOX","LIME_SHULKER_BOX","PINK_SHULKER_BOX",
                "GRAY_SHULKER_BOX","LIGHT_GRAY_SHULKER_BOX","CYAN_SHULKER_BOX","PURPLE_SHULKER_BOX",
                "BLUE_SHULKER_BOX","BROWN_SHULKER_BOX","GREEN_SHULKER_BOX","RED_SHULKER_BOX",
                "BLACK_SHULKER_BOX"));

        config.addDefault("settings.max_items_per_mail",   27);
        config.addDefault("settings.max_mails_per_player", 50);
        config.addDefault("settings.mail_expiry_days",     30);

        config.options().copyDefaults(true);
    }

    /** Trim + lower‑case every server identifier to a single canonical form. */
    private void sanitise() {
        String canonical = normalised(config.getString("server.name"));
        config.set("server.name", canonical);

        final String channelsRoot = "channels";
        if (config.isConfigurationSection(channelsRoot)) {
            config.getConfigurationSection(channelsRoot).getKeys(false).forEach(key -> {

                List<String> cleaned = config.getStringList(channelsRoot + "." + key).stream()
                        .map(this::normalised)
                        .collect(Collectors.toCollection(ArrayList::new));

                config.set(channelsRoot + "." + normalised(key), cleaned);

                if (!key.equalsIgnoreCase(normalised(key)))
                    config.set(channelsRoot + "." + key, null); // remove original
            });
        }
    }

    public void saveConfig() {
        try { config.save(configFile); }
        catch (IOException e) { plugin.getLogger().severe("Could not save config.yml: " + e); }
    }

    /*------------------------------------------------------------------------*/
    public String normalised(String raw) { return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT); }

    /* Database getters ------------------------------------------------------*/
    public String getDatabaseType()  { return config.getString("database.type", "sqlite"); }
    public String getSQLiteFile()    { return config.getString("database.sqlite.file"); }
    public String getMySQLHost()     { return config.getString("database.mysql.host"); }
    public int    getMySQLPort()     { return config.getInt("database.mysql.port"); }
    public String getMySQLDatabase() { return config.getString("database.mysql.database"); }
    public String getMySQLUsername() { return config.getString("database.mysql.username"); }
    public String getMySQLPassword() { return config.getString("database.mysql.password"); }

    /* General getters -------------------------------------------------------*/
    /** Always canonical form. */
    public String getServerName() { return normalised(config.getString("server.name")); }

    /** Allowed destination list (canonical, de‑duplicated). */
    public List<String> getAllowedDestinations() {
        List<String> destinations = config.getStringList("channels." + getServerName()).stream()
                .map(this::normalised)
                .distinct()
                .toList();
        
        // If no specific channels are configured for this server
        if (destinations.isEmpty()) {
            // Check if channels section exists
            if (config.isConfigurationSection("channels")) {
                Set<String> allChannelKeys = config.getConfigurationSection("channels").getKeys(false);
                
                // If there are no channel configurations at all, allow sending to self (single-server mode)
                if (allChannelKeys.isEmpty()) {
                    plugin.getLogger().info("No channel configurations found, enabling single-server mode for: " + getServerName());
                    return List.of(getServerName());
                }
                
                // Check if this is a single-server setup by seeing if all configured servers 
                // point to the same server (meaning it's probably a hub or single-server setup)
                Set<String> allDestinations = new HashSet<>();
                for (String key : allChannelKeys) {
                    List<String> serverDests = config.getStringList("channels." + key);
                    allDestinations.addAll(serverDests.stream().map(this::normalised).toList());
                }
                
                // If this server appears as a destination but isn't configured to send anywhere,
                // it might be a legitimate single-server or hub setup - allow self-sending
                if (allDestinations.contains(getServerName())) {
                    plugin.getLogger().info("Server " + getServerName() + " is a mail destination but has no outgoing channels, enabling self-sending");
                    return List.of(getServerName());
                }
                
                // Otherwise, return empty list (strict multi-server mode where this server has no outgoing channels)
                plugin.getLogger().warning("Server " + getServerName() + " has no configured mail channels and is not a destination. Mail sending disabled.");
                return Collections.emptyList();
            } else {
                // No channels section exists - single server mode, allow sending to self
                plugin.getLogger().info("No channels section found, enabling single-server mode for: " + getServerName());
                return List.of(getServerName());
            }
        }
        
        plugin.getLogger().info("Server " + getServerName() + " can send mail to: " + destinations);
        return destinations;
    }

    public List<String> getBlacklistedItems() { return config.getStringList("blacklist.items"); }
    public int  getMaxItemsPerMail()          { return config.getInt("settings.max_items_per_mail"); }
    public int  getMaxMailsPerPlayer()        { return config.getInt("settings.max_mails_per_player"); }
    public int  getMailExpiryDays()           { return config.getInt("settings.mail_expiry_days"); }
}
