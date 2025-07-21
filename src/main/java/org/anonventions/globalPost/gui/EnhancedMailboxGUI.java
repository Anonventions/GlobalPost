package org.anonventions.globalPost.gui;

import org.anonventions.globalPost.GlobalPost;
import org.anonventions.globalPost.models.Mail;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced mailbox GUI with modern design, pagination, themes, and advanced features
 */
public class EnhancedMailboxGUI implements Listener {

    private final GlobalPost plugin;
    private final Player player;
    private final ThemeManager themeManager;
    private final Inventory inventory;
    private List<Mail> allMails;
    private List<Mail> filteredMails;
    private int currentPage;
    private final int itemsPerPage;
    private String searchFilter;
    private boolean showReadMails;
    private SortType sortType;
    private boolean isRefreshing;

    // GUI Layout constants
    private static final int[] MAIL_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    
    private static final int INFO_SLOT = 4;
    private static final int SEARCH_SLOT = 47;
    private static final int FILTER_SLOT = 46;
    private static final int SORT_SLOT = 48;
    private static final int SETTINGS_SLOT = 49;
    private static final int REFRESH_SLOT = 50;
    private static final int SEND_MAIL_SLOT = 51;
    private static final int PREVIOUS_PAGE_SLOT = 45;
    private static final int NEXT_PAGE_SLOT = 53;
    private static final int CLOSE_SLOT = 52;

    public enum SortType {
        NEWEST_FIRST("Newest First", "§7Sort by newest mail first"),
        OLDEST_FIRST("Oldest First", "§7Sort by oldest mail first"),
        SENDER_NAME("Sender Name", "§7Sort by sender name"),
        SERVER_NAME("Server Name", "§7Sort by source server"),
        ITEM_COUNT("Item Count", "§7Sort by number of items");
        
        private final String displayName;
        private final String description;
        
        SortType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    public EnhancedMailboxGUI(GlobalPost plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.themeManager = plugin.getThemeManager();
        this.itemsPerPage = MAIL_SLOTS.length;
        this.currentPage = 0;
        this.searchFilter = "";
        this.showReadMails = false;
        this.sortType = SortType.NEWEST_FIRST;
        this.isRefreshing = false;
        this.allMails = new ArrayList<>();
        this.filteredMails = new ArrayList<>();

        UITheme theme = themeManager.getPlayerTheme(player);
        this.inventory = Bukkit.createInventory(null, 54, 
            theme.getPrimaryColor() + "✉ " + theme.getAccentColor() + "§lMailbox " + 
            theme.getSubtitleColor() + "§o(Enhanced)");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadMails();
    }

    private void loadMails() {
        isRefreshing = true;
        updateLoadingDisplay();
        
        plugin.getMailboxManager().getPlayerMails(player.getUniqueId()).thenAccept(mailList -> {
            this.allMails = mailList != null ? mailList : new ArrayList<>();
            isRefreshing = false;
            Bukkit.getScheduler().runTask(plugin, () -> {
                applyFiltersAndSort();
                updateInventory();
            });
        });
    }

    private void applyFiltersAndSort() {
        filteredMails = allMails.stream()
                .filter(mail -> showReadMails || !mail.isRead())
                .filter(mail -> searchFilter.isEmpty() || 
                    mail.getSenderName().toLowerCase().contains(searchFilter.toLowerCase()) ||
                    mail.getSourceServer().toLowerCase().contains(searchFilter.toLowerCase()))
                .sorted((m1, m2) -> {
                    switch (sortType) {
                        case NEWEST_FIRST:
                            return Long.compare(m2.getTimestamp(), m1.getTimestamp());
                        case OLDEST_FIRST:
                            return Long.compare(m1.getTimestamp(), m2.getTimestamp());
                        case SENDER_NAME:
                            return m1.getSenderName().compareToIgnoreCase(m2.getSenderName());
                        case SERVER_NAME:
                            return m1.getSourceServer().compareToIgnoreCase(m2.getSourceServer());
                        case ITEM_COUNT:
                            return Integer.compare(m2.getItems().size(), m1.getItems().size());
                        default:
                            return 0;
                    }
                })
                .collect(Collectors.toList());
        
        // Reset to first page if current page is out of bounds
        int maxPage = Math.max(0, (filteredMails.size() - 1) / itemsPerPage);
        if (currentPage > maxPage) {
            currentPage = maxPage;
        }
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
            theme.getPrimaryColor() + "§l📧 Mailbox Information",
            theme.getTextColor() + "Loading your mail...",
            "",
            theme.getSubtitleColor() + "Please wait while we fetch your messages."
        );
        inventory.setItem(INFO_SLOT, info);
    }

