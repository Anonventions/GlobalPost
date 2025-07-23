package org.anonventions.globalPost.managers;

import org.anonventions.globalPost.GlobalPost;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Handles playing sounds for mail system events
 */
public class SoundManager {
    
    private final GlobalPost plugin;
    
    public SoundManager(GlobalPost plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Play GUI open sound to player
     */
    public void playGuiOpenSound(Player player) {
        playSound(player, plugin.getConfigManager().getGuiOpenSound());
    }
    
    /**
     * Play mail send sound to player
     */
    public void playMailSendSound(Player player) {
        playSound(player, plugin.getConfigManager().getMailSendSound());
    }
    
    /**
     * Play mail receive sound to player
     */
    public void playMailReceiveSound(Player player) {
        playSound(player, plugin.getConfigManager().getMailReceiveSound());
    }
    
    /**
     * Play a sound to a player if sounds are enabled
     */
    private void playSound(Player player, String soundName) {
        if (!plugin.getConfigManager().isSoundEnabled() || soundName == null) {
            return;
        }
        
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            float volume = plugin.getConfigManager().getSoundVolume();
            float pitch = plugin.getConfigManager().getSoundPitch();
            
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound name in config: " + soundName);
        }
    }
}