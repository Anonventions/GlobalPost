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
import java.util.List;
import java.util.UUID;

public class SendMailGUI implements Listener {

    private final GlobalPost plugin;
    private final Player player;
    private final String destinationServer;
    private final String recipientName;
    private final Inventory inventory;
    private boolean isProcessing = false;

    public SendMailGUI(GlobalPost plugin, Player player, String destinationServer, String recipientName) {
        this.plugin = plugin;
        this.player = player;
        this.destinationServer = destinationServer;
        this.recipientName = recipientName;
        this.inventory = Bukkit.createInventory(null, 54, "§6§lSend Mail to " + recipientName);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupGUI();
    }

    private void setupGUI() {
        // Create border
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName(" ");
        border.setItemMeta(borderMeta);

        // Set border items
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
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
        inventory.setItem(49, sendButton);

        // Cancel button
        ItemStack cancelButton = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName("§c§lCancel");
        cancelMeta.setLore(List.of("§7Click to cancel and return items"));
        cancelButton.setItemMeta(cancelMeta);
        inventory.setItem(45, cancelButton);

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
        inventory.setItem(4, info);
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

        // Allow placing items in the mail area (slots 10-43, excluding borders)
        if (slot >= 10 && slot <= 43 && slot % 9 != 0 && slot % 9 != 8) {
            ItemStack clicked = event.getCurrentItem();

            if (event.isShiftClick() && event.getClickedInventory() == player.getInventory()) {
                // Handle shift click from player inventory
                ItemStack item = event.getCurrentItem();
                if (item != null && !plugin.getBlacklistManager().isBlacklisted(item)) {
                    // Find empty slot in mail area
                    for (int i = 10; i <= 43; i++) {
                        if (i % 9 != 0 && i % 9 != 8 && (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR)) {
                            inventory.setItem(i, item.clone());
                            item.setAmount(0);
                            break;
                        }
                    }
                }
                event.setCancelled(true);
                return;
            }

            if (clicked != null && plugin.getBlacklistManager().isBlacklisted(clicked)) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot send " + clicked.getType().name() + " through mail!");
                return;
            }

            return; // Allow normal item placement
        }

        event.setCancelled(true);

        if (slot == 49) { // Send button
            if (!isProcessing) {
                isProcessing = true;
                sendMail();
            }
        } else if (slot == 45) { // Cancel button
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
        for (int i = 10; i <= 43; i++) {
            if (i % 9 != 0 && i % 9 != 8) {
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

                        // Clear the mail slots
                        for (int i = 10; i <= 43; i++) {
                            if (i % 9 != 0 && i % 9 != 8) {
                                inventory.setItem(i, null);
                            }
                        }

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
        for (int i = 10; i <= 43; i++) {
            if (i % 9 != 0 && i % 9 != 8) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    // Try to add to player inventory, drop if full
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(item);
                    } else {
                        player.getWorld().dropItem(player.getLocation(), item);
                        player.sendMessage("§eDropped " + item.getType().name() + " because your inventory is full!");
                    }
                }
            }
        }
    }
}