    private void updateInventory() {
        inventory.clear();
        UITheme theme = themeManager.getPlayerTheme(player);
        
        // Create border
        createBorder(theme);
        
        // Mail display
        if (filteredMails.isEmpty()) {
            displayNoMail(theme);
        } else {
            displayMails(theme);
        }
        
        // Control buttons
        createControlButtons(theme);
        
        // Info panel
        createInfoPanel(theme);
        
        // Navigation
        createNavigationButtons(theme);
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

    private void displayNoMail(UITheme theme) {
        ItemStack noMail = UIUtils.createItem(
            theme.getNoMailMaterial(),
            theme.getErrorColor() + "§l📪 No Mail",
            theme.getSubtitleColor() + "You don't have any mail to display.",
            "",
            theme.getTextColor() + "Filters: " + getActiveFiltersText(),
            "",
            theme.getAccentColor() + "► Send yourself some mail to test!",
            theme.getAccentColor() + "► Try adjusting your filters above."
        );
        inventory.setItem(22, noMail);
    }

    private void displayMails(UITheme theme) {
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, filteredMails.size());
        
        for (int i = 0; i < MAIL_SLOTS.length && (startIndex + i) < endIndex; i++) {
            Mail mail = filteredMails.get(startIndex + i);
            ItemStack mailItem = createMailItem(mail, theme, startIndex + i + 1);
            inventory.setItem(MAIL_SLOTS[i], mailItem);
        }
    }

