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
import java.util.UUID;

/**
 * Enhanced send mail GUI with modern design and advanced features
 */
public class EnhancedSendMailGUI implements Listener {

    private final GlobalPost plugin;
    private final Player player;
    private final String destinationServer;
    private final String recipientName;
    private final ThemeManager themeManager;
    private final Inventory inventory;
    private boolean isProcessing = false;
    private String messageText = "";

    // Layout constants
    private static final int[] MAIL_SLOTS = {
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    
    private static final int INFO_SLOT = 4;
    private static final int RECIPIENT_SLOT = 13;
    private static final int MESSAGE_SLOT = 46;
    private static final int QUICK_FILL_SLOT = 47;
    private static final int CLEAR_SLOT = 48;
    private static final int PREVIEW_SLOT = 49;
    private static final int SEND_SLOT = 51;
    private static final int CANCEL_SLOT = 45;
    private static final int TEMPLATE_SLOT = 52;
    private static final int HELP_SLOT = 53;

    public EnhancedSendMailGUI(GlobalPost plugin, Player player, String destinationServer, String recipientName) {
        this.plugin = plugin;
        this.player = player;
        this.destinationServer = destinationServer;
        this.recipientName = recipientName;
        this.themeManager = plugin.getThemeManager();

        UITheme theme = themeManager.getPlayerTheme(player);
        this.inventory = Bukkit.createInventory(null, 54, 
            theme.getPrimaryColor() + "📮 " + theme.getAccentColor() + "§lSend Mail " + 
            theme.getSubtitleColor() + "→ " + recipientName);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        updateInventory();
    }

    private void updateInventory() {
        inventory.clear();
        UITheme theme = themeManager.getPlayerTheme(player);
        
        // Create border
        createBorder(theme);
        
        // Control buttons
        createControlButtons(theme);
        
        // Info panel
        createInfoPanel(theme);
        
        // Recipient display
        createRecipientDisplay(theme);
        
        // Mail area instructions
        createMailAreaInstructions(theme);
    }

    private void createBorder(UITheme theme) {
        ItemStack border = UIUtils.createBorder(theme.getBorderMaterial(), " ");
        
        // Top border
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
        }
        
        // Bottom border  
        for (int i = 45; i < 54; i++) {
            if (i != MESSAGE_SLOT && i != QUICK_FILL_SLOT && i != CLEAR_SLOT && 
                i != PREVIEW_SLOT && i != SEND_SLOT && i != CANCEL_SLOT && 
                i != TEMPLATE_SLOT && i != HELP_SLOT) {
                inventory.setItem(i, border);
            }
        }
        
        // Side borders
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }
    }

    private void createControlButtons(UITheme theme) {
        // Message button
        ItemStack message = UIUtils.createItem(
            Material.WRITABLE_BOOK,
            theme.getAccentColor() + "§l✍ Add Message",
            theme.getTextColor() + "Current message: " + (messageText.isEmpty() ? 
                theme.getSubtitleColor() + "None" : theme.getAccentColor() + "\"" + messageText + "\""),
            "",
            theme.getSuccessColor() + "▶ Click to add/edit message",
            theme.getSubtitleColor() + "Messages help explain what you're sending"
        );
        inventory.setItem(MESSAGE_SLOT, message);
        
        // Quick fill button
        ItemStack quickFill = UIUtils.createItem(
            Material.HOPPER,
            theme.getAccentColor() + "§l⚡ Quick Fill",
            theme.getSubtitleColor() + "Quickly add items from your inventory",
            "",
            theme.getSuccessColor() + "▶ Left-click: Fill with hotbar items",
            theme.getWarningColor() + "▶ Right-click: Fill with inventory items",
            theme.getSubtitleColor() + "Skips blacklisted items automatically"
        );
        inventory.setItem(QUICK_FILL_SLOT, quickFill);
        
        // Clear button
        ItemStack clear = UIUtils.createItem(
            Material.BARRIER,
            theme.getWarningColor() + "§l🗑 Clear All",
            theme.getSubtitleColor() + "Remove all items from mail slots",
            "",
            theme.getWarningColor() + "▶ Click to clear all items",
            theme.getErrorColor() + "Items will be returned to your inventory"
        );
        inventory.setItem(CLEAR_SLOT, clear);
        
        // Preview button
        ItemStack preview = UIUtils.createItem(
            Material.SPYGLASS,
            theme.getAccentColor() + "§l👁 Preview Mail",
            theme.getSubtitleColor() + "See how your mail will look",
            "",
            theme.getSuccessColor() + "▶ Click to preview before sending"
        );
        inventory.setItem(PREVIEW_SLOT, preview);
        
        // Send button
        int itemCount = getMailItemCount();
        boolean canSend = itemCount > 0 && itemCount <= plugin.getConfigManager().getMaxItemsPerMail();
        
        ItemStack send = UIUtils.createItem(
            canSend ? theme.getSendMaterial() : Material.GRAY_CONCRETE,
            canSend ? theme.getSuccessColor() + "§l📨 Send Mail" : theme.getErrorColor() + "§l📨 Cannot Send",
            theme.getTextColor() + "To: " + theme.getAccentColor() + recipientName,
            theme.getTextColor() + "Server: " + theme.getAccentColor() + destinationServer,
            theme.getTextColor() + "Items: " + theme.getAccentColor() + itemCount + "/" + plugin.getConfigManager().getMaxItemsPerMail(),
            "",
            canSend ? theme.getSuccessColor() + "▶ Click to send your mail!" : 
                      theme.getErrorColor() + "▶ Add items to enable sending"
        );
        
        if (canSend) {
            send = UIUtils.addGlow(send);
        }
        
        inventory.setItem(SEND_SLOT, send);
        
        // Cancel button
        ItemStack cancel = UIUtils.createItem(
            theme.getCancelMaterial(),
            theme.getErrorColor() + "§l✖ Cancel",
            theme.getSubtitleColor() + "Return items and close interface",
            "",
            theme.getErrorColor() + "▶ Click to cancel and go back"
        );
        inventory.setItem(CANCEL_SLOT, cancel);
        
        // Template button (future feature)
        ItemStack template = UIUtils.createItem(
            Material.BOOKSHELF,
            theme.getAccentColor() + "§l📋 Templates",
            theme.getSubtitleColor() + "Quick mail templates (Coming Soon!)",
            "",
            theme.getSubtitleColor() + "▶ Will include common item sets",
            theme.getSubtitleColor() + "▶ Custom template creation",
            theme.getSubtitleColor() + "▶ One-click mail sending"
        );
        inventory.setItem(TEMPLATE_SLOT, template);
        
        // Help button
        ItemStack help = UIUtils.createItem(
            Material.QUESTION_MARK_BANNER_PATTERN,
            theme.getAccentColor() + "§l❓ Help",
            theme.getSubtitleColor() + "Mail sending guide and tips",
            "",
            theme.getTextColor() + "• Place items in the slots above",
            theme.getTextColor() + "• Add a message for context",
            theme.getTextColor() + "• Click send when ready",
            theme.getTextColor() + "• Items will be delivered instantly"
        );
        inventory.setItem(HELP_SLOT, help);
    }

    private void createInfoPanel(UITheme theme) {
        int itemCount = getMailItemCount();
        int maxItems = plugin.getConfigManager().getMaxItemsPerMail();
        
        String progressBar = UIUtils.createProgressBar(itemCount, maxItems, 10, "█", "░", theme.getSuccessColor());
        
        ItemStack info = UIUtils.createItem(
            Material.KNOWLEDGE_BOOK,
            theme.getPrimaryColor() + "§l📊 Mail Composition",
            "",
            theme.getTextColor() + "Items Added: " + theme.getAccentColor() + itemCount + "/" + maxItems,
            theme.getTextColor() + "Progress: " + progressBar + " " + theme.getSubtitleColor() + 
                "(" + Math.round((float)itemCount / maxItems * 100) + "%)",
            theme.getTextColor() + "Message: " + (messageText.isEmpty() ? 
                theme.getErrorColor() + "None" : theme.getSuccessColor() + "Added"),
            "",
            theme.getSubtitleColor() + "Mail Status:",
            itemCount == 0 ? theme.getErrorColor() + "• No items added yet" :
            itemCount > maxItems ? theme.getErrorColor() + "• Too many items!" :
            theme.getSuccessColor() + "• Ready to send!",
            "",
            theme.getAccentColor() + "Add items by dragging them into the slots below"
        );
        inventory.setItem(INFO_SLOT, info);
    }

    private void createRecipientDisplay(UITheme theme) {
        // Create a player head for the recipient
        ItemStack recipientHead = UIUtils.createPlayerSkull(
            recipientName,
            theme.getPrimaryColor() + "§l👤 Recipient: " + theme.getAccentColor() + recipientName,
            theme.getTextColor() + "Server: " + theme.getAccentColor() + destinationServer,
            "",
            theme.getSubtitleColor() + "This mail will be delivered to:",
            theme.getAccentColor() + recipientName + theme.getSubtitleColor() + " on " + 
                theme.getAccentColor() + destinationServer,
            "",
            theme.getTextColor() + "Delivery Method: " + theme.getSuccessColor() + "Cross-Server",
            theme.getTextColor() + "Delivery Time: " + theme.getSuccessColor() + "Instant",
            theme.getTextColor() + "Notification: " + theme.getSuccessColor() + "On Join/Command"
        );
        inventory.setItem(RECIPIENT_SLOT, recipientHead);
    }

    private void createMailAreaInstructions(UITheme theme) {
        // Add instructional items in empty mail slots
        if (getMailItemCount() == 0) {
            ItemStack instruction1 = UIUtils.createItem(
                Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                theme.getSubtitleColor() + "Mail Slot",
                theme.getAccentColor() + "▶ Drag items here to add to mail",
                theme.getSubtitleColor() + "Items will be sent to " + recipientName
            );
            
            ItemStack instruction2 = UIUtils.createItem(
                Material.LIME_STAINED_GLASS_PANE,
                theme.getSuccessColor() + "Quick Tip",
                theme.getAccentColor() + "▶ Use Quick Fill for faster setup",
                theme.getSubtitleColor() + "Shift-click items to move them quickly"
            );
            
            ItemStack instruction3 = UIUtils.createItem(
                Material.YELLOW_STAINED_GLASS_PANE,
                theme.getWarningColor() + "Remember",
                theme.getAccentColor() + "▶ Some items cannot be mailed",
                theme.getSubtitleColor() + "Blacklisted items will be rejected"
            );
            
            // Place instructions in a few strategic slots
            inventory.setItem(MAIL_SLOTS[3], instruction1);
            inventory.setItem(MAIL_SLOTS[10], instruction2);
            inventory.setItem(MAIL_SLOTS[17], instruction3);
        }
    }

    private int getMailItemCount() {
        int count = 0;
        for (int slot : MAIL_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR && 
                !item.getType().name().contains("GLASS_PANE")) {
                count++;
            }
        }
        return count;
    }

    private List<ItemStack> getMailItems() {
        List<ItemStack> items = new ArrayList<>();
        for (int slot : MAIL_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR && 
                !item.getType().name().contains("GLASS_PANE")) {
                items.add(item.clone());
            }
        }
        return items;
    }

    public void open() {
        UITheme theme = themeManager.getPlayerTheme(player);
        themeManager.playSound(player, theme.getOpenSound());
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;

        UITheme theme = themeManager.getPlayerTheme(player);
        int slot = event.getSlot();

        // Allow item placement in mail slots
        for (int mailSlot : MAIL_SLOTS) {
            if (slot == mailSlot) {
                ItemStack clicked = event.getCurrentItem();
                
                // Handle shift click from player inventory
                if (event.isShiftClick() && event.getClickedInventory() == player.getInventory()) {
                    ItemStack item = event.getCurrentItem();
                    if (item != null && !plugin.getBlacklistManager().isBlacklisted(item)) {
                        // Find empty slot in mail area
                        for (int i : MAIL_SLOTS) {
                            ItemStack slotItem = inventory.getItem(i);
                            if (slotItem == null || slotItem.getType() == Material.AIR || 
                                slotItem.getType().name().contains("GLASS_PANE")) {
                                inventory.setItem(i, item.clone());
                                item.setAmount(0);
                                updateControlButtons(theme);
                                break;
                            }
                        }
                    } else if (item != null) {
                        player.sendMessage(theme.getErrorColor() + "✗ You cannot send " + 
                            item.getType().name().replace("_", " ").toLowerCase() + " through mail!");
                        themeManager.playSound(player, theme.getErrorSound());
                    }
                    event.setCancelled(true);
                    return;
                }
                
                // Check for blacklisted items on normal clicks
                if (clicked != null && plugin.getBlacklistManager().isBlacklisted(clicked)) {
                    event.setCancelled(true);
                    player.sendMessage(theme.getErrorColor() + "✗ You cannot send " + 
                        clicked.getType().name().replace("_", " ").toLowerCase() + " through mail!");
                    themeManager.playSound(player, theme.getErrorSound());
                    return;
                }
                
                // Update GUI after item changes
                Bukkit.getScheduler().runTaskLater(plugin, () -> updateControlButtons(theme), 1L);
                return; // Allow normal item placement
            }
        }

        event.setCancelled(true);

        // Handle control buttons
        switch (slot) {
            case MESSAGE_SLOT:
                handleMessageClick(theme);
                break;
                
            case QUICK_FILL_SLOT:
                handleQuickFill(event.isLeftClick(), theme);
                break;
                
            case CLEAR_SLOT:
                handleClear(theme);
                break;
                
            case PREVIEW_SLOT:
                handlePreview(theme);
                break;
                
            case SEND_SLOT:
                handleSend(theme);
                break;
                
            case CANCEL_SLOT:
                handleCancel(theme);
                break;
                
            case TEMPLATE_SLOT:
                themeManager.playSound(player, theme.getClickSound());
                player.sendMessage(theme.getAccentColor() + "📋 Mail templates coming in a future update!");
                break;
                
            case HELP_SLOT:
                handleHelp(theme);
                break;
        }
    }

    private void updateControlButtons(UITheme theme) {
        createInfoPanel(theme);
        
        // Update send button
        int itemCount = getMailItemCount();
        boolean canSend = itemCount > 0 && itemCount <= plugin.getConfigManager().getMaxItemsPerMail();
        
        ItemStack send = UIUtils.createItem(
            canSend ? theme.getSendMaterial() : Material.GRAY_CONCRETE,
            canSend ? theme.getSuccessColor() + "§l📨 Send Mail" : theme.getErrorColor() + "§l📨 Cannot Send",
            theme.getTextColor() + "To: " + theme.getAccentColor() + recipientName,
            theme.getTextColor() + "Server: " + theme.getAccentColor() + destinationServer,
            theme.getTextColor() + "Items: " + theme.getAccentColor() + itemCount + "/" + plugin.getConfigManager().getMaxItemsPerMail(),
            "",
            canSend ? theme.getSuccessColor() + "▶ Click to send your mail!" : 
                      theme.getErrorColor() + "▶ Add items to enable sending"
        );
        
        if (canSend) {
            send = UIUtils.addGlow(send);
        }
        
        inventory.setItem(SEND_SLOT, send);
        
        // Clear instruction items when items are added
        if (itemCount > 0) {
            for (int slot : MAIL_SLOTS) {
                ItemStack item = inventory.getItem(slot);
                if (item != null && item.getType().name().contains("GLASS_PANE")) {
                    inventory.setItem(slot, null);
                }
            }
        } else {
            createMailAreaInstructions(theme);
        }
    }

    private void handleMessageClick(UITheme theme) {
        themeManager.playSound(player, theme.getClickSound());
        player.closeInventory();
        player.sendMessage(theme.getAccentColor() + "✍ Message feature coming soon!");
        player.sendMessage(theme.getSubtitleColor() + "For now, you can include a note by renaming an item.");
        // Could implement chat-based message input here
    }

    private void handleQuickFill(boolean isLeftClick, UITheme theme) {
        themeManager.playSound(player, theme.getClickSound());
        
        int added = 0;
        int availableSlots = 0;
        
        // Count available mail slots
        for (int slot : MAIL_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType() == Material.AIR || 
                item.getType().name().contains("GLASS_PANE")) {
                availableSlots++;
            }
        }
        
        if (availableSlots == 0) {
            player.sendMessage(theme.getErrorColor() + "✗ Mail is already full!");
            themeManager.playSound(player, theme.getErrorSound());
            return;
        }
        
        // Get items from player inventory
        ItemStack[] sourceItems = isLeftClick ? 
            java.util.Arrays.copyOfRange(player.getInventory().getContents(), 0, 9) : // Hotbar
            player.getInventory().getContents(); // Full inventory
        
        int slotIndex = 0;
        for (ItemStack item : sourceItems) {
            if (item != null && item.getType() != Material.AIR && 
                !plugin.getBlacklistManager().isBlacklisted(item) && added < availableSlots) {
                
                // Find next available mail slot
                while (slotIndex < MAIL_SLOTS.length) {
                    ItemStack slotItem = inventory.getItem(MAIL_SLOTS[slotIndex]);
                    if (slotItem == null || slotItem.getType() == Material.AIR || 
                        slotItem.getType().name().contains("GLASS_PANE")) {
                        inventory.setItem(MAIL_SLOTS[slotIndex], item.clone());
                        item.setAmount(0);
                        added++;
                        slotIndex++;
                        break;
                    }
                    slotIndex++;
                }
            }
        }
        
        if (added > 0) {
            player.sendMessage(theme.getSuccessColor() + "✓ Added " + added + " items to mail!");
            updateControlButtons(theme);
        } else {
            player.sendMessage(theme.getErrorColor() + "✗ No suitable items found to add!");
            themeManager.playSound(player, theme.getErrorSound());
        }
    }

    private void handleClear(UITheme theme) {
        themeManager.playSound(player, theme.getClickSound());
        
        int cleared = 0;
        for (int slot : MAIL_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR && 
                !item.getType().name().contains("GLASS_PANE")) {
                // Return item to player inventory
                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(item);
                } else {
                    player.getWorld().dropItem(player.getLocation(), item);
                }
                inventory.setItem(slot, null);
                cleared++;
            }
        }
        
        if (cleared > 0) {
            player.sendMessage(theme.getSuccessColor() + "✓ Cleared " + cleared + " items from mail!");
            updateControlButtons(theme);
        } else {
            player.sendMessage(theme.getSubtitleColor() + "No items to clear.");
        }
    }

    private void handlePreview(UITheme theme) {
        themeManager.playSound(player, theme.getClickSound());
        
        List<ItemStack> items = getMailItems();
        if (items.isEmpty()) {
            player.sendMessage(theme.getErrorColor() + "✗ No items to preview!");
            return;
        }
        
        player.sendMessage(theme.getAccentColor() + "📋 Mail Preview:");
        player.sendMessage(theme.getTextColor() + "To: " + theme.getAccentColor() + recipientName + 
            theme.getTextColor() + " on " + theme.getAccentColor() + destinationServer);
        player.sendMessage(theme.getTextColor() + "Items (" + items.size() + "):");
        
        for (ItemStack item : items) {
            String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() 
                ? item.getItemMeta().getDisplayName()
                : item.getType().name().replace("_", " ").toLowerCase();
            player.sendMessage(theme.getSubtitleColor() + "  • " + theme.getAccentColor() + 
                item.getAmount() + "x " + itemName);
        }
    }

    private void handleSend(UITheme theme) {
        if (isProcessing) return;
        
        List<ItemStack> items = getMailItems();
        if (items.isEmpty()) {
            player.sendMessage(theme.getErrorColor() + "✗ You need to add items to send mail!");
            themeManager.playSound(player, theme.getErrorSound());
            return;
        }

        if (items.size() > plugin.getConfigManager().getMaxItemsPerMail()) {
            player.sendMessage(theme.getErrorColor() + "✗ Too many items! Maximum: " + 
                plugin.getConfigManager().getMaxItemsPerMail());
            themeManager.playSound(player, theme.getErrorSound());
            return;
        }

        isProcessing = true;
        themeManager.playSound(player, theme.getClickSound());
        player.sendMessage(theme.getWarningColor() + "📨 Sending mail...");

        // Send the mail asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                UUID recipientUUID = getRecipientUUID(recipientName);

                boolean success = plugin.getMailboxManager().sendMailDirect(
                        player.getUniqueId(),
                        player.getName(),
                        recipientUUID,
                        recipientName,
                        destinationServer,
                        items,
                        messageText.isEmpty() ? null : messageText
                );

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (success) {
                        player.sendMessage(theme.getSuccessColor() + "✓ Mail sent successfully to " + 
                            theme.getAccentColor() + recipientName + theme.getSuccessColor() + "!");
                        player.sendMessage(theme.getSubtitleColor() + "Delivered " + items.size() + 
                            " items to " + destinationServer);
                        themeManager.playSound(player, theme.getSuccessSound());
                        
                        // Clear the mail slots
                        for (int slot : MAIL_SLOTS) {
                            inventory.setItem(slot, null);
                        }
                        
                        player.closeInventory();
                    } else {
                        player.sendMessage(theme.getErrorColor() + "✗ Failed to send mail! Please try again.");
                        themeManager.playSound(player, theme.getErrorSound());
                        isProcessing = false;
                    }
                });

            } catch (Exception e) {
                plugin.getLogger().severe("Error sending mail: " + e.getMessage());
                e.printStackTrace();

                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(theme.getErrorColor() + "✗ An error occurred while sending mail!");
                    themeManager.playSound(player, theme.getErrorSound());
                    isProcessing = false;
                });
            }
        });
    }

    private void handleCancel(UITheme theme) {
        themeManager.playSound(player, theme.getCloseSound());
        returnItems();
        player.closeInventory();
    }

    private void handleHelp(UITheme theme) {
        themeManager.playSound(player, theme.getClickSound());
        
        player.sendMessage(theme.getAccentColor() + "📘 Mail Sending Help:");
        player.sendMessage(theme.getTextColor() + "1. " + theme.getSuccessColor() + "Drag items" + 
            theme.getTextColor() + " into the slots above");
        player.sendMessage(theme.getTextColor() + "2. " + theme.getSuccessColor() + "Use Quick Fill" + 
            theme.getTextColor() + " for faster setup");
        player.sendMessage(theme.getTextColor() + "3. " + theme.getSuccessColor() + "Add a message" + 
            theme.getTextColor() + " for context (optional)");
        player.sendMessage(theme.getTextColor() + "4. " + theme.getSuccessColor() + "Click Send" + 
            theme.getTextColor() + " when ready");
        player.sendMessage(theme.getSubtitleColor() + "Items will be delivered instantly when the recipient logs in!");
    }

    private UUID getRecipientUUID(String playerName) {
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        try {
            return Bukkit.getOfflinePlayer(playerName).getUniqueId();
        } catch (Exception e) {
            plugin.getLogger().warning("Could not get UUID for player: " + playerName);
            return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes());
        }
    }

    private void returnItems() {
        for (int slot : MAIL_SLOTS) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR && 
                !item.getType().name().contains("GLASS_PANE")) {
                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(item);
                } else {
                    player.getWorld().dropItem(player.getLocation(), item);
                    player.sendMessage("§eDropped " + item.getType().name() + " because your inventory is full!");
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getPlayer() instanceof Player)) return;

        Player closer = (Player) event.getPlayer();
        if (!closer.equals(player)) return;

        UITheme theme = themeManager.getPlayerTheme(player);
        themeManager.playSound(player, theme.getCloseSound());

        // Return items to player when closing without sending
        if (!isProcessing) {
            returnItems();
        }
        
        // Unregister this listener to prevent memory leaks
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
    }
}