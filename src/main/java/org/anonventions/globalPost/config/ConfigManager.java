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
        config.addDefault("gui.mailbox.previous_page_slot", 46);
        config.addDefault("gui.mailbox.next_page_slot", 52);
        config.addDefault("gui.mailbox.refresh_slot", 50);
        config.addDefault("gui.mailbox.send_mail_slot", 49);
        
        // Mail item defaults
        config.addDefault("gui.mailbox.mail_item.material", "PAPER");
        config.addDefault("gui.mailbox.mail_item.custom_model_data", 0);
        config.addDefault("gui.mailbox.mail_item.name", "§6Mail from {sender}");
        config.addDefault("gui.mailbox.mail_item.lore", Arrays.asList(
            "§7From: §f{sender}", "§7Server: §f{server}", "§7Time: §f{time}",
            "§7Items: §f{items}", "§7Message: §f{message}", "", "§aClick to collect!"
        ));
        
        // Button defaults
        config.addDefault("gui.mailbox.refresh_button.material", "COMPASS");
        config.addDefault("gui.mailbox.refresh_button.custom_model_data", 0);
        config.addDefault("gui.mailbox.refresh_button.name", "§bRefresh");
        config.addDefault("gui.mailbox.refresh_button.lore", Arrays.asList("§7Click to refresh your mailbox"));
        
        config.addDefault("gui.mailbox.send_mail_button.material", "WRITABLE_BOOK");
        config.addDefault("gui.mailbox.send_mail_button.custom_model_data", 0);
        config.addDefault("gui.mailbox.send_mail_button.name", "§aSend Mail");
        config.addDefault("gui.mailbox.send_mail_button.lore", Arrays.asList("§7Click to send mail to another server"));
        
        // Border item defaults for mailbox
        config.addDefault("gui.mailbox.border_item.material", "GRAY_STAINED_GLASS_PANE");
        config.addDefault("gui.mailbox.border_item.custom_model_data", 0);
        config.addDefault("gui.mailbox.border_item.name", " ");
        config.addDefault("gui.mailbox.border_item.lore", Arrays.asList());
        
        // GUI defaults for sendmail
        config.addDefault("gui.sendmail.title", "§6§lSend Mail to {recipient}");
        config.addDefault("gui.sendmail.player_head_slot", 4);
        config.addDefault("gui.sendmail.content_slots", Arrays.asList(10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43));
        config.addDefault("gui.sendmail.send_button_slot", 48);
        config.addDefault("gui.sendmail.message_button_slot", 49);
        config.addDefault("gui.sendmail.cancel_button_slot", 45);
        
        // SendMail button defaults
        config.addDefault("gui.sendmail.send_button.material", "GREEN_WOOL");
        config.addDefault("gui.sendmail.send_button.custom_model_data", 0);
        config.addDefault("gui.sendmail.send_button.name", "§a§lSend Mail");
        config.addDefault("gui.sendmail.send_button.lore", Arrays.asList(
            "§7To: §f{recipient}", "§7Server: §f{server}", "", "§aClick to send!"
        ));
        
        config.addDefault("gui.sendmail.message_button.material", "WRITABLE_BOOK");
        config.addDefault("gui.sendmail.message_button.custom_model_data", 0);
        config.addDefault("gui.sendmail.message_button.name", "§e§lAdd Message");
        config.addDefault("gui.sendmail.message_button.lore", Arrays.asList(
            "§7Click to add a message", "§7to your mail package", "", "§7Current: §f{message}"
        ));
        
        config.addDefault("gui.sendmail.cancel_button.material", "RED_WOOL");
        config.addDefault("gui.sendmail.cancel_button.custom_model_data", 0);
        config.addDefault("gui.sendmail.cancel_button.name", "§c§lCancel");
        config.addDefault("gui.sendmail.cancel_button.lore", Arrays.asList("§7Click to cancel and return items"));
        
        // Border item defaults for sendmail
        config.addDefault("gui.sendmail.border_item.material", "GRAY_STAINED_GLASS_PANE");
        config.addDefault("gui.sendmail.border_item.custom_model_data", 0);
        config.addDefault("gui.sendmail.border_item.name", " ");
        config.addDefault("gui.sendmail.border_item.lore", Arrays.asList());

        // Message system defaults
        config.addDefault("messages.enabled", true);
        config.addDefault("messages.max_length", 255);
        config.addDefault("messages.timeout_seconds", 30);

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
    public int getPreviousPageSlot()          { return config.getInt("gui.mailbox.previous_page_slot"); }
    public int getNextPageSlot()              { return config.getInt("gui.mailbox.next_page_slot"); }
    public int getRefreshSlot()               { return config.getInt("gui.mailbox.refresh_slot"); }
    public int getSendMailSlot()              { return config.getInt("gui.mailbox.send_mail_slot"); }
    
    // Mail item customization
    public String getMailItemMaterial()       { return config.getString("gui.mailbox.mail_item.material"); }
    public int getMailItemCustomModelData()   { return config.getInt("gui.mailbox.mail_item.custom_model_data"); }
    public String getMailItemName()           { return config.getString("gui.mailbox.mail_item.name"); }
    public List<String> getMailItemLore()     { return config.getStringList("gui.mailbox.mail_item.lore"); }
    
    // Button customization
    public String getRefreshButtonMaterial()  { return config.getString("gui.mailbox.refresh_button.material"); }
    public int getRefreshButtonCustomModelData() { return config.getInt("gui.mailbox.refresh_button.custom_model_data"); }
    public String getRefreshButtonName()      { return config.getString("gui.mailbox.refresh_button.name"); }
    public List<String> getRefreshButtonLore() { return config.getStringList("gui.mailbox.refresh_button.lore"); }
    
    public String getSendMailButtonMaterial() { return config.getString("gui.mailbox.send_mail_button.material"); }
    public int getSendMailButtonCustomModelData() { return config.getInt("gui.mailbox.send_mail_button.custom_model_data"); }
    public String getSendMailButtonName()     { return config.getString("gui.mailbox.send_mail_button.name"); }
    public List<String> getSendMailButtonLore() { return config.getStringList("gui.mailbox.send_mail_button.lore"); }
    
    // Border item customization
    public String getBorderItemMaterial()      { return config.getString("gui.mailbox.border_item.material"); }
    public int getBorderItemCustomModelData()  { return config.getInt("gui.mailbox.border_item.custom_model_data"); }
    public String getBorderItemName()          { return config.getString("gui.mailbox.border_item.name"); }
    public List<String> getBorderItemLore()    { return config.getStringList("gui.mailbox.border_item.lore"); }
    
    public String getSendMailTitle()          { return config.getString("gui.sendmail.title"); }
    public int getSendMailPlayerHeadSlot()    { return config.getInt("gui.sendmail.player_head_slot"); }
    public List<Integer> getSendMailContentSlots() { return config.getIntegerList("gui.sendmail.content_slots"); }
    public int getSendButtonSlot()            { return config.getInt("gui.sendmail.send_button_slot"); }
    public int getMessageButtonSlot()         { return config.getInt("gui.sendmail.message_button_slot"); }
    public int getCancelButtonSlot()          { return config.getInt("gui.sendmail.cancel_button_slot"); }
    
    // SendMail button customization
    public String getSendButtonMaterial()     { return config.getString("gui.sendmail.send_button.material"); }
    public int getSendButtonCustomModelData() { return config.getInt("gui.sendmail.send_button.custom_model_data"); }
    public String getSendButtonName()         { return config.getString("gui.sendmail.send_button.name"); }
    public List<String> getSendButtonLore()   { return config.getStringList("gui.sendmail.send_button.lore"); }
    
    public String getMessageButtonMaterial()  { return config.getString("gui.sendmail.message_button.material"); }
    public int getMessageButtonCustomModelData() { return config.getInt("gui.sendmail.message_button.custom_model_data"); }
    public String getMessageButtonName()      { return config.getString("gui.sendmail.message_button.name"); }
    public List<String> getMessageButtonLore() { return config.getStringList("gui.sendmail.message_button.lore"); }
    
    public String getCancelButtonMaterial()   { return config.getString("gui.sendmail.cancel_button.material"); }
    public int getCancelButtonCustomModelData() { return config.getInt("gui.sendmail.cancel_button.custom_model_data"); }
    public String getCancelButtonName()       { return config.getString("gui.sendmail.cancel_button.name"); }
    public List<String> getCancelButtonLore() { return config.getStringList("gui.sendmail.cancel_button.lore"); }
    
    // SendMail border item customization
    public String getSendMailBorderItemMaterial() { return config.getString("gui.sendmail.border_item.material"); }
    public int getSendMailBorderItemCustomModelData() { return config.getInt("gui.sendmail.border_item.custom_model_data"); }
    public String getSendMailBorderItemName()   { return config.getString("gui.sendmail.border_item.name"); }
    public List<String> getSendMailBorderItemLore() { return config.getStringList("gui.sendmail.border_item.lore"); }
    
    /* Message system getters -----------------------------------------------*/
    public boolean isMessagingEnabled()       { return config.getBoolean("messages.enabled"); }
    public int getMaxMessageLength()          { return config.getInt("messages.max_length"); }
    public int getChatInputTimeout()          { return config.getInt("messages.timeout_seconds"); }
    public String getChatInputPrompt()        { return "§eType your message (or 'cancel' to abort):"; }
    public String getChatAcceptPrompt()       { return "§aType 'confirm' to send or 'cancel' to abort:"; }
}
