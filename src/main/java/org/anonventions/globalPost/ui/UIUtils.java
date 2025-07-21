package org.anonventions.globalPost.ui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for UI operations and item creation
 */
public class UIUtils {
    
    /**
     * Creates a custom item with name and lore
     */
    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Creates a custom item with name and lore (varargs)
     */
    public static ItemStack createItem(Material material, String name, String... lore) {
        return createItem(material, name, Arrays.asList(lore));
    }
    
    /**
     * Creates a player skull item
     */
    public static ItemStack createPlayerSkull(Player player, String name, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.setOwningPlayer(player);
            skull.setItemMeta(meta);
        }
        return skull;
    }
    
    /**
     * Creates a player skull item by name
     */
    public static ItemStack createPlayerSkull(String playerName, String name, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            meta.setOwningPlayer(offlinePlayer);
            skull.setItemMeta(meta);
        }
        return skull;
    }
    
    /**
     * Creates a border item for GUIs
     */
    public static ItemStack createBorder(Material material, String name) {
        ItemStack border = new ItemStack(material);
        ItemMeta meta = border.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name != null ? name : " ");
            border.setItemMeta(meta);
        }
        return border;
    }
    
    /**
     * Creates a glowing item effect
     */
    public static ItemStack addGlow(ItemStack item) {
        // Note: This is a simplified version. For true glowing effects,
        // you might need NBT manipulation or custom enchantments
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Add a harmless enchantment and hide it for glow effect
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Formats time duration into readable string
     */
    public static String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " day" + (days != 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hour" + (hours != 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        } else {
            return seconds + " second" + (seconds != 1 ? "s" : "");
        }
    }
    
    /**
     * Creates an animated loading item (cycles through different materials)
     */
    public static ItemStack createLoadingItem(int frame) {
        Material[] loadingMaterials = {
            Material.CLOCK, Material.CLOCK, Material.CLOCK, Material.CLOCK
        };
        
        String[] loadingFrames = {
            "§e⟳ Loading...",
            "§e⟲ Loading...",
            "§e⟳ Loading...", 
            "§e⟲ Loading..."
        };
        
        int index = frame % loadingMaterials.length;
        return createItem(loadingMaterials[index], loadingFrames[index], "§7Please wait...");
    }
    
    /**
     * Wraps long text into multiple lines
     */
    public static List<String> wrapText(String text, int maxLength) {
        List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLength) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
            }
            
            if (currentLine.length() > 0) {
                currentLine.append(" ");
            }
            currentLine.append(word);
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines;
    }
    
    /**
     * Creates a progress bar string
     */
    public static String createProgressBar(int current, int max, int length, String completeChar, String incompleteChar, String color) {
        double percentage = (double) current / max;
        int completeBars = (int) (percentage * length);
        int incompleteBars = length - completeBars;
        
        StringBuilder bar = new StringBuilder();
        bar.append(color);
        
        for (int i = 0; i < completeBars; i++) {
            bar.append(completeChar);
        }
        
        bar.append("§7");
        for (int i = 0; i < incompleteBars; i++) {
            bar.append(incompleteChar);
        }
        
        return bar.toString();
    }
    
    /**
     * Centers text in a GUI title
     */
    public static String centerText(String text, int maxLength) {
        if (text.length() >= maxLength) {
            return text;
        }
        
        int spaces = (maxLength - text.length()) / 2;
        StringBuilder centered = new StringBuilder();
        
        for (int i = 0; i < spaces; i++) {
            centered.append(" ");
        }
        
        centered.append(text);
        return centered.toString();
    }
    
    /**
     * Gets the count of items in an inventory that match a material
     */
    public static int getItemCount(org.bukkit.inventory.Inventory inventory, Material material) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }
    
    /**
     * Gets available inventory slots
     */
    public static int getAvailableSlots(org.bukkit.inventory.PlayerInventory inventory) {
        int available = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                available++;
            }
        }
        return available;
    }
}