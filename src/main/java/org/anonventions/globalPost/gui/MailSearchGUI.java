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

import java.util.ArrayList;
import java.util.List;

/**
 * Advanced search and filter GUI for the mailbox
 */
public class MailSearchGUI implements Listener {

    private final GlobalPost plugin;
    private final Player player;
    private final ThemeManager themeManager;
    private final Inventory inventory;
    private final EnhancedMailboxGUI parentGUI;
    
    // Search criteria
    private String searchTerm = "";
    private String serverFilter = "";
    private boolean showReadMails = false;
    private boolean showUnreadMails = true;
    private boolean showItemsOnly = false;
    private int minItems = 0;
    private int maxItems = 64;
    private long timeRangeStart = 0;
    private long timeRangeEnd = System.currentTimeMillis();

    // Layout constants
    private static final int SEARCH_TERM_SLOT = 10;
    private static final int SERVER_FILTER_SLOT = 11;
    private static final int READ_FILTER_SLOT = 12;
    private static final int UNREAD_FILTER_SLOT = 13;
    private static final int ITEMS_FILTER_SLOT = 14;
    private static final int ITEM_COUNT_SLOT = 15;
    private static final int TIME_RANGE_SLOT = 16;
    private static final int CLEAR_ALL_SLOT = 19;
    private static final int APPLY_FILTERS_SLOT = 25;
    private static final int BACK_SLOT = 45;
    private static final int HELP_SLOT = 49;
    private static final int SAVED_SEARCHES_SLOT = 53;

    public MailSearchGUI(GlobalPost plugin, Player player, EnhancedMailboxGUI parentGUI) {
        this.plugin = plugin;
        this.player = player;
        this.themeManager = plugin.getThemeManager();
        this.parentGUI = parentGUI;

        UITheme theme = themeManager.getPlayerTheme(player);
        this.inventory = Bukkit.createInventory(null, 54, 
            theme.getPrimaryColor() + "🔍 " + theme.getAccentColor() + "§lAdvanced Search & Filters");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        updateInventory();
    }

