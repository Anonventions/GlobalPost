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

        config.addDefault("settings.max_items_per_mail",   27);
        config.addDefault("settings.max_mails_per_player", 50);
        config.addDefault("settings.mail_expiry_days",     30);
        config.addDefault("settings.enable_local_mail", true);

        // Sound defaults
        config.addDefault("sounds.enabled", true);
        config.addDefault("sounds.gui_open", "UI_BUTTON_CLICK");
        config.addDefault("sounds.mail_send", "ENTITY_PLAYER_LEVELUP");
        config.addDefault("sounds.mail_receive", "BLOCK_NOTE_BLOCK_BELL");
        config.addDefault("sounds.volume", 1.0);
        config.addDefault("sounds.pitch", 1.0);

        // GUI defaults for mailbox
        config.addDefault("gui.mailbox.title", "§6§lMailbox");
        config.addDefault("gui.mailbox.player_head_slot", 4);
        config.addDefault("gui.mailbox.content_slots", Arrays.asList(10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43));
        config.addDefault("gui.mailbox.pagination_slots", Arrays.asList(46, 47, 48, 49, 50, 51, 52));
        
        // GUI item defaults
        config.addDefault("gui.mailbox.border_item.material", "GRAY_STAINED_GLASS_PANE");
        config.addDefault("gui.mailbox.border_item.custom_model_data", 0);
        config.addDefault("gui.mailbox.border_item.name", " ");
        config.addDefault("gui.mailbox.border_item.lore", Collections.emptyList());

        config.addDefault("gui.mailbox.refresh.material", "COMPASS");
        config.addDefault("gui.mailbox.refresh.custom_model_data", 0);
        config.addDefault("gui.mailbox.refresh.name", "§bRefresh");
        config.addDefault("gui.mailbox.refresh.lore", Arrays.asList("§7Click to refresh your mailbox"));
        config.addDefault("gui.mailbox.refresh.slot", 50);

        config.addDefault("gui.mailbox.send_mail.material", "WRITABLE_BOOK");
        config.addDefault("gui.mailbox.send_mail.custom_model_data", 0);
        config.addDefault("gui.mailbox.send_mail.name", "§aSend Mail");
        config.addDefault("gui.mailbox.send_mail.lore", Arrays.asList("§7Click to send mail to another server"));
        config.addDefault("gui.mailbox.send_mail.slot", 49);

        // Message system defaults
        config.addDefault("messages.enabled", true);
        config.addDefault("messages.max_length", 255);
        config.addDefault("messages.chat_input.timeout", 30);
        config.addDefault("messages.chat_input.prompt", "§eType your message (or 'cancel' to abort):");
        config.addDefault("messages.chat_input.accept_prompt", "§aType 'confirm' to send or 'cancel' to abort:");

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
    public int  getMaxItemsPerMail()          { return config.getInt("settings.max_items_per_mail"); }
    public int  getMaxMailsPerPlayer()        { return config.getInt("settings.max_mails_per_player"); }
    public int  getMailExpiryDays()           { return config.getInt("settings.mail_expiry_days"); }
    public boolean isLocalMailEnabled()       { return config.getBoolean("settings.enable_local_mail"); }
    
    /* Sound getters ---------------------------------------------------------*/
    public boolean isSoundEnabled()           { return config.getBoolean("sounds.enabled"); }
    public String getGuiOpenSound()           { return config.getString("sounds.gui_open"); }
    public String getMailSendSound()          { return config.getString("sounds.mail_send"); }
    public String getMailReceiveSound()       { return config.getString("sounds.mail_receive"); }
    public float getSoundVolume()             { return (float) config.getDouble("sounds.volume"); }
    public float getSoundPitch()              { return (float) config.getDouble("sounds.pitch"); }
    
    /* GUI getters -----------------------------------------------------------*/
    public String getMailboxTitle()           { return config.getString("gui.mailbox.title"); }
    public int getPlayerHeadSlot()            { return config.getInt("gui.mailbox.player_head_slot"); }
    public List<Integer> getContentSlots()    { return config.getIntegerList("gui.mailbox.content_slots"); }
    public List<Integer> getPaginationSlots() { return config.getIntegerList("gui.mailbox.pagination_slots"); }
    
    public String getBorderMaterial()         { return config.getString("gui.mailbox.border_item.material"); }
    public int getBorderCustomModelData()     { return config.getInt("gui.mailbox.border_item.custom_model_data"); }
    public String getBorderName()             { return config.getString("gui.mailbox.border_item.name"); }
    public List<String> getBorderLore()       { return config.getStringList("gui.mailbox.border_item.lore"); }
    
    public String getRefreshMaterial()        { return config.getString("gui.mailbox.refresh.material"); }
    public int getRefreshCustomModelData()    { return config.getInt("gui.mailbox.refresh.custom_model_data"); }
    public String getRefreshName()            { return config.getString("gui.mailbox.refresh.name"); }
    public List<String> getRefreshLore()      { return config.getStringList("gui.mailbox.refresh.lore"); }
    public int getRefreshSlot()               { return config.getInt("gui.mailbox.refresh.slot"); }
    
    public String getSendMailMaterial()       { return config.getString("gui.mailbox.send_mail.material"); }
    public int getSendMailCustomModelData()   { return config.getInt("gui.mailbox.send_mail.custom_model_data"); }
    public String getSendMailName()           { return config.getString("gui.mailbox.send_mail.name"); }
    public List<String> getSendMailLore()     { return config.getStringList("gui.mailbox.send_mail.lore"); }
    public int getSendMailSlot()              { return config.getInt("gui.mailbox.send_mail.slot"); }
    
    /* Message system getters -----------------------------------------------*/
    public boolean isMessagingEnabled()       { return config.getBoolean("messages.enabled"); }
    public int getMaxMessageLength()          { return config.getInt("messages.max_length"); }
    public int getChatInputTimeout()          { return config.getInt("messages.chat_input.timeout"); }
    public String getChatInputPrompt()        { return config.getString("messages.chat_input.prompt"); }
    public String getChatAcceptPrompt()       { return config.getString("messages.chat_input.accept_prompt"); }
}
