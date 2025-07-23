/*─────────────────────────────────────────────────────────────────────────────
 *  org/anonventions/globalPost/commands/PostCommand.java
 *───────────────────────────────────────────────────────────────────────────*/
package org.anonventions.globalPost.commands;

import org.anonventions.globalPost.GlobalPost;
import org.anonventions.globalPost.gui.MailboxGUI;
import org.anonventions.globalPost.gui.SendMailGUI;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * /post command entry‑point.
 * – Case‑insensitive server matching.
 * – Integrated tab completion using canonical names from ConfigManager.
 */
public class PostCommand implements CommandExecutor, TabCompleter {

    private final GlobalPost plugin;

    public PostCommand(GlobalPost plugin) { this.plugin = plugin; }

    /*------------------------------------------------------------------------*/
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player p)) { sender.sendMessage("§cPlayers only!"); return true; }

        if (args.length == 0) { new MailboxGUI(plugin, p).open(); return true; }

        switch (args[0].toLowerCase(Locale.ROOT)) {

            case "send" -> {
                if (args.length < 2) { 
                    p.sendMessage("§cUsage: /post send <server> [player]"); 
                    p.sendMessage("§7Available servers: " + String.join(", ", plugin.getMailboxManager().getAvailableDestinations()));
                    return true; 
                }

                String dest       = plugin.getConfigManager().normalised(args[1]);
                String recipient  = args.length > 2 ? args[2] : p.getName();
                
                // Check if destination is valid
                List<String> availableDestinations = plugin.getMailboxManager().getAvailableDestinations();
                if (!availableDestinations.contains(dest)) {
                    p.sendMessage("§cInvalid destination server: " + dest); 
                    p.sendMessage("§7Available servers: " + String.join(", ", availableDestinations));
                    return true;
                }
                
                // Special handling for local mail
                String currentServer = plugin.getConfigManager().getServerName();
                if (dest.equals(currentServer) || dest.equals("local")) {
                    dest = currentServer; // Ensure consistent naming for local mail
                }
                
                new SendMailGUI(plugin, p, dest, recipient).open();
            }

            case "check" ->
                    plugin.getMailboxManager().getMailCount(p.getUniqueId()).thenAccept(cnt ->
                            p.sendMessage("§aYou have " + cnt + " unread mail(s)."));

            case "reload" -> {
                if (!p.hasPermission("globalpost.admin")) { p.sendMessage("§cYou lack globalpost.admin"); return true; }
                plugin.getConfigManager().loadConfig();
                plugin.getBlacklistManager().reloadBlacklist();
                p.sendMessage("§aGlobalPost reloaded!");
            }

            default -> p.sendMessage("§cUsage: /post [send|check|reload]");
        }
        return true;
    }

    /*------------------------------------------------------------------------*/
    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (args.length == 1) return List.of("send", "check", "reload");
        if (args.length == 2 && args[0].equalsIgnoreCase("send"))
            return plugin.getMailboxManager().getAvailableDestinations();
        return Collections.emptyList();
    }
}
