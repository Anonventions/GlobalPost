package org.anonventions.globalPost.gui;

import org.anonventions.globalPost.GlobalPost;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SendMailGUI implements Listener {

    private final GlobalPost plugin;
    private final Player player;
    private final String destinationServer;
    private final String recipientName;
    private final Inventory inventory;
    private boolean isProcessing = false;
    private final boolean invisibleBorders;
    private final Set<ItemStack> trackedItems = new HashSet<>();

    public SendMailGUI(GlobalPost plugin, Player player, String destinationServer, String recipientName) {
        this.plugin = plugin;
        this.player = player;
        this.destinationServer = destinationServer;
        this.recipientName = recipientName;
        this.invisibleBorders = plugin.getConfigManager().hasInvisibleBorders();
        this.inventory = Bukkit.createInventory(null, 54, "§6§lSend Mail to " + recipientName);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupGUI();
    }

    private void setupGUI() {
        // Create border (only if not invisible)
        ItemStack border = null;
        
        if (!invisibleBorders) {
            border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta borderMeta = border.getItemMeta();
            borderMeta.setDisplayName(" ");
            border.setItemMeta(borderMeta);
        }

        // Set border items (only if borders are visible)
        if (!invisibleBorders) {
            for (int i = 0; i < 54; i++) {
                if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                    inventory.setItem(i, border);
                }
            }
        }

        // Send button
        ItemStack sendButton = new ItemStack(Material.GREEN_WOOL);
        ItemMeta sendMeta = sendButton.getItemMeta();
        sendMeta.setDisplayName("§a§lSend Mail");
        sendMeta.setLore(List.of(
                "§7To: §f" + recipientName,
                "§7Server: §f" + destinationServer,
                "",
                "§aClick to send!"
        ));
        sendButton.setItemMeta(sendMeta);
        inventory.setItem(invisibleBorders ? 53 : 49, sendButton);

        // Cancel button
        ItemStack cancelButton = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName("§c§lCancel");
        cancelMeta.setLore(List.of("§7Click to cancel and return items"));
        cancelButton.setItemMeta(cancelMeta);
        inventory.setItem(invisibleBorders ? 45 : 45, cancelButton);

        // Info item
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§6§lMail Information");
        infoMeta.setLore(List.of(
                "§7Place items in the slots to send them",
                "§7to §f" + recipientName + " §7on §f" + destinationServer,
                "",
                "§7Maximum items: §f" + plugin.getConfigManager().getMaxItemsPerMail()
        ));
        info.setItemMeta(infoMeta);
        inventory.setItem(invisibleBorders ? 4 : 4, info);
    }

    private boolean isMailSlot(int slot) {
        if (invisibleBorders) {
            // Without borders, avoid the button/info slots
            return slot != 4 && slot != 45 && slot != 53;
        } else {
            // With borders, use the middle area excluding borders
            return slot >= 10 && slot <= 43 && slot % 9 != 0 && slot % 9 != 8;
        }
    }

    private int getSendButtonSlot() {
        return invisibleBorders ? 53 : 49;
    }

    private int getCancelButtonSlot() {
        return 45; // Same for both modes
    }

    private int getInfoSlot() {
        return 4; // Same for both modes
    }

    private boolean isItemAlreadyTracked(ItemStack item) {
        if (item == null) return false;
        
        // Check global tracking first
        if (!plugin.trackItemForMail(player.getUniqueId(), item)) {
            return true; // Already tracked globally
        }
        
        // Check if an equivalent item is already in the local tracked set
        for (ItemStack trackedItem : trackedItems) {
            if (trackedItem.isSimilar(item)) {
                // Release the global tracking since we found a local duplicate
                plugin.releaseItemTracking(player.getUniqueId(), item);
                return true;
            }
        }
        return false;
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();

        if (!clicker.equals(player)) return;

        int slot = event.getSlot();

        // Allow placing items in the mail area
        if (isMailSlot(slot)) {
            ItemStack clicked = event.getCurrentItem();

            if (event.isShiftClick() && event.getClickedInventory() == player.getInventory()) {
                // Handle shift click from player inventory (including hotbar)
                ItemStack item = event.getCurrentItem();
                if (item != null && !plugin.getBlacklistManager().isBlacklisted(item)) {
                    // Check if this exact item is already being tracked to prevent duplication
                    if (!isItemAlreadyTracked(item)) {
                        // Find empty slot in mail area
                        for (int i = 0; i < 54; i++) {
                            if (isMailSlot(i) && (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR)) {
                                ItemStack clonedItem = item.clone();
                                inventory.setItem(i, clonedItem);
                                trackedItems.add(clonedItem); // Track the cloned item
                                item.setAmount(0); // Remove from player inventory to prevent duplication
                                break;
                            }
                        }
                    }
                }
                event.setCancelled(true);
                return;
            }

            // Handle direct placement
            if (event.getAction().toString().contains("PLACE")) {
                ItemStack itemToPlace = event.getCursor();
                if (itemToPlace != null && !plugin.getBlacklistManager().isBlacklisted(itemToPlace)) {
                    if (!isItemAlreadyTracked(itemToPlace)) {
                        ItemStack clonedItem = itemToPlace.clone();
                        trackedItems.add(clonedItem);
                        // Allow the placement to continue normally
                        return;
                    } else {
                        event.setCancelled(true);
                        player.sendMessage("§cThis item is already being sent!");
                        return;
                    }
                }
            }

            if (clicked != null && plugin.getBlacklistManager().isBlacklisted(clicked)) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot send " + clicked.getType().name() + " through mail!");
                return;
            }

            return; // Allow normal item placement
        }

        event.setCancelled(true);

        if (slot == getSendButtonSlot()) { // Send button
            if (!isProcessing) {
                isProcessing = true;
                sendMail();
            }
        } else if (slot == getCancelButtonSlot()) { // Cancel button
            returnItems();
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getPlayer() instanceof Player)) return;

        Player closer = (Player) event.getPlayer();
        if (!closer.equals(player)) return;

        // Return items to player when closing without sending
        if (!isProcessing) {
            returnItems();
        }
    }

    private void sendMail() {
        List<ItemStack> items = new ArrayList<>();

        // Collect items from mail slots
        for (int i = 0; i < 54; i++) {
            if (isMailSlot(i)) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    items.add(item.clone());
                }
            }
        }

        if (items.isEmpty()) {
            player.sendMessage("§cYou need to add items to send mail!");
            isProcessing = false;
            return;
        }

        if (items.size() > plugin.getConfigManager().getMaxItemsPerMail()) {
            player.sendMessage("§cToo many items! Maximum: " + plugin.getConfigManager().getMaxItemsPerMail());
            isProcessing = false;
            return;
        }

        // Show processing message
        player.sendMessage("§eSending mail...");

        // Send the mail asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Get or create recipient UUID
                UUID recipientUUID = getRecipientUUID(recipientName);

                // Create and send mail
                boolean success = plugin.getMailboxManager().sendMailDirect(
                        player.getUniqueId(),
                        player.getName(),
                        recipientUUID,
                        recipientName,
                        destinationServer,
                        items,
                        null
                );

                // Handle result on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (success) {
                        player.sendMessage("§aMail sent successfully to " + recipientName + " on " + destinationServer + "!");

                        // Clear the mail slots and tracked items
                        for (int i = 0; i < 54; i++) {
                            if (isMailSlot(i)) {
                                ItemStack item = inventory.getItem(i);
                                if (item != null) {
                                    plugin.releaseItemTracking(player.getUniqueId(), item);
                                }
                                inventory.setItem(i, null);
                            }
                        }
                        trackedItems.clear();
                        plugin.clearPlayerTracking(player.getUniqueId());

                        player.closeInventory();
                    } else {
                        player.sendMessage("§cFailed to send mail! Please try again.");
                        isProcessing = false;
                    }
                });

            } catch (Exception e) {
                plugin.getLogger().severe("Error sending mail: " + e.getMessage());
                e.printStackTrace();

                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage("§cAn error occurred while sending mail!");
                    isProcessing = false;
                });
            }
        });
    }

    private UUID getRecipientUUID(String playerName) {
        // Try to get UUID from online player first
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        // Try to get UUID from offline player
        try {
            return Bukkit.getOfflinePlayer(playerName).getUniqueId();
        } catch (Exception e) {
            plugin.getLogger().warning("Could not get UUID for player: " + playerName);
            // Generate a random UUID as fallback (not ideal but prevents crashes)
            return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes());
        }
    }

    private void returnItems() {
        for (int i = 0; i < 54; i++) {
            if (isMailSlot(i)) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    // Try to add to player inventory, drop if full
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(item);
                    } else {
                        player.getWorld().dropItem(player.getLocation(), item);
                        player.sendMessage("§eDropped " + item.getType().name() + " because your inventory is full!");
                    }
                    // Release from tracking
                    plugin.releaseItemTracking(player.getUniqueId(), item);
                    trackedItems.remove(item);
                }
            }
        }
        // Clear all tracked items when closing
        trackedItems.clear();
        // Also clear any remaining global tracking for this player
        plugin.clearPlayerTracking(player.getUniqueId());
    }
}