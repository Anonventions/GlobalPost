package org.anonventions.globalPost.gui;

import org.anonventions.globalPost.GlobalPost;
import org.anonventions.globalPost.models.Mail;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MailboxGUI implements Listener {

    private final GlobalPost plugin;
    private final Player player;
    private final Inventory inventory;
    private List<Mail> mails;

    public MailboxGUI(GlobalPost plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§6§lMailbox");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadMails();
    }

    private void loadMails() {
        plugin.getMailboxManager().getPlayerMails(player.getUniqueId()).thenAccept(mailList -> {
            this.mails = mailList;
            Bukkit.getScheduler().runTask(plugin, this::updateInventory);
        });
    }

    private void updateInventory() {
        inventory.clear();

        if (mails.isEmpty()) {
            ItemStack noMail = new ItemStack(Material.BARRIER);
            ItemMeta meta = noMail.getItemMeta();
            meta.setDisplayName("§cNo mail");
            meta.setLore(List.of("§7You have no unread mail."));
            noMail.setItemMeta(meta);
            inventory.setItem(22, noMail);
            return;
        }

        for (int i = 0; i < Math.min(mails.size(), 45); i++) {
            Mail mail = mails.get(i);
            ItemStack mailItem = new ItemStack(Material.PAPER);
            ItemMeta meta = mailItem.getItemMeta();

            meta.setDisplayName("§6Mail from " + mail.getSenderName());

            List<String> lore = new ArrayList<>();
            lore.add("§7From: §f" + mail.getSenderName());
            lore.add("§7Server: §f" + mail.getSourceServer());
            lore.add("§7Items: §f" + mail.getItems().size());

            if (mail.getMessage() != null && !mail.getMessage().trim().isEmpty()) {
                lore.add("§7Message: §f" + mail.getMessage());
            }

            lore.add("");
            lore.add("§aClick to collect!");

            meta.setLore(lore);
            mailItem.setItemMeta(meta);

            inventory.setItem(i, mailItem);
        }

        // Add navigation and utility items
        ItemStack sendMail = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta sendMeta = sendMail.getItemMeta();
        sendMeta.setDisplayName("§aSend Mail");
        sendMeta.setLore(List.of("§7Click to send mail to another server"));
        sendMail.setItemMeta(sendMeta);
        inventory.setItem(49, sendMail);

        ItemStack refresh = new ItemStack(Material.COMPASS);
        ItemMeta refreshMeta = refresh.getItemMeta();
        refreshMeta.setDisplayName("§bRefresh");
        refreshMeta.setLore(List.of("§7Click to refresh your mailbox"));
        refresh.setItemMeta(refreshMeta);
        inventory.setItem(53, refresh);
    }

    public void open() {
        player.openInventory(inventory);
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

        if (slot == 49) { // Send mail
            clicker.closeInventory();
            // Open server selection GUI or command
            clicker.sendMessage("§aUse /post send <server> [player] to send mail!");
            return;
        }

        if (slot == 53) { // Refresh
            loadMails();
            return;
        }

        if (slot < mails.size()) {
            Mail mail = mails.get(slot);
            collectMail(mail, clicker);
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
                    loadMails(); // Refresh the GUI
                });
            }
        });
    }
}