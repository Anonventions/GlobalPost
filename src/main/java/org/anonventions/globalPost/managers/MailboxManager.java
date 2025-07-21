/*─────────────────────────────────────────────────────────────────────────────
 *  org/anonventions/globalPost/managers/MailboxManager.java
 *───────────────────────────────────────────────────────────────────────────*/
package org.anonventions.globalPost.managers;

import org.anonventions.globalPost.GlobalPost;
import org.anonventions.globalPost.models.Mail;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * High‑level mail orchestration.
 * – Centralised name canonicalisation.
 * – Delegates persistence to DatabaseManager.
 */
public class MailboxManager {

    private final GlobalPost plugin;

    public MailboxManager(GlobalPost plugin) { this.plugin = plugin; }

    private String canonical(String s) { return plugin.getConfigManager().normalised(s); }

    /*------------------------------------------------------------------------*/
    public CompletableFuture<Boolean> sendMail(Player sender, String recipient, String dest,
                                               List<ItemStack> items, String msg) {
        return CompletableFuture.supplyAsync(() ->
                sendCore(sender.getUniqueId(), sender.getName(), recipient, dest, items, msg));
    }

    public boolean sendMailDirect(UUID senderUUID, String senderName, UUID recipientUUID,
                                  String recipientName, String dest, List<ItemStack> items, String msg) {
        return sendCore(senderUUID, senderName, recipientName, dest, items, msg);
    }

    private boolean sendCore(UUID senderUUID, String senderName, String recipientName,
                             String dest, List<ItemStack> items, String msg) {

        dest = canonical(dest);
        if (!plugin.getConfigManager().getAllowedDestinations().contains(dest)) {
            plugin.getLogger().warning("Invalid destination: " + dest); return false;
        }

        for (ItemStack it : items)
            if (plugin.getBlacklistManager().isBlacklisted(it)) {
                plugin.getLogger().warning("Blacklisted: " + it.getType()); return false;
            }

        Mail mail = new Mail(
                senderUUID, senderName,
                getRecipientUUID(recipientName), recipientName,
                plugin.getConfigManager().getServerName(), dest,
                items, msg);

        return plugin.getDatabaseManager().saveMail(mail).join();
    }

    /*------------------------------------------------------------------------*/
    public CompletableFuture<List<Mail>> getPlayerMails(UUID uuid) {
        return plugin.getDatabaseManager().getUnreadMails(uuid,
                plugin.getConfigManager().getServerName());
    }

    public CompletableFuture<Boolean> collectMail(int id, Player p) {
        return plugin.getDatabaseManager().markMailAsCollected(id).thenApply(success -> {
            if (success) plugin.getLogger().info("Mail "+id+" collected by "+p.getName());
            return success;
        });
    }

    public CompletableFuture<Integer> getMailCount(UUID uuid) {
        return plugin.getDatabaseManager().getMailCount(uuid,
                plugin.getConfigManager().getServerName());
    }

    /*------------------------------------------------------------------------*/
    private UUID getRecipientUUID(String playerName) {
        Player online = Bukkit.getPlayer(playerName);
        if (online != null) return online.getUniqueId();
        try { return Bukkit.getOfflinePlayer(playerName).getUniqueId(); }
        catch (Exception ex) {
            plugin.getLogger().warning("UUID lookup failed for "+playerName);
            return UUID.nameUUIDFromBytes(("OfflinePlayer:"+playerName).getBytes());
        }
    }
}
