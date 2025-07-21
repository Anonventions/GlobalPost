package org.anonventions.globalPost.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Utility class for creating ItemStacks from configuration
 */
public class ItemBuilder {
    
    /**
     * Create an ItemStack from configuration parameters
     */
    public static ItemStack createItem(String material, int customModelData, String name, List<String> lore) {
        Material mat;
        try {
            mat = Material.valueOf(material.toUpperCase());
        } catch (IllegalArgumentException e) {
            mat = Material.STONE; // fallback
        }
        
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            if (name != null && !name.trim().isEmpty()) {
                meta.setDisplayName(name);
            }
            
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
            
            if (customModelData != 0) {
                meta.setCustomModelData(customModelData);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Replace placeholders in a string
     */
    public static String replacePlaceholders(String text, String sender, String server, String time, int items, String message) {
        if (text == null) return "";
        
        return text
                .replace("{sender}", sender != null ? sender : "Unknown")
                .replace("{server}", server != null ? server : "Unknown")
                .replace("{time}", time != null ? time : "Unknown")
                .replace("{items}", String.valueOf(items))
                .replace("{message}", message != null && !message.trim().isEmpty() ? message : "No message");
    }
    
    /**
     * Replace placeholders in a list of strings
     */
    public static List<String> replacePlaceholders(List<String> lore, String sender, String server, String time, int items, String message) {
        if (lore == null) return List.of();
        
        return lore.stream()
                .map(line -> replacePlaceholders(line, sender, server, time, items, message))
                .toList();
    }
}