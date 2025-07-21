package org.anonventions.globalPost.gui;

import org.anonventions.globalPost.GlobalPost;
import org.anonventions.globalPost.models.Mail;
import org.anonventions.globalPost.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MailboxGUI implements Listener {

    private final GlobalPost plugin;
    private final Player player;
    private final Inventory inventory;
    private List<Mail> allMails = new ArrayList<>();
    private PaginationHelper pagination;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    public MailboxGUI(GlobalPost plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        
        String title = plugin.getConfigManager().getMailboxTitle();
        this.inventory = Bukkit.createInventory(null, 54, title);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadMails();
    }

    private void loadMails() {
        plugin.getMailboxManager().getPlayerMails(player.getUniqueId()).thenAccept(mailList -> {
            this.allMails = mailList;
            this.pagination = new PaginationHelper(allMails, plugin.getConfigManager().getContentSlots().size());
            Bukkit.getScheduler().runTask(plugin, this::updateInventory);
        });
    }

    private void updateInventory() {
        inventory.clear();
        
        // Set border items
        setupBorder();
        
        // Add player head
        setupPlayerHead();
        
        // Add navigation items
        setupNavigationItems();
        
        // Add mail items
        setupMailItems();
        
        // Add pagination if needed
        setupPagination();
    }
    
    private void setupBorder() {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            border.setItemMeta(meta);
        }
        
        // Set border items (slots 0-8, 9, 17, 18, 26, 27, 35, 36, 44, 45-53)
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                // Skip specific slots that have special functions
                if (!isSpecialSlot(i)) {
                    inventory.setItem(i, border);
                }
            }
        }
    }
    
    private boolean isSpecialSlot(int slot) {
        List<Integer> contentSlots = plugin.getConfigManager().getContentSlots();
        
        return slot == plugin.getConfigManager().getPlayerHeadSlot() ||
               slot == plugin.getConfigManager().getRefreshSlot() ||
               slot == plugin.getConfigManager().getSendMailSlot() ||
               slot == plugin.getConfigManager().getPreviousPageSlot() ||
               slot == plugin.getConfigManager().getNextPageSlot() ||
               contentSlots.contains(slot);
    }
    
    private void setupPlayerHead() {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
        
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName("§6" + player.getName());
            meta.setLore(List.of("§7Your mailbox", "§7Total mails: §f" + allMails.size()));
            playerHead.setItemMeta(meta);
        }
        
        inventory.setItem(plugin.getConfigManager().getPlayerHeadSlot(), playerHead);
    }
    
    private void setupNavigationItems() {
        // Refresh button
        ItemStack refresh = new ItemStack(Material.COMPASS);
        ItemMeta refreshMeta = refresh.getItemMeta();
        if (refreshMeta != null) {
            refreshMeta.setDisplayName("§bRefresh");
            refreshMeta.setLore(List.of("§7Click to refresh your mailbox"));
            refresh.setItemMeta(refreshMeta);
        }
        inventory.setItem(plugin.getConfigManager().getRefreshSlot(), refresh);
        
        // Send mail button
        ItemStack sendMail = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta sendMeta = sendMail.getItemMeta();
        if (sendMeta != null) {
            sendMeta.setDisplayName("§aSend Mail");
            sendMeta.setLore(List.of("§7Click to send mail to another server"));
            sendMail.setItemMeta(sendMeta);
        }
        inventory.setItem(plugin.getConfigManager().getSendMailSlot(), sendMail);
    }
    
    private void setupMailItems() {
        if (allMails.isEmpty()) {
            // Show "no mail" message
            ItemStack noMail = ItemBuilder.createItem("BARRIER", 0, "§cNo mail", List.of("§7You have no unread mail."));
            inventory.setItem(22, noMail);
            return;
        }
        
        List<Mail> currentPageMails = pagination.getCurrentPageMails();
        List<Integer> contentSlots = plugin.getConfigManager().getContentSlots();
        
        for (int i = 0; i < currentPageMails.size() && i < contentSlots.size(); i++) {
            Mail mail = currentPageMails.get(i);
            ItemStack mailItem = createMailItem(mail);
            inventory.setItem(contentSlots.get(i), mailItem);
        }
    }
    
    private ItemStack createMailItem(Mail mail) {
        String timeStr = mail.getSentAt() != null ? dateFormat.format(mail.getSentAt()) : "Unknown";
        String message = mail.getMessage();
        
        // Get mail item config from config.yml
        String name = ItemBuilder.replacePlaceholders(
            "§6Mail from {sender}", 
            mail.getSenderName(), 
            mail.getSourceServer(), 
            timeStr, 
            mail.getItems().size(), 
            message
        );
        
        List<String> baseLore = List.of(
            "§7From: §f{sender}",
            "§7Server: §f{server}",
            "§7Time: §f{time}",
            "§7Items: §f{items}",
            "§7Message: §f{message}",
            "",
            "§aClick to collect!"
        );
        
        List<String> lore = ItemBuilder.replacePlaceholders(
            baseLore, 
            mail.getSenderName(), 
            mail.getSourceServer(), 
            timeStr, 
            mail.getItems().size(), 
            message
        );
        
        return ItemBuilder.createItem("PAPER", 0, name, lore);
    }
    
    private void setupPagination() {
        if (pagination.getTotalPages() <= 1) {
            return; // No pagination needed
        }
        
        // Previous page button
        if (pagination.hasPreviousPage()) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta meta = prevPage.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§7« Previous Page");
                meta.setLore(List.of("§8Click to go to previous page", "§8Page " + (pagination.getCurrentPage()) + "/" + pagination.getTotalPages()));
                prevPage.setItemMeta(meta);
            }
            inventory.setItem(plugin.getConfigManager().getPreviousPageSlot(), prevPage);
        }
        
        // Next page button
        if (pagination.hasNextPage()) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta meta = nextPage.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§7Next Page »");
                meta.setLore(List.of("§8Click to go to next page", "§8Page " + (pagination.getCurrentPage() + 2) + "/" + pagination.getTotalPages()));
                nextPage.setItemMeta(meta);
            }
            inventory.setItem(plugin.getConfigManager().getNextPageSlot(), nextPage);
        }
        
        // Page info in title bar area
        ItemStack pageInfo = new ItemStack(Material.BOOK);
        ItemMeta meta = pageInfo.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Page " + (pagination.getCurrentPage() + 1));
            meta.setLore(List.of("§7Total pages: §f" + pagination.getTotalPages(), "§7Total mails: §f" + pagination.getTotalMails()));
            pageInfo.setItemMeta(meta);
        }
        inventory.setItem(13, pageInfo); // Top center slot for page info
    }

    public void open() {
        player.openInventory(inventory);
        plugin.getSoundManager().playGuiOpenSound(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();

        if (!clicker.equals(player)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getSlot();

        // Handle refresh
        if (slot == plugin.getConfigManager().getRefreshSlot()) {
            loadMails();
            return;
        }

        // Handle send mail
        if (slot == plugin.getConfigManager().getSendMailSlot()) {
            clicker.closeInventory();
            clicker.sendMessage("§aUse /post send <server> [player] to send mail!");
            return;
        }

        // Handle pagination
        if (slot == plugin.getConfigManager().getPreviousPageSlot() && pagination.hasPreviousPage()) {
            pagination.previousPage();
            updateInventory();
            return;
        }
        
        if (slot == plugin.getConfigManager().getNextPageSlot() && pagination.hasNextPage()) {
            pagination.nextPage();
            updateInventory();
            return;
        }

        // Handle mail collection
        List<Integer> contentSlots = plugin.getConfigManager().getContentSlots();
        int mailIndex = contentSlots.indexOf(slot);
        if (mailIndex != -1) {
            List<Mail> currentPageMails = pagination.getCurrentPageMails();
            if (mailIndex < currentPageMails.size()) {
                Mail mail = currentPageMails.get(mailIndex);
                collectMail(mail, clicker);
            }
        }
    }

    private void collectMail(Mail mail, Player player) {
        // Check if player has enough inventory space
        int requiredSlots = mail.getItems().size();
        int availableSlots = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                availableSlots++;
            }
        }

        if (availableSlots < requiredSlots) {
            player.sendMessage("§cYou don't have enough inventory space to collect this mail!");
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
                    player.sendMessage("§aMail collected from " + mail.getSenderName() + "!");
                    plugin.getSoundManager().playMailReceiveSound(player);
                    loadMails(); // Refresh the GUI
                });
            }
        });
    }
}