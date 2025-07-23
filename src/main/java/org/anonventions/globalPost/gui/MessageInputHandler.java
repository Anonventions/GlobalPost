package org.anonventions.globalPost.gui;

import org.anonventions.globalPost.GlobalPost;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Handles message input through chat for mail system
 */
public class MessageInputHandler implements Listener {
    
    private final GlobalPost plugin;
    private final Set<Player> awaitingInput = new HashSet<>();
    private final Set<Player> awaitingConfirmation = new HashSet<>();
    private final java.util.Map<Player, String> playerMessages = new java.util.HashMap<>();
    private final java.util.Map<Player, Consumer<String>> callbacks = new java.util.HashMap<>();
    private final java.util.Map<Player, BukkitRunnable> timeoutTasks = new java.util.HashMap<>();
    
    public MessageInputHandler(GlobalPost plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Request message input from player
     */
    public void requestMessage(Player player, Consumer<String> callback) {
        if (awaitingInput.contains(player) || awaitingConfirmation.contains(player)) {
            player.sendMessage("§cYou are already inputting a message!");
            return;
        }
        
        awaitingInput.add(player);
        callbacks.put(player, callback);
        
        player.sendMessage(plugin.getConfigManager().getChatInputPrompt());
        
        // Set timeout
        int timeout = plugin.getConfigManager().getChatInputTimeout();
        BukkitRunnable timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (awaitingInput.contains(player) || awaitingConfirmation.contains(player)) {
                    cleanupPlayer(player);
                    player.sendMessage("§cMessage input timed out.");
                }
            }
        };
        timeoutTask.runTaskLater(plugin, timeout * 20L);
        timeoutTasks.put(player, timeoutTask);
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        if (awaitingInput.contains(player)) {
            event.setCancelled(true);
            
            if (message.equalsIgnoreCase("cancel")) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    cleanupPlayer(player);
                    player.sendMessage("§cMessage cancelled.");
                });
                return;
            }
            
            if (message.length() > plugin.getConfigManager().getMaxMessageLength()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage("§cMessage too long! Maximum: " + plugin.getConfigManager().getMaxMessageLength() + " characters");
                });
                return;
            }
            
            playerMessages.put(player, message);
            awaitingInput.remove(player);
            awaitingConfirmation.add(player);
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage("§eYour message: §f" + message);
                player.sendMessage(plugin.getConfigManager().getChatAcceptPrompt());
            });
            
        } else if (awaitingConfirmation.contains(player)) {
            event.setCancelled(true);
            
            if (message.equalsIgnoreCase("confirm")) {
                String playerMessage = playerMessages.get(player);
                Consumer<String> callback = callbacks.get(player);
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    cleanupPlayer(player);
                    if (callback != null) {
                        callback.accept(playerMessage);
                    }
                });
                
            } else if (message.equalsIgnoreCase("cancel")) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    cleanupPlayer(player);
                    player.sendMessage("§cMessage cancelled.");
                });
            } else {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage("§cPlease type 'confirm' to send or 'cancel' to abort.");
                });
            }
        }
    }
    
    /**
     * Clean up player data
     */
    private void cleanupPlayer(Player player) {
        awaitingInput.remove(player);
        awaitingConfirmation.remove(player);
        playerMessages.remove(player);
        callbacks.remove(player);
        
        BukkitRunnable timeoutTask = timeoutTasks.remove(player);
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }
    }
    
    /**
     * Check if player is currently inputting a message
     */
    public boolean isInputtingMessage(Player player) {
        return awaitingInput.contains(player) || awaitingConfirmation.contains(player);
    }
}