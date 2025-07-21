/*─────────────────────────────────────────────────────────────────────────────
 *  org/anonventions/globalPost/commands/PostCommand.java
 *───────────────────────────────────────────────────────────────────────────*/
package org.anonventions.globalPost.commands;

import org.anonventions.globalPost.GlobalPost;
import org.anonventions.globalPost.gui.EnhancedMailboxGUI;
import org.anonventions.globalPost.gui.EnhancedSendMailGUI;
import org.anonventions.globalPost.gui.ThemeSelectionGUI;
import org.anonventions.globalPost.ui.UITheme;
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

        if (args.length == 0) { new EnhancedMailboxGUI(plugin, p).open(); return true; }

        switch (args[0].toLowerCase(Locale.ROOT)) {

            case "send" -> {
                if (args.length < 2) { p.sendMessage("§cUsage: /post send <server> [player]"); return true; }

                String dest       = plugin.getConfigManager().normalised(args[1]);
                String recipient  = args.length > 2 ? args[2] : p.getName();

                if (!plugin.getConfigManager().getAllowedDestinations().contains(dest)) {
                    p.sendMessage("§cInvalid destination server: " + dest); return true;
                }
                new EnhancedSendMailGUI(plugin, p, dest, recipient).open();
            }

            case "check" -> {
                UITheme theme = plugin.getThemeManager().getPlayerTheme(p);
                plugin.getMailboxManager().getMailCount(p.getUniqueId()).thenAccept(cnt ->
                        p.sendMessage(theme.getAccentColor() + "📬 You have " + theme.getSuccessColor() + 
                        cnt + theme.getAccentColor() + " unread mail(s)."));
            }

            case "theme", "themes" -> new ThemeSelectionGUI(plugin, p).open();

            case "reload" -> {
                if (!p.hasPermission("globalpost.admin")) { 
                    UITheme theme = plugin.getThemeManager().getPlayerTheme(p);
                    p.sendMessage(theme.getErrorColor() + "✗ You lack globalpost.admin permission"); 
                    return true; 
                }
                plugin.getConfigManager().loadConfig();
                plugin.getBlacklistManager().reloadBlacklist();
                UITheme theme = plugin.getThemeManager().getPlayerTheme(p);
                p.sendMessage(theme.getSuccessColor() + "✓ GlobalPost reloaded!");
            }

            default -> {
                UITheme theme = plugin.getThemeManager().getPlayerTheme(p);
                p.sendMessage(theme.getErrorColor() + "Usage: " + theme.getAccentColor() + 
                    "/post " + theme.getTextColor() + "[send|check|theme|reload]");
            }
        }
        return true;
    }

    /*------------------------------------------------------------------------*/
    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (args.length == 1) return List.of("send", "check", "theme", "themes", "reload");
        if (args.length == 2 && args[0].equalsIgnoreCase("send"))
            return plugin.getConfigManager().getAllowedDestinations();
        return Collections.emptyList();
    }
}
