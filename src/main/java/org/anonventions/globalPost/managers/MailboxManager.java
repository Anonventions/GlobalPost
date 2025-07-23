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
        String currentServer = plugin.getConfigManager().getServerName();
        
        // If local mail is enabled and destination is the same server, allow it
        boolean isLocalMail = plugin.getConfigManager().isLocalMailEnabled() && 
                             (dest.equals(currentServer) || dest.isEmpty());
        
        // If MySQL is not configured, only allow local mail
        boolean mysqlConfigured = "mysql".equalsIgnoreCase(plugin.getConfigManager().getDatabaseType());
        
        if (!mysqlConfigured && !isLocalMail) {
            plugin.getLogger().warning("Cross-server mail disabled - MySQL not configured. Use local mail instead.");
            return false;
        }
        
        // For configured MySQL setups, check allowed destinations
        if (mysqlConfigured && !isLocalMail && !plugin.getConfigManager().getAllowedDestinations().contains(dest)) {
            plugin.getLogger().warning("Invalid destination: " + dest); 
            return false;
        }

        for (ItemStack it : items)
            if (plugin.getBlacklistManager().isBlacklisted(it)) {
                plugin.getLogger().warning("Blacklisted: " + it.getType()); return false;
            }

        // For local mail, set destination to current server
        String finalDest = isLocalMail ? currentServer : dest;

        Mail mail = new Mail(
                senderUUID, senderName,
                getRecipientUUID(recipientName), recipientName,
                currentServer, finalDest,
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
    /**
     * Check if local mail is supported
     */
    public boolean isLocalMailSupported() {
        return plugin.getConfigManager().isLocalMailEnabled();
    }
    
    /**
     * Check if cross-server mail is available (MySQL configured)
     */
    public boolean isCrossServerMailAvailable() {
        return "mysql".equalsIgnoreCase(plugin.getConfigManager().getDatabaseType());
    }
    
    /**
     * Get available destination servers for mail
     */
    public List<String> getAvailableDestinations() {
        List<String> destinations = new ArrayList<>();
        
        // Add local server if local mail is enabled
        if (isLocalMailSupported()) {
            destinations.add(plugin.getConfigManager().getServerName());
        }
        
        // Add configured destinations if MySQL is available
        if (isCrossServerMailAvailable()) {
            destinations.addAll(plugin.getConfigManager().getAllowedDestinations());
        }
        
        return destinations;
    }

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
