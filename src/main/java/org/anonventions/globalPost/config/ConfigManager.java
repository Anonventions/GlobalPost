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

        config.addDefault("channels.server1", Arrays.asList("server2", "server3"));
        config.addDefault("channels.server2", Collections.singletonList("server1"));

        config.addDefault("blacklist.items", Arrays.asList(
                "SHULKER_BOX","WHITE_SHULKER_BOX","ORANGE_SHULKER_BOX","MAGENTA_SHULKER_BOX",
                "LIGHT_BLUE_SHULKER_BOX","YELLOW_SHULKER_BOX","LIME_SHULKER_BOX","PINK_SHULKER_BOX",
                "GRAY_SHULKER_BOX","LIGHT_GRAY_SHULKER_BOX","CYAN_SHULKER_BOX","PURPLE_SHULKER_BOX",
                "BLUE_SHULKER_BOX","BROWN_SHULKER_BOX","GREEN_SHULKER_BOX","RED_SHULKER_BOX",
                "BLACK_SHULKER_BOX"));

        config.addDefault("blacklist.custom_items.display_names", Arrays.asList("§cAdmin Item", "§4[BANNED]"));
        config.addDefault("blacklist.custom_items.lore_contains", Arrays.asList("ADMIN ONLY", "NOT TRADEABLE"));
        config.addDefault("blacklist.custom_items.nbt_keys", Arrays.asList("CustomAdminData", "UntradableItem"));

        config.addDefault("settings.max_items_per_mail",   27);
        config.addDefault("settings.max_mails_per_player", 50);
        config.addDefault("settings.mail_expiry_days",     30);
        config.addDefault("settings.invisible_borders",    false);

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
        return config.getStringList("channels." + getServerName()).stream()
                .map(this::normalised)
                .distinct()
                .toList();
    }

    public List<String> getBlacklistedItems() { return config.getStringList("blacklist.items"); }
    public List<String> getBlacklistedDisplayNames() { return config.getStringList("blacklist.custom_items.display_names"); }
    public List<String> getBlacklistedLoreContains() { return config.getStringList("blacklist.custom_items.lore_contains"); }
    public List<String> getBlacklistedNBTKeys() { return config.getStringList("blacklist.custom_items.nbt_keys"); }
    public int  getMaxItemsPerMail()          { return config.getInt("settings.max_items_per_mail"); }
    public int  getMaxMailsPerPlayer()        { return config.getInt("settings.max_mails_per_player"); }
    public int  getMailExpiryDays()           { return config.getInt("settings.mail_expiry_days"); }
    public boolean hasInvisibleBorders()      { return config.getBoolean("settings.invisible_borders"); }
}
