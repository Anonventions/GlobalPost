package org.anonventions.globalPost.gui;

import org.anonventions.globalPost.GlobalPost;
import org.anonventions.globalPost.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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
    private String mailMessage = null;

    public SendMailGUI(GlobalPost plugin, Player player, String destinationServer, String recipientName) {
        this.plugin = plugin;
        this.player = player;
        this.destinationServer = destinationServer;
        this.recipientName = recipientName;
        
        String title = plugin.getConfigManager().getMailboxTitle().replace("{recipient}", recipientName);
        this.inventory = Bukkit.createInventory(null, 54, title);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupGUI();
    }

    private void setupGUI() {
        // Create border
        setupBorder();
        
        // Add player head
        setupPlayerHead();
        
        // Add control buttons
        setupControlButtons();
        
        // Add info item
        setupInfoItem();
    }
    
    private void setupBorder() {
        ItemStack border = ItemBuilder.createItem(
            plugin.getConfigManager().getBorderMaterial(),
            plugin.getConfigManager().getBorderCustomModelData(),
            plugin.getConfigManager().getBorderName(),
            plugin.getConfigManager().getBorderLore()
        );
        
        // Set border items
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                // Skip content slots and special button slots
                List<Integer> contentSlots = plugin.getConfigManager().getContentSlots();
                if (!contentSlots.contains(i) && i != 4 && i != 45 && i != 48 && i != 49) {
                    inventory.setItem(i, border);
                }
            }
        }
    }
    
    private void setupPlayerHead() {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
        
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName("§6" + player.getName());
            meta.setLore(List.of("§7Sending mail to:", "§f" + recipientName, "§7on server: §f" + destinationServer));
            playerHead.setItemMeta(meta);
        }
        
        inventory.setItem(plugin.getConfigManager().getPlayerHeadSlot(), playerHead);
    }
    
    private void setupControlButtons() {
        // Send button
        List<String> sendLore = List.of(
            "§7To: §f" + recipientName,
            "§7Server: §f" + destinationServer,
            "",
            "§aClick to send!"
        );
        ItemStack sendButton = ItemBuilder.createItem("GREEN_WOOL", 0, "§a§lSend Mail", sendLore);
        inventory.setItem(48, sendButton);
        
        // Message button
        if (plugin.getConfigManager().isMessagingEnabled()) {
            List<String> messageLore = List.of(
                "§7Click to add a message",
                "§7to your mail package",
                "",
                mailMessage != null ? "§aMessage: §f" + mailMessage : "§7No message set"
            );
            ItemStack messageButton = ItemBuilder.createItem("WRITABLE_BOOK", 0, "§e§lAdd Message", messageLore);
            inventory.setItem(49, messageButton);
        }
        
        // Cancel button
        ItemStack cancelButton = ItemBuilder.createItem("RED_WOOL", 0, "§c§lCancel", 
            List.of("§7Click to cancel and return items"));
        inventory.setItem(45, cancelButton);
    }
    
    private void setupInfoItem() {
        List<String> infoLore = List.of(
            "§7Place items in the slots to send them",
            "§7to §f" + recipientName + " §7on §f" + destinationServer,
            "",
            "§7Maximum items: §f" + plugin.getConfigManager().getMaxItemsPerMail()
        );
        ItemStack info = ItemBuilder.createItem("BOOK", 0, "§6§lMail Information", infoLore);
        inventory.setItem(53, info); // Put info item in a different slot
    }

    public void open() {
        player.openInventory(inventory);
        plugin.getSoundManager().playGuiOpenSound(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();

        if (!clicker.equals(player)) return;

        int slot = event.getSlot();
        List<Integer> contentSlots = plugin.getConfigManager().getContentSlots();

        // Allow placing items in the mail area (content slots)
        if (contentSlots.contains(slot)) {
            ItemStack clicked = event.getCurrentItem();

            if (event.isShiftClick() && event.getClickedInventory() == player.getInventory()) {
                // Handle shift click from player inventory
                ItemStack item = event.getCurrentItem();
                if (item != null && !plugin.getBlacklistManager().isBlacklisted(item)) {
                    // Find empty slot in mail area
                    for (int contentSlot : contentSlots) {
                        if (inventory.getItem(contentSlot) == null || inventory.getItem(contentSlot).getType() == Material.AIR) {
                            inventory.setItem(contentSlot, item.clone());
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

        if (slot == 48) { // Send button
            if (!isProcessing) {
                isProcessing = true;
                sendMail();
            }
        } else if (slot == 49 && plugin.getConfigManager().isMessagingEnabled()) { // Message button
            if (!plugin.getMessageInputHandler().isInputtingMessage(player)) {
                player.closeInventory();
                plugin.getMessageInputHandler().requestMessage(player, message -> {
                    mailMessage = message;
                    player.sendMessage("§aMessage added to mail!");
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        open(); // Reopen the GUI
                        setupControlButtons(); // Update the message button
                    });
                });
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
        List<Integer> contentSlots = plugin.getConfigManager().getContentSlots();

        // Collect items from mail slots
        for (int slot : contentSlots) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                items.add(item.clone());
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
                        mailMessage
                );

                // Handle result on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (success) {
                        player.sendMessage("§aMail sent successfully to " + recipientName + " on " + destinationServer + "!");
                        plugin.getSoundManager().playMailSendSound(player);

                        // Clear the mail slots
                        for (int slot : contentSlots) {
                            inventory.setItem(slot, null);
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
        List<Integer> contentSlots = plugin.getConfigManager().getContentSlots();
        for (int slot : contentSlots) {
            ItemStack item = inventory.getItem(slot);
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