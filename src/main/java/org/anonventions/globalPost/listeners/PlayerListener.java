/*─────────────────────────────────────────────────────────────────────────────
 *  org/anonventions/globalPost/listeners/PlayerListener.java
 *───────────────────────────────────────────────────────────────────────────*/
package org.anonventions.globalPost.listeners;

import org.anonventions.globalPost.GlobalPost;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Notifies players of unread mail on join.
 * Uses MailboxManager#getMailCount which already applies per‑server filter.
 */
public class PlayerListener implements Listener {

    private final GlobalPost plugin;
    public PlayerListener(GlobalPost plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        plugin.getMailboxManager().getMailCount(p.getUniqueId()).thenAccept(cnt -> {
            if (cnt > 0)
                plugin.getServer().getScheduler().runTaskLater(plugin,
                        () -> p.sendMessage("§6[Mail] §aYou have " + cnt + " unread mail(s)! Use /post to check."),
                        20L);
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        // Clear any item tracking when player disconnects to prevent memory leaks
        plugin.clearPlayerTracking(p.getUniqueId());
    }
}