    private void updateInventory() {
        inventory.clear();
        UITheme theme = themeManager.getPlayerTheme(player);
        
        // Create border
        createBorder(theme);
        
        // Search options
        createSearchOptions(theme);
        
        // Filter options
        createFilterOptions(theme);
        
        // Control buttons
        createControlButtons(theme);
        
        // Info panel
        createInfoPanel(theme);
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

    private void createSearchOptions(UITheme theme) {
        // Search term
        ItemStack searchTerm = UIUtils.createItem(
            Material.SPYGLASS,
            theme.getAccentColor() + "§l🔍 Search Term",
            theme.getTextColor() + "Current: " + (this.searchTerm.isEmpty() ? 
                theme.getSubtitleColor() + "None" : theme.getAccentColor() + "\"" + this.searchTerm + "\""),
            "",
            theme.getSubtitleColor() + "Searches in:",
            theme.getSubtitleColor() + "• Sender names",
            theme.getSubtitleColor() + "• Server names",
            theme.getSubtitleColor() + "• Message content",
            "",
            theme.getSuccessColor() + "▶ Click to set search term"
        );
        inventory.setItem(SEARCH_TERM_SLOT, searchTerm);
        
        // Server filter
        ItemStack serverFilter = UIUtils.createItem(
            Material.COMPASS,
            theme.getAccentColor() + "§l🌐 Server Filter",
            theme.getTextColor() + "Current: " + (this.serverFilter.isEmpty() ? 
                theme.getSubtitleColor() + "All Servers" : theme.getAccentColor() + this.serverFilter),
            "",
            theme.getSubtitleColor() + "Filter mail by source server",
            "",
            theme.getSuccessColor() + "▶ Click to select server"
        );
        inventory.setItem(SERVER_FILTER_SLOT, serverFilter);
    }

    private void createFilterOptions(UITheme theme) {
        // Read mail filter
        ItemStack readFilter = UIUtils.createItem(
            showReadMails ? Material.LIME_CONCRETE : Material.GRAY_CONCRETE,
            (showReadMails ? theme.getSuccessColor() : theme.getSubtitleColor()) + "§l📖 Read Mail",
            theme.getTextColor() + "Show read mail: " + (showReadMails ? 
                theme.getSuccessColor() + "Yes" : theme.getErrorColor() + "No"),
            "",
            theme.getSuccessColor() + "▶ Click to toggle"
        );
        if (showReadMails) {
            readFilter = UIUtils.addGlow(readFilter);
        }
        inventory.setItem(READ_FILTER_SLOT, readFilter);
        
        // Unread mail filter
        ItemStack unreadFilter = UIUtils.createItem(
            showUnreadMails ? Material.LIME_CONCRETE : Material.GRAY_CONCRETE,
            (showUnreadMails ? theme.getSuccessColor() : theme.getSubtitleColor()) + "§l📩 Unread Mail",
            theme.getTextColor() + "Show unread mail: " + (showUnreadMails ? 
                theme.getSuccessColor() + "Yes" : theme.getErrorColor() + "No"),
            "",
            theme.getSuccessColor() + "▶ Click to toggle"
        );
        if (showUnreadMails) {
            unreadFilter = UIUtils.addGlow(unreadFilter);
        }
        inventory.setItem(UNREAD_FILTER_SLOT, unreadFilter);
        
        // Items only filter
        ItemStack itemsFilter = UIUtils.createItem(
            showItemsOnly ? Material.LIME_CONCRETE : Material.GRAY_CONCRETE,
            (showItemsOnly ? theme.getSuccessColor() : theme.getSubtitleColor()) + "§l📦 Items Only",
            theme.getTextColor() + "Show mail with items only: " + (showItemsOnly ? 
                theme.getSuccessColor() + "Yes" : theme.getErrorColor() + "No"),
            "",
            theme.getSubtitleColor() + "Hide empty mail messages",
            "",
            theme.getSuccessColor() + "▶ Click to toggle"
        );
        if (showItemsOnly) {
            itemsFilter = UIUtils.addGlow(itemsFilter);
        }
        inventory.setItem(ITEMS_FILTER_SLOT, itemsFilter);
        
        // Item count filter
        ItemStack itemCount = UIUtils.createItem(
            Material.HOPPER,
            theme.getAccentColor() + "§l📊 Item Count Range",
            theme.getTextColor() + "Min items: " + theme.getAccentColor() + minItems,
            theme.getTextColor() + "Max items: " + theme.getAccentColor() + maxItems,
            "",
            theme.getSubtitleColor() + "Filter by number of items in mail",
            "",
            theme.getSuccessColor() + "▶ Left-click to increase minimum",
            theme.getWarningColor() + "▶ Right-click to decrease minimum",
            theme.getAccentColor() + "▶ Shift+click to adjust maximum"
        );
        inventory.setItem(ITEM_COUNT_SLOT, itemCount);
        
        // Time range filter
        ItemStack timeRange = UIUtils.createItem(
            Material.CLOCK,
            theme.getAccentColor() + "§l🕒 Time Range",
            theme.getTextColor() + "Range: " + theme.getAccentColor() + getTimeRangeText(),
            "",
            theme.getSubtitleColor() + "Filter by when mail was sent",
            "",
            theme.getSuccessColor() + "▶ Click to adjust time range"
        );
        inventory.setItem(TIME_RANGE_SLOT, timeRange);
    }

    private void createControlButtons(UITheme theme) {
        // Clear all filters
        ItemStack clearAll = UIUtils.createItem(
            Material.BARRIER,
            theme.getWarningColor() + "§l🗑 Clear All Filters",
            theme.getSubtitleColor() + "Reset all search criteria to default",
            "",
            theme.getWarningColor() + "▶ Click to clear all filters"
        );
        inventory.setItem(CLEAR_ALL_SLOT, clearAll);
        
        // Apply filters
        boolean hasFilters = !searchTerm.isEmpty() || !serverFilter.isEmpty() || 
                           !showUnreadMails || showReadMails || showItemsOnly || 
                           minItems > 0 || maxItems < 64;
        
        ItemStack applyFilters = UIUtils.createItem(
            hasFilters ? theme.getSendMaterial() : Material.GRAY_CONCRETE,
            hasFilters ? theme.getSuccessColor() + "§l✓ Apply Filters" : 
                        theme.getSubtitleColor() + "§l✓ Apply Filters",
            theme.getTextColor() + "Active filters: " + theme.getAccentColor() + getActiveFilterCount(),
            "",
            hasFilters ? theme.getSuccessColor() + "▶ Click to apply and return to mailbox" :
                        theme.getSubtitleColor() + "▶ No filters to apply"
        );
        
        if (hasFilters) {
            applyFilters = UIUtils.addGlow(applyFilters);
        }
        
        inventory.setItem(APPLY_FILTERS_SLOT, applyFilters);
        
        // Back button
        ItemStack back = UIUtils.createItem(
            theme.getCancelMaterial(),
            theme.getErrorColor() + "§l◀ Back to Mailbox",
            theme.getSubtitleColor() + "Return without applying filters",
            "",
            theme.getErrorColor() + "▶ Click to go back"
        );
        inventory.setItem(BACK_SLOT, back);
        
        // Help button
        ItemStack help = UIUtils.createItem(
            Material.QUESTION_MARK_BANNER_PATTERN,
            theme.getAccentColor() + "§l❓ Search Help",
            theme.getSubtitleColor() + "Learn how to use advanced search",
            "",
            theme.getTextColor() + "Search Tips:",
            theme.getSubtitleColor() + "• Use specific terms for better results",
            theme.getSubtitleColor() + "• Combine multiple filters",
            theme.getSubtitleColor() + "• Time ranges help find old mail",
            theme.getSubtitleColor() + "• Save frequently used searches"
        );
        inventory.setItem(HELP_SLOT, help);
        
        // Saved searches (future feature)
        ItemStack savedSearches = UIUtils.createItem(
            Material.BOOKSHELF,
            theme.getAccentColor() + "§l💾 Saved Searches",
            theme.getSubtitleColor() + "Quick access to saved search criteria",
            "",
            theme.getSubtitleColor() + "Coming Soon:",
            theme.getSubtitleColor() + "• Save custom search combinations",
            theme.getSubtitleColor() + "• Quick filter presets",
            theme.getSubtitleColor() + "• Share searches with friends"
        );
        inventory.setItem(SAVED_SEARCHES_SLOT, savedSearches);
    }

    private void createInfoPanel(UITheme theme) {
        int activeFilters = getActiveFilterCount();
        
        ItemStack info = UIUtils.createItem(
            Material.KNOWLEDGE_BOOK,
            theme.getPrimaryColor() + "§l📋 Search Information",
            "",
            theme.getTextColor() + "Active Filters: " + theme.getAccentColor() + activeFilters,
            theme.getTextColor() + "Search Term: " + (searchTerm.isEmpty() ? 
                theme.getSubtitleColor() + "None" : theme.getAccentColor() + "\"" + searchTerm + "\""),
            theme.getTextColor() + "Server Filter: " + (serverFilter.isEmpty() ? 
                theme.getSubtitleColor() + "All" : theme.getAccentColor() + serverFilter),
            "",
            theme.getSubtitleColor() + "Filters help you find specific mail quickly.",
            theme.getSubtitleColor() + "Use multiple filters for precise results."
        );
        inventory.setItem(4, info);
    }

    private String getTimeRangeText() {
        if (timeRangeStart == 0) {
            return "All Time";
        }
        
        long now = System.currentTimeMillis();
        long dayMs = 24 * 60 * 60 * 1000L;
        
        if (now - timeRangeStart <= dayMs) {
            return "Last 24 hours";
        } else if (now - timeRangeStart <= 7 * dayMs) {
            return "Last week";
        } else if (now - timeRangeStart <= 30 * dayMs) {
            return "Last month";
        } else {
            return "Custom range";
        }
    }

    private int getActiveFilterCount() {
        int count = 0;
        if (!searchTerm.isEmpty()) count++;
        if (!serverFilter.isEmpty()) count++;
        if (!showUnreadMails) count++;
        if (showReadMails) count++;
        if (showItemsOnly) count++;
        if (minItems > 0) count++;
        if (maxItems < 64) count++;
        if (timeRangeStart > 0) count++;
        return count;
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

        UITheme theme = themeManager.getPlayerTheme(player);
        int slot = event.getSlot();

        switch (slot) {
            case SEARCH_TERM_SLOT:
                handleSearchTermClick(theme);
                break;
                
            case SERVER_FILTER_SLOT:
                handleServerFilterClick(theme);
                break;
                
            case READ_FILTER_SLOT:
                showReadMails = !showReadMails;
                themeManager.playSound(player, theme.getClickSound());
                updateInventory();
                break;
                
            case UNREAD_FILTER_SLOT:
                showUnreadMails = !showUnreadMails;
                themeManager.playSound(player, theme.getClickSound());
                updateInventory();
                break;
                
            case ITEMS_FILTER_SLOT:
                showItemsOnly = !showItemsOnly;
                themeManager.playSound(player, theme.getClickSound());
                updateInventory();
                break;
                
            case ITEM_COUNT_SLOT:
                handleItemCountClick(event, theme);
                break;
                
            case TIME_RANGE_SLOT:
                handleTimeRangeClick(theme);
                break;
                
            case CLEAR_ALL_SLOT:
                handleClearAll(theme);
                break;
                
            case APPLY_FILTERS_SLOT:
                handleApplyFilters(theme);
                break;
                
            case BACK_SLOT:
                themeManager.playSound(player, theme.getCloseSound());
                clicker.closeInventory();
                parentGUI.open();
                break;
                
            case HELP_SLOT:
                handleHelp(theme);
                break;
                
            case SAVED_SEARCHES_SLOT:
                themeManager.playSound(player, theme.getClickSound());
                player.sendMessage(theme.getAccentColor() + "💾 Saved searches coming in a future update!");
                break;
        }
    }

    private void handleSearchTermClick(UITheme theme) {
        themeManager.playSound(player, theme.getClickSound());
        player.closeInventory();
        player.sendMessage(theme.getAccentColor() + "✍ Search term input coming soon!");
        player.sendMessage(theme.getSubtitleColor() + "For now, filters work with predefined criteria.");
        // Could implement chat-based search input here
    }

    private void handleServerFilterClick(UITheme theme) {
        themeManager.playSound(player, theme.getClickSound());
        
        // Cycle through available servers
        List<String> servers = new ArrayList<>(plugin.getConfigManager().getAllowedDestinations());
        servers.add(0, ""); // Add "All servers" option
        
        int currentIndex = servers.indexOf(serverFilter);
        int nextIndex = (currentIndex + 1) % servers.size();
        serverFilter = servers.get(nextIndex);
        
        updateInventory();
        
        String filterText = serverFilter.isEmpty() ? "All Servers" : serverFilter;
        player.sendMessage(theme.getAccentColor() + "🌐 Server filter set to: " + 
            theme.getSuccessColor() + filterText);
    }

    private void handleItemCountClick(InventoryClickEvent event, UITheme theme) {
        themeManager.playSound(player, theme.getClickSound());
        
        if (event.isShiftClick()) {
            // Adjust maximum
            if (event.isLeftClick()) {
                maxItems = Math.min(64, maxItems + 5);
            } else if (event.isRightClick()) {
                maxItems = Math.max(minItems + 1, maxItems - 5);
            }
        } else {
            // Adjust minimum
            if (event.isLeftClick()) {
                minItems = Math.min(maxItems - 1, minItems + 1);
            } else if (event.isRightClick()) {
                minItems = Math.max(0, minItems - 1);
            }
        }
        
        updateInventory();
        player.sendMessage(theme.getAccentColor() + "📊 Item count range: " + 
            theme.getSuccessColor() + minItems + "-" + maxItems);
    }

    private void handleTimeRangeClick(UITheme theme) {
        themeManager.playSound(player, theme.getClickSound());
        
        // Cycle through time ranges
        long now = System.currentTimeMillis();
        long dayMs = 24 * 60 * 60 * 1000L;
        
        if (timeRangeStart == 0) {
            timeRangeStart = now - dayMs; // Last 24 hours
        } else if (timeRangeStart == now - dayMs) {
            timeRangeStart = now - (7 * dayMs); // Last week
        } else if (timeRangeStart == now - (7 * dayMs)) {
            timeRangeStart = now - (30 * dayMs); // Last month
        } else {
            timeRangeStart = 0; // All time
        }
        
        updateInventory();
        player.sendMessage(theme.getAccentColor() + "🕒 Time range set to: " + 
            theme.getSuccessColor() + getTimeRangeText());
    }

    private void handleClearAll(UITheme theme) {
        themeManager.playSound(player, theme.getClickSound());
        
        searchTerm = "";
        serverFilter = "";
        showReadMails = false;
        showUnreadMails = true;
        showItemsOnly = false;
        minItems = 0;
        maxItems = 64;
        timeRangeStart = 0;
        
        updateInventory();
        player.sendMessage(theme.getSuccessColor() + "✓ All filters cleared!");
    }

    private void handleApplyFilters(UITheme theme) {
        themeManager.playSound(player, theme.getSuccessSound());
        
        // Apply filters to parent GUI (this would need to be implemented in EnhancedMailboxGUI)
        player.sendMessage(theme.getSuccessColor() + "✓ Filters applied! Returning to mailbox...");
        
        player.closeInventory();
        parentGUI.open();
        
        // TODO: Pass filter criteria to parent GUI
        // For now, just show a message
        player.sendMessage(theme.getSubtitleColor() + "Filter integration coming in next update!");
    }

    private void handleHelp(UITheme theme) {
        themeManager.playSound(player, theme.getClickSound());
        
        player.sendMessage(theme.getAccentColor() + "🔍 Advanced Search Help:");
        player.sendMessage(theme.getTextColor() + "• " + theme.getSuccessColor() + "Search Term: " + 
            theme.getSubtitleColor() + "Find mail by sender, server, or content");
        player.sendMessage(theme.getTextColor() + "• " + theme.getSuccessColor() + "Server Filter: " + 
            theme.getSubtitleColor() + "Show mail from specific servers only");
        player.sendMessage(theme.getTextColor() + "• " + theme.getSuccessColor() + "Read/Unread: " + 
            theme.getSubtitleColor() + "Toggle which mail types to show");
        player.sendMessage(theme.getTextColor() + "• " + theme.getSuccessColor() + "Item Count: " + 
            theme.getSubtitleColor() + "Filter by number of items in mail");
        player.sendMessage(theme.getTextColor() + "• " + theme.getSuccessColor() + "Time Range: " + 
            theme.getSubtitleColor() + "Show mail from specific time periods");
        player.sendMessage(theme.getAccentColor() + "Combine multiple filters for precise results!");
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