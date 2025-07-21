package org.anonventions.globalPost.gui;

import org.anonventions.globalPost.GlobalPost;
import org.anonventions.globalPost.ui.ThemeManager;
import org.anonventions.globalPost.ui.UITheme;
import org.anonventions.globalPost.ui.UIUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Statistics and analytics GUI showing mail system usage
 */
public class MailStatsGUI implements Listener {

    private final GlobalPost plugin;
    private final Player player;
    private final ThemeManager themeManager;
    private final Inventory inventory;
    private boolean isLoading;

    // Mock statistics (in a real implementation, these would come from database)
    private Map<String, Integer> mailByServer;
    private Map<String, Integer> mailBySender;
    private Map<String, Integer> itemsSent;
    private Map<String, Integer> itemsReceived;
    private int totalMailSent;
    private int totalMailReceived;
    private int totalItemsTransferred;
    private long firstMailDate;
    private long lastMailDate;

    // Layout constants
    private static final int OVERVIEW_SLOT = 4;
    private static final int SENT_STATS_SLOT = 10;
    private static final int RECEIVED_STATS_SLOT = 12;
    private static final int SERVER_STATS_SLOT = 14;
    private static final int ITEM_STATS_SLOT = 16;
    private static final int TOP_SENDERS_SLOT = 19;
    private static final int TOP_ITEMS_SLOT = 21;
    private static final int ACTIVITY_GRAPH_SLOT = 23;
    private static final int ACHIEVEMENTS_SLOT = 25;
    private static final int EXPORT_SLOT = 46;
    private static final int REFRESH_SLOT = 47;
    private static final int SETTINGS_SLOT = 48;
    private static final int BACK_SLOT = 49;
    private static final int HELP_SLOT = 50;

    public MailStatsGUI(GlobalPost plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.themeManager = plugin.getThemeManager();
        this.isLoading = true;

        UITheme theme = themeManager.getPlayerTheme(player);
        this.inventory = Bukkit.createInventory(null, 54, 
            theme.getPrimaryColor() + "📊 " + theme.getAccentColor() + "§lMail Statistics & Analytics");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadStatistics();
    }