    private ItemStack createMailItem(Mail mail, UITheme theme, int index) {
        // Use different materials based on mail properties
        Material material;
        if (mail.isRead()) {
            material = Material.MAP; // Read mail
        } else if (mail.getItems().size() > 10) {
            material = theme.getMailMaterial(); // Large mail
        } else {
            material = Material.PAPER; // Normal mail
        }
        
        if (mail.getItems().size() > 15) {
            material = Material.ENCHANTED_BOOK; // Very large mail
        }

        List<String> lore = new ArrayList<>();
        
        // Header
        lore.add(theme.getAccentColor() + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        lore.add(theme.getTextColor() + "From: " + theme.getAccentColor() + mail.getSenderName());
        lore.add(theme.getTextColor() + "Server: " + theme.getAccentColor() + mail.getSourceServer());
        lore.add(theme.getTextColor() + "Items: " + theme.getAccentColor() + mail.getItems().size() + " items");
        
        // Timestamp
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(mail.getTimestamp() / 1000, 0, java.time.ZoneOffset.UTC);
        String timeAgo = UIUtils.formatDuration(System.currentTimeMillis() - mail.getTimestamp());
        lore.add(theme.getTextColor() + "Sent: " + theme.getSubtitleColor() + timeAgo + " ago");
        lore.add(theme.getSubtitleColor() + "(" + dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) + ")");
        
        // Status
        if (mail.isRead()) {
            lore.add(theme.getSubtitleColor() + "Status: §7✓ Read");
        } else {
            lore.add(theme.getSuccessColor() + "Status: ✦ Unread");
        }
        
        // Message if available
        if (mail.getMessage() != null && !mail.getMessage().trim().isEmpty()) {
            lore.add("");
            lore.add(theme.getTextColor() + "Message:");
            List<String> wrappedMessage = UIUtils.wrapText(mail.getMessage(), 30);
            for (String line : wrappedMessage) {
                lore.add(theme.getSubtitleColor() + "  " + line);
            }
        }
        
        // Item preview
        if (!mail.getItems().isEmpty()) {
            lore.add("");
            lore.add(theme.getTextColor() + "Items Preview:");
            int previewCount = Math.min(3, mail.getItems().size());
            for (int i = 0; i < previewCount; i++) {
                ItemStack item = mail.getItems().get(i);
                String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() 
                    ? item.getItemMeta().getDisplayName()
                    : item.getType().name().replace("_", " ").toLowerCase();
                lore.add(theme.getSubtitleColor() + "  • " + theme.getAccentColor() + 
                    item.getAmount() + "x " + itemName);
            }
            if (mail.getItems().size() > previewCount) {
                lore.add(theme.getSubtitleColor() + "  ... and " + 
                    (mail.getItems().size() - previewCount) + " more items");
            }
        }
        
        lore.add("");
        lore.add(theme.getAccentColor() + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        lore.add(theme.getSuccessColor() + "▶ " + theme.getAccentColor() + "Left-click to collect items!");
        lore.add(theme.getWarningColor() + "▶ " + theme.getAccentColor() + "Right-click for mail options");
        
        ItemStack mailItem = UIUtils.createItem(material, 
            theme.getPrimaryColor() + "§l✉ " + theme.getAccentColor() + "Mail #" + index + 
            (!mail.isRead() ? " " + theme.getSuccessColor() + "●" : ""), lore);
        
        // Add glow effect for unread mail
        if (!mail.isRead()) {
            mailItem = UIUtils.addGlow(mailItem);
        }
        
        return mailItem;
    }

    private void createControlButtons(UITheme theme) {
        // Search button
        ItemStack search = UIUtils.createItem(
            Material.SPYGLASS,
            theme.getAccentColor() + "§l🔍 Search & Filter",
            theme.getTextColor() + "Current filter: " + (searchFilter.isEmpty() ? "§7None" : "§f" + searchFilter),
            theme.getTextColor() + "Show read mail: " + (showReadMails ? "§aYes" : "§cNo"),
            "",
            theme.getSuccessColor() + "▶ Left-click to toggle read mail filter",
            theme.getWarningColor() + "▶ Right-click to clear search filter"
        );
        inventory.setItem(SEARCH_SLOT, search);
        
        // Sort button
        ItemStack sort = UIUtils.createItem(
            Material.HOPPER,
            theme.getAccentColor() + "§l⚡ Sort Options",
            theme.getTextColor() + "Current sort: " + theme.getAccentColor() + sortType.getDisplayName(),
            theme.getSubtitleColor() + sortType.getDescription(),
            "",
            theme.getSuccessColor() + "▶ Click to change sort order"
        );
        inventory.setItem(SORT_SLOT, sort);
        
        // Settings/Theme button
        ItemStack settings = UIUtils.createItem(
            Material.REDSTONE,
            theme.getAccentColor() + "§l⚙ Settings & Themes",
            theme.getTextColor() + "Current theme: " + theme.getDisplayName(),
            "",
            theme.getSuccessColor() + "▶ Click to change theme and settings"
        );
        inventory.setItem(SETTINGS_SLOT, settings);
        
        // Refresh button
        ItemStack refresh = UIUtils.createItem(
            theme.getRefreshMaterial(),
            theme.getAccentColor() + "§l🔄 Refresh Mailbox",
            theme.getSubtitleColor() + "Last updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            "",
            theme.getSuccessColor() + "▶ Click to refresh your mailbox"
        );
        inventory.setItem(REFRESH_SLOT, refresh);
        
        // Send Mail button
        ItemStack sendMail = UIUtils.createItem(
            theme.getSendMaterial(),
            theme.getSuccessColor() + "§l📮 Send New Mail",
            theme.getSubtitleColor() + "Send items to other servers",
            "",
            theme.getSuccessColor() + "▶ Click to open send mail interface"
        );
        inventory.setItem(SEND_MAIL_SLOT, sendMail);
    }

    private void createInfoPanel(UITheme theme) {
        int totalMails = allMails.size();
        int unreadMails = (int) allMails.stream().filter(mail -> !mail.isRead()).count();
        int currentDisplayed = Math.min(itemsPerPage, filteredMails.size() - (currentPage * itemsPerPage));
        currentDisplayed = Math.max(0, currentDisplayed);
        
        ItemStack info = UIUtils.createItem(
            Material.KNOWLEDGE_BOOK,
            theme.getPrimaryColor() + "§l📊 Mailbox Statistics",
            "",
            theme.getTextColor() + "Total Mail: " + theme.getAccentColor() + totalMails,
            theme.getTextColor() + "Unread Mail: " + theme.getSuccessColor() + unreadMails,
            theme.getTextColor() + "Filtered Results: " + theme.getAccentColor() + filteredMails.size(),
            theme.getTextColor() + "Currently Showing: " + theme.getAccentColor() + currentDisplayed,
            "",
            theme.getSubtitleColor() + "Page " + (currentPage + 1) + " of " + Math.max(1, (filteredMails.size() - 1) / itemsPerPage + 1),
            "",
            theme.getTextColor() + "Active Filters:",
            theme.getSubtitleColor() + getActiveFiltersText()
        );
        inventory.setItem(INFO_SLOT, info);
    }

    private String getActiveFiltersText() {
        List<String> filters = new ArrayList<>();
        if (!searchFilter.isEmpty()) {
            filters.add("Search: " + searchFilter);
        }
        if (!showReadMails) {
            filters.add("Hide read mail");
        }
        filters.add("Sort: " + sortType.getDisplayName());
        
        return filters.isEmpty() ? "None" : String.join(", ", filters);
    }

    private void createNavigationButtons(UITheme theme) {
        int totalPages = Math.max(1, (filteredMails.size() - 1) / itemsPerPage + 1);
        
        // Previous page
        if (currentPage > 0) {
            ItemStack prevPage = UIUtils.createItem(
                theme.getPreviousPageMaterial(),
                theme.getAccentColor() + "§l◀ Previous Page",
                theme.getSubtitleColor() + "Go to page " + currentPage,
                "",
                theme.getSuccessColor() + "▶ Click to go to previous page"
            );
            inventory.setItem(PREVIOUS_PAGE_SLOT, prevPage);
        } else {
            ItemStack disabled = UIUtils.createItem(
                Material.GRAY_STAINED_GLASS_PANE,
                theme.getSubtitleColor() + "◀ Previous Page",
                theme.getErrorColor() + "No previous page available"
            );
            inventory.setItem(PREVIOUS_PAGE_SLOT, disabled);
        }
        
        // Next page
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = UIUtils.createItem(
                theme.getNextPageMaterial(),
                theme.getAccentColor() + "§l▶ Next Page",
                theme.getSubtitleColor() + "Go to page " + (currentPage + 2),
                "",
                theme.getSuccessColor() + "▶ Click to go to next page"
            );
            inventory.setItem(NEXT_PAGE_SLOT, nextPage);
        } else {
            ItemStack disabled = UIUtils.createItem(
                Material.GRAY_STAINED_GLASS_PANE,
                theme.getSubtitleColor() + "▶ Next Page",
                theme.getErrorColor() + "No next page available"
            );
            inventory.setItem(NEXT_PAGE_SLOT, disabled);
        }
        
        // Close button
        ItemStack close = UIUtils.createItem(
            theme.getCancelMaterial(),
            theme.getErrorColor() + "§l✖ Close Mailbox",
            theme.getSubtitleColor() + "Exit the mailbox interface",
            "",
            theme.getErrorColor() + "▶ Click to close"
        );
        inventory.setItem(CLOSE_SLOT, close);
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

        if (isRefreshing) return;

        UITheme theme = themeManager.getPlayerTheme(player);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getSlot();
        
        // Handle mail item clicks
        for (int i = 0; i < MAIL_SLOTS.length; i++) {
            if (slot == MAIL_SLOTS[i]) {
                int mailIndex = (currentPage * itemsPerPage) + i;
                if (mailIndex < filteredMails.size()) {
                    Mail mail = filteredMails.get(mailIndex);
                    if (event.isLeftClick()) {
                        collectMail(mail, clicker, theme);
                    } else if (event.isRightClick()) {
                        // Could open mail options GUI here
                        clicker.sendMessage(theme.getAccentColor() + "Right-click mail options coming soon!");
                    }
                }
                return;
            }
        }
        
        // Handle control buttons
        switch (slot) {
            case SEARCH_SLOT:
                if (event.isLeftClick()) {
                    showReadMails = !showReadMails;
                    themeManager.playSound(player, theme.getClickSound());
                    applyFiltersAndSort();
                    updateInventory();
                } else if (event.isRightClick()) {
                    searchFilter = "";
                    themeManager.playSound(player, theme.getClickSound());
                    applyFiltersAndSort();
                    updateInventory();
                }
                break;
                
            case SORT_SLOT:
                SortType[] types = SortType.values();
                int currentIndex = java.util.Arrays.asList(types).indexOf(sortType);
                sortType = types[(currentIndex + 1) % types.length];
                themeManager.playSound(player, theme.getClickSound());
                applyFiltersAndSort();
                updateInventory();
                break;
                
            case SETTINGS_SLOT:
                themeManager.playSound(player, theme.getClickSound());
                clicker.closeInventory();
                new ThemeSelectionGUI(plugin, player).open();
                break;
                
            case REFRESH_SLOT:
                themeManager.playSound(player, theme.getClickSound());
                loadMails();
                break;
                
            case SEND_MAIL_SLOT:
                themeManager.playSound(player, theme.getClickSound());
                clicker.closeInventory();
                clicker.sendMessage(theme.getAccentColor() + "Use " + theme.getSuccessColor() + "/post send <server> [player]" + 
                    theme.getAccentColor() + " to send mail!");
                break;
                
            case PREVIOUS_PAGE_SLOT:
                if (currentPage > 0) {
                    currentPage--;
                    themeManager.playSound(player, theme.getClickSound());
                    updateInventory();
                }
                break;
                
            case NEXT_PAGE_SLOT:
                int totalPages = Math.max(1, (filteredMails.size() - 1) / itemsPerPage + 1);
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    themeManager.playSound(player, theme.getClickSound());
                    updateInventory();
                }
                break;
                
            case CLOSE_SLOT:
                themeManager.playSound(player, theme.getCloseSound());
                clicker.closeInventory();
                break;
        }
    }

    private void collectMail(Mail mail, Player player, UITheme theme) {
        // Check if player has enough inventory space
        int requiredSlots = mail.getItems().size();
        int availableSlots = UIUtils.getAvailableSlots(player.getInventory());

        if (availableSlots < requiredSlots) {
            player.sendMessage(theme.getErrorColor() + "✗ You don't have enough inventory space to collect this mail!");
            player.sendMessage(theme.getSubtitleColor() + "Required: " + requiredSlots + " slots, Available: " + availableSlots + " slots");
            themeManager.playSound(player, theme.getErrorSound());
            return;
        }

        // Give items to player
        for (ItemStack item : mail.getItems()) {
            player.getInventory().addItem(item);
        }

        // Mark as collected
        plugin.getMailboxManager().collectMail(mail.getId(), player).thenAccept(success -> {
            if (success) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(theme.getSuccessColor() + "✓ Mail collected from " + 
                        theme.getAccentColor() + mail.getSenderName() + theme.getSuccessColor() + "!");
                    player.sendMessage(theme.getSubtitleColor() + "Received " + mail.getItems().size() + 
                        " items from " + mail.getSourceServer());
                    themeManager.playSound(player, theme.getSuccessSound());
                    loadMails(); // Refresh the GUI
                });
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(theme.getErrorColor() + "✗ Failed to collect mail! Please try again.");
                    themeManager.playSound(player, theme.getErrorSound());
                });
            }
        });
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