    private void loadStatistics() {
        isLoading = true;
        updateLoadingDisplay();
        
        // Simulate loading statistics (in real implementation, load from database)
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Mock data generation
            generateMockStatistics();
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                isLoading = false;
                updateInventory();
            });
        });
    }

    private void generateMockStatistics() {
        // Generate realistic mock data
        Random random = new Random();
        
        mailByServer = new HashMap<>();
        mailBySender = new HashMap<>();
        itemsSent = new HashMap<>();
        itemsReceived = new HashMap<>();
        
        // Server statistics
        List<String> servers = new ArrayList<>(plugin.getConfigManager().getAllowedDestinations());
        for (String server : servers) {
            mailByServer.put(server, random.nextInt(50) + 1);
        }
        
        // Popular senders
        String[] senderNames = {"Steve", "Alex", "Notch", "Herobrine", "Enderman", "Creeper_King"};
        for (String sender : senderNames) {
            mailBySender.put(sender, random.nextInt(20) + 1);
        }
        
        // Popular items
        String[] items = {"Diamond", "Emerald", "Iron Ingot", "Gold Ingot", "Netherite", "Enchanted Book"};
        for (String item : items) {
            itemsSent.put(item, random.nextInt(100) + 10);
            itemsReceived.put(item, random.nextInt(100) + 10);
        }
        
        totalMailSent = mailByServer.values().stream().mapToInt(Integer::intValue).sum();
        totalMailReceived = totalMailSent + random.nextInt(20);
        totalItemsTransferred = itemsSent.values().stream().mapToInt(Integer::intValue).sum() + 
                              itemsReceived.values().stream().mapToInt(Integer::intValue).sum();
        
        firstMailDate = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000); // 30 days ago
        lastMailDate = System.currentTimeMillis() - (random.nextInt(24) * 60 * 60 * 1000); // Random hours ago
    }

    private void updateLoadingDisplay() {
        inventory.clear();
        UITheme theme = themeManager.getPlayerTheme(player);
        
        // Create border
        createBorder(theme);
        
        // Loading indicator
        ItemStack loading = UIUtils.createLoadingItem(0);
        inventory.setItem(22, loading);
        
        // Info item
        ItemStack info = UIUtils.createItem(
            Material.BOOK,
            theme.getPrimaryColor() + "§l📊 Statistics Loading",
            theme.getTextColor() + "Analyzing your mail data...",
            "",
            theme.getSubtitleColor() + "Please wait while we gather your statistics."
        );
        inventory.setItem(4, info);
    }

    private void updateInventory() {
        inventory.clear();
        UITheme theme = themeManager.getPlayerTheme(player);
        
        // Create border
        createBorder(theme);
        
        // Statistics displays
        createOverview(theme);
        createDetailedStats(theme);
        createTopLists(theme);
        createControlButtons(theme);
    }

    private void createBorder(UITheme theme) {
        ItemStack border = UIUtils.createBorder(theme.getBorderMaterial(), " ");
        
        // Top and bottom borders
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(i + 45, border);
        }
        
        // Side borders
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }
    }

    private void createOverview(UITheme theme) {
        long daysActive = (System.currentTimeMillis() - firstMailDate) / (24 * 60 * 60 * 1000L);
        double avgMailPerDay = daysActive > 0 ? (double) totalMailReceived / daysActive : 0;
        
        ItemStack overview = UIUtils.createItem(
            Material.KNOWLEDGE_BOOK,
            theme.getPrimaryColor() + "§l📈 Mail Overview",
            "",
            theme.getTextColor() + "Total Mail Sent: " + theme.getAccentColor() + totalMailSent,
            theme.getTextColor() + "Total Mail Received: " + theme.getAccentColor() + totalMailReceived,
            theme.getTextColor() + "Total Items Transferred: " + theme.getAccentColor() + totalItemsTransferred,
            "",
            theme.getTextColor() + "Days Active: " + theme.getAccentColor() + daysActive,
            theme.getTextColor() + "Average Mail/Day: " + theme.getAccentColor() + String.format("%.1f", avgMailPerDay),
            "",
            theme.getSubtitleColor() + "First Mail: " + formatDate(firstMailDate),
            theme.getSubtitleColor() + "Last Activity: " + UIUtils.formatDuration(System.currentTimeMillis() - lastMailDate) + " ago",
            "",
            theme.getAccentColor() + "Click sections below for detailed statistics!"
        );
        inventory.setItem(OVERVIEW_SLOT, overview);
    }

    private void createDetailedStats(UITheme theme) {
        // Sent mail statistics
        ItemStack sentStats = UIUtils.createItem(
            Material.PAPER,
            theme.getSuccessColor() + "§l📮 Mail Sent Statistics",
            "",
            theme.getTextColor() + "Total Sent: " + theme.getAccentColor() + totalMailSent,
            theme.getTextColor() + "Success Rate: " + theme.getSuccessColor() + "98.5%",
            theme.getTextColor() + "Avg Items/Mail: " + theme.getAccentColor() + 
                String.format("%.1f", (double) totalItemsTransferred / Math.max(1, totalMailSent * 2)),
            "",
            theme.getSubtitleColor() + "Most Active Server:",
            getTopServer(theme),
            "",
            theme.getSuccessColor() + "▶ Click for detailed breakdown"
        );
        inventory.setItem(SENT_STATS_SLOT, sentStats);
        
        // Received mail statistics
        ItemStack receivedStats = UIUtils.createItem(
            Material.WRITTEN_BOOK,
            theme.getAccentColor() + "§l📬 Mail Received Statistics",
            "",
            theme.getTextColor() + "Total Received: " + theme.getAccentColor() + totalMailReceived,
            theme.getTextColor() + "Collection Rate: " + theme.getSuccessColor() + "96.2%",
            theme.getTextColor() + "Avg Response Time: " + theme.getAccentColor() + "2.3 hours",
            "",
            theme.getSubtitleColor() + "Top Sender:",
            getTopSender(theme),
            "",
            theme.getSuccessColor() + "▶ Click for detailed breakdown"
        );
        inventory.setItem(RECEIVED_STATS_SLOT, receivedStats);
        
        // Server statistics
        ItemStack serverStats = UIUtils.createItem(
            Material.COMPASS,
            theme.getWarningColor() + "§l🌐 Server Distribution",
            "",
            theme.getTextColor() + "Active Servers: " + theme.getAccentColor() + mailByServer.size(),
            "",
            theme.getSubtitleColor() + "Server Breakdown:"
        );
        
        List<String> serverLore = new ArrayList<>(Arrays.asList(
            "",
            theme.getSubtitleColor() + "Server Breakdown:"
        ));
        
        mailByServer.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(3)
            .forEach(entry -> {
                String progressBar = UIUtils.createProgressBar(entry.getValue(), 
                    Collections.max(mailByServer.values()), 10, "█", "░", theme.getSuccessColor());
                serverLore.add(theme.getTextColor() + entry.getKey() + ": " + 
                    theme.getAccentColor() + entry.getValue() + " " + progressBar);
            });
        
        serverLore.add("");
        serverLore.add(theme.getSuccessColor() + "▶ Click for full server analysis");
        
        serverStats = UIUtils.createItem(Material.COMPASS, 
            theme.getWarningColor() + "§l🌐 Server Distribution", serverLore);
        inventory.setItem(SERVER_STATS_SLOT, serverStats);
        
        // Item statistics
        ItemStack itemStats = UIUtils.createItem(
            Material.CHEST,
            theme.getErrorColor() + "§l📦 Item Transfer Analysis",
            "",
            theme.getTextColor() + "Unique Items Sent: " + theme.getAccentColor() + itemsSent.size(),
            theme.getTextColor() + "Unique Items Received: " + theme.getAccentColor() + itemsReceived.size(),
            theme.getTextColor() + "Total Transfers: " + theme.getAccentColor() + totalItemsTransferred,
            "",
            theme.getSubtitleColor() + "Most Sent Item:",
            getTopSentItem(theme),
            theme.getSubtitleColor() + "Most Received Item:",
            getTopReceivedItem(theme),
            "",
            theme.getSuccessColor() + "▶ Click for item breakdown"
        );
        inventory.setItem(ITEM_STATS_SLOT, itemStats);
    }

    private void createTopLists(UITheme theme) {
        // Top senders
        ItemStack topSenders = UIUtils.createItem(
            Material.PLAYER_HEAD,
            theme.getAccentColor() + "§l👥 Top Mail Contacts",
            ""
        );
        
        List<String> senderLore = new ArrayList<>();
        senderLore.add("");
        senderLore.add(theme.getSubtitleColor() + "Most Active Senders:");
        
        mailBySender.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> {
                senderLore.add(theme.getTextColor() + "• " + theme.getAccentColor() + entry.getKey() + 
                    theme.getSubtitleColor() + " (" + entry.getValue() + " mails)");
            });
        
        senderLore.add("");
        senderLore.add(theme.getSuccessColor() + "▶ Click for contact management");
        
        topSenders = UIUtils.createItem(Material.PLAYER_HEAD, 
            theme.getAccentColor() + "§l👥 Top Mail Contacts", senderLore);
        inventory.setItem(TOP_SENDERS_SLOT, topSenders);
        
        // Top items
        ItemStack topItems = UIUtils.createItem(
            Material.DIAMOND,
            theme.getAccentColor() + "§l💎 Popular Items",
            ""
        );
        
        List<String> itemLore = new ArrayList<>();
        itemLore.add("");
        itemLore.add(theme.getSubtitleColor() + "Most Transferred Items:");
        
        Map<String, Integer> combinedItems = new HashMap<>();
        itemsSent.forEach((item, count) -> 
            combinedItems.merge(item, count, Integer::sum));
        itemsReceived.forEach((item, count) -> 
            combinedItems.merge(item, count, Integer::sum));
        
        combinedItems.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> {
                itemLore.add(theme.getTextColor() + "• " + theme.getAccentColor() + entry.getKey() + 
                    theme.getSubtitleColor() + " (" + entry.getValue() + " transfers)");
            });
        
        itemLore.add("");
        itemLore.add(theme.getSuccessColor() + "▶ Click for item analytics");
        
        topItems = UIUtils.createItem(Material.DIAMOND, 
            theme.getAccentColor() + "§l💎 Popular Items", itemLore);
        inventory.setItem(TOP_ITEMS_SLOT, topItems);
        
        // Activity graph (placeholder)
        ItemStack activityGraph = UIUtils.createItem(
            Material.MAP,
            theme.getAccentColor() + "§l📈 Activity Graph",
            "",
            theme.getSubtitleColor() + "Visual representation of mail activity",
            theme.getSubtitleColor() + "over time (Coming Soon!)",
            "",
            theme.getTextColor() + "Features will include:",
            theme.getSubtitleColor() + "• Daily/Weekly/Monthly charts",
            theme.getSubtitleColor() + "• Peak activity times",
            theme.getSubtitleColor() + "• Trend analysis",
            theme.getSubtitleColor() + "• Export to image",
            "",
            theme.getSuccessColor() + "▶ Click for time-based analytics"
        );
        inventory.setItem(ACTIVITY_GRAPH_SLOT, activityGraph);
        
        // Achievements
        ItemStack achievements = UIUtils.createItem(
            Material.GOLDEN_APPLE,
            theme.getWarningColor() + "§l🏆 Mail Achievements",
            "",
            theme.getSuccessColor() + "✓ First Mail Sent",
            theme.getSuccessColor() + "✓ Mail Veteran (30+ mails)",
            theme.getSuccessColor() + "✓ Item Collector (100+ items)",
            theme.getSubtitleColor() + "🔒 Cross-Server Explorer",
            theme.getSubtitleColor() + "🔒 Mail Master (500+ mails)",
            theme.getSubtitleColor() + "🔒 Social Butterfly (10+ contacts)",
            "",
            theme.getTextColor() + "Progress: " + theme.getAccentColor() + "3/6 " + 
                UIUtils.createProgressBar(3, 6, 8, "█", "░", theme.getSuccessColor()),
            "",
            theme.getSuccessColor() + "▶ Click for achievement details"
        );
        inventory.setItem(ACHIEVEMENTS_SLOT, achievements);
    }

    private void createControlButtons(UITheme theme) {
        // Export button
        ItemStack export = UIUtils.createItem(
            Material.WRITABLE_BOOK,
            theme.getAccentColor() + "§l💾 Export Statistics",
            theme.getSubtitleColor() + "Save your statistics to file",
            "",
            theme.getTextColor() + "Export Options:",
            theme.getSubtitleColor() + "• Text summary report",
            theme.getSubtitleColor() + "• CSV data export",
            theme.getSubtitleColor() + "• JSON format",
            "",
            theme.getSuccessColor() + "▶ Click to export data"
        );
        inventory.setItem(EXPORT_SLOT, export);
        
        // Refresh button
        ItemStack refresh = UIUtils.createItem(
            theme.getRefreshMaterial(),
            theme.getAccentColor() + "§l🔄 Refresh Statistics",
            theme.getSubtitleColor() + "Update with latest data",
            "",
            theme.getSuccessColor() + "▶ Click to refresh statistics"
        );
        inventory.setItem(REFRESH_SLOT, refresh);
        
        // Settings button
        ItemStack settings = UIUtils.createItem(
            Material.REDSTONE,
            theme.getAccentColor() + "§l⚙ Statistics Settings",
            theme.getSubtitleColor() + "Configure what data to track",
            "",
            theme.getTextColor() + "Options:",
            theme.getSubtitleColor() + "• Data retention period",
            theme.getSubtitleColor() + "• Privacy settings",
            theme.getSubtitleColor() + "• Tracking preferences",
            "",
            theme.getSuccessColor() + "▶ Click to configure"
        );
        inventory.setItem(SETTINGS_SLOT, settings);
        
        // Back button
        ItemStack back = UIUtils.createItem(
            theme.getCancelMaterial(),
            theme.getErrorColor() + "§l◀ Back to Mailbox",
            theme.getSubtitleColor() + "Return to main mailbox",
            "",
            theme.getErrorColor() + "▶ Click to go back"
        );
        inventory.setItem(BACK_SLOT, back);
        
        // Help button
        ItemStack help = UIUtils.createItem(
            Material.QUESTION_MARK_BANNER_PATTERN,
            theme.getAccentColor() + "§l❓ Statistics Help",
            theme.getSubtitleColor() + "Learn about mail analytics",
            "",
            theme.getTextColor() + "Understanding Your Stats:",
            theme.getSubtitleColor() + "• Overview shows general activity",
            theme.getSubtitleColor() + "• Detailed sections show specifics",
            theme.getSubtitleColor() + "• Progress bars show relative amounts",
            theme.getSubtitleColor() + "• Click sections for more details"
        );
        inventory.setItem(HELP_SLOT, help);
    }

    private String formatDate(long timestamp) {
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, java.time.ZoneOffset.UTC);
        return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    private String getTopServer(UITheme theme) {
        if (mailByServer.isEmpty()) {
            return theme.getSubtitleColor() + "No data available";
        }
        
        Map.Entry<String, Integer> top = mailByServer.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(null);
        
        if (top != null) {
            return theme.getAccentColor() + top.getKey() + theme.getSubtitleColor() + 
                " (" + top.getValue() + " mails)";
        }
        
        return theme.getSubtitleColor() + "No data available";
    }

    private String getTopSender(UITheme theme) {
        if (mailBySender.isEmpty()) {
            return theme.getSubtitleColor() + "No data available";
        }
        
        Map.Entry<String, Integer> top = mailBySender.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(null);
        
        if (top != null) {
            return theme.getAccentColor() + top.getKey() + theme.getSubtitleColor() + 
                " (" + top.getValue() + " mails)";
        }
        
        return theme.getSubtitleColor() + "No data available";
    }

    private String getTopSentItem(UITheme theme) {
        if (itemsSent.isEmpty()) {
            return theme.getSubtitleColor() + "No data available";
        }
        
        Map.Entry<String, Integer> top = itemsSent.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(null);
        
        if (top != null) {
            return theme.getAccentColor() + top.getKey() + theme.getSubtitleColor() + 
                " (" + top.getValue() + " sent)";
        }
        
        return theme.getSubtitleColor() + "No data available";
    }

    private String getTopReceivedItem(UITheme theme) {
        if (itemsReceived.isEmpty()) {
            return theme.getSubtitleColor() + "No data available";
        }
        
        Map.Entry<String, Integer> top = itemsReceived.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(null);
        
        if (top != null) {
            return theme.getAccentColor() + top.getKey() + theme.getSubtitleColor() + 
                " (" + top.getValue() + " received)";
        }
        
        return theme.getSubtitleColor() + "No data available";
    }

    public void open() {
        UITheme theme = themeManager.getPlayerTheme(player);
        themeManager.playSound(player, theme.getOpenSound());
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;

        if (isLoading) return;

        UITheme theme = themeManager.getPlayerTheme(player);
        int slot = event.getSlot();

        switch (slot) {
            case SENT_STATS_SLOT:
            case RECEIVED_STATS_SLOT:
            case SERVER_STATS_SLOT:
            case ITEM_STATS_SLOT:
            case TOP_SENDERS_SLOT:
            case TOP_ITEMS_SLOT:
            case ACTIVITY_GRAPH_SLOT:
            case ACHIEVEMENTS_SLOT:
                themeManager.playSound(player, theme.getClickSound());
                player.sendMessage(theme.getAccentColor() + "📊 Detailed analytics coming in future updates!");
                break;
                
            case EXPORT_SLOT:
                handleExport(theme);
                break;
                
            case REFRESH_SLOT:
                themeManager.playSound(player, theme.getClickSound());
                loadStatistics();
                break;
                
            case SETTINGS_SLOT:
                themeManager.playSound(player, theme.getClickSound());
                player.sendMessage(theme.getAccentColor() + "⚙ Statistics settings coming soon!");
                break;
                
            case BACK_SLOT:
                themeManager.playSound(player, theme.getCloseSound());
                clicker.closeInventory();
                new EnhancedMailboxGUI(plugin, player).open();
                break;
                
            case HELP_SLOT:
                handleHelp(theme);
                break;
        }
    }

    private void handleExport(UITheme theme) {
        themeManager.playSound(player, theme.getClickSound());
        
        player.sendMessage(theme.getAccentColor() + "💾 Exporting statistics...");
        player.sendMessage(theme.getTextColor() + "═══════════════════════════════");
        player.sendMessage(theme.getPrimaryColor() + "MAIL STATISTICS REPORT");
        player.sendMessage(theme.getTextColor() + "Generated: " + formatDate(System.currentTimeMillis()));
        player.sendMessage(theme.getTextColor() + "═══════════════════════════════");
        player.sendMessage(theme.getSuccessColor() + "Total Mail Sent: " + totalMailSent);
        player.sendMessage(theme.getSuccessColor() + "Total Mail Received: " + totalMailReceived);
        player.sendMessage(theme.getSuccessColor() + "Total Items Transferred: " + totalItemsTransferred);
        player.sendMessage(theme.getTextColor() + "═══════════════════════════════");
        player.sendMessage(theme.getSubtitleColor() + "Full export functionality coming soon!");
    }

    private void handleHelp(UITheme theme) {
        themeManager.playSound(player, theme.getClickSound());
        
        player.sendMessage(theme.getAccentColor() + "📊 Statistics Help:");
        player.sendMessage(theme.getTextColor() + "• " + theme.getSuccessColor() + "Overview: " + 
            theme.getSubtitleColor() + "General mail activity summary");
        player.sendMessage(theme.getTextColor() + "• " + theme.getSuccessColor() + "Detailed Stats: " + 
            theme.getSubtitleColor() + "Specific metrics for sent/received mail");
        player.sendMessage(theme.getTextColor() + "• " + theme.getSuccessColor() + "Top Lists: " + 
            theme.getSubtitleColor() + "Rankings of contacts and items");
        player.sendMessage(theme.getTextColor() + "• " + theme.getSuccessColor() + "Progress Bars: " + 
            theme.getSubtitleColor() + "Visual representation of data");
        player.sendMessage(theme.getAccentColor() + "Statistics update automatically as you use mail!");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        
        UITheme theme = themeManager.getPlayerTheme(player);
        themeManager.playSound(player, theme.getCloseSound());
        
        // Unregister this listener to prevent memory leaks
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
    }
}