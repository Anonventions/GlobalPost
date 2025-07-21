package org.anonventions.globalPost.managers;

import org.anonventions.globalPost.GlobalPost;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemBlacklistManager {

    private final GlobalPost plugin;
    private final Set<Material> blacklistedItems;
    private Set<String> blacklistedDisplayNames;
    private Set<String> blacklistedLoreContains;
    private Set<String> blacklistedNBTKeys;

    public ItemBlacklistManager(GlobalPost plugin) {
        this.plugin = plugin;
        this.blacklistedItems = new HashSet<>();
        this.blacklistedDisplayNames = new HashSet<>();
        this.blacklistedLoreContains = new HashSet<>();
        this.blacklistedNBTKeys = new HashSet<>();
        loadBlacklist();
    }

    private void loadBlacklist() {
        blacklistedItems.clear();
        blacklistedDisplayNames.clear();
        blacklistedLoreContains.clear();
        blacklistedNBTKeys.clear();

        // Load material blacklist
        for (String itemName : plugin.getConfigManager().getBlacklistedItems()) {
            try {
                Material material = Material.valueOf(itemName.toUpperCase());
                blacklistedItems.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in blacklist: " + itemName);
            }
        }

        // Load custom item blacklists
        blacklistedDisplayNames.addAll(plugin.getConfigManager().getBlacklistedDisplayNames());
        blacklistedLoreContains.addAll(plugin.getConfigManager().getBlacklistedLoreContains());
        blacklistedNBTKeys.addAll(plugin.getConfigManager().getBlacklistedNBTKeys());

        plugin.getLogger().info("Loaded " + blacklistedItems.size() + " blacklisted materials, " +
                blacklistedDisplayNames.size() + " display names, " +
                blacklistedLoreContains.size() + " lore patterns, " +
                blacklistedNBTKeys.size() + " NBT keys");
    }

    public boolean isBlacklisted(ItemStack item) {
        if (item == null) return false;

        // Check material blacklist
        if (blacklistedItems.contains(item.getType())) {
            return true;
        }

        // Check custom item properties
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Check display name
            if (meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                for (String blacklistedName : blacklistedDisplayNames) {
                    if (displayName.equals(blacklistedName)) {
                        return true;
                    }
                }
            }

            // Check lore
            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
                for (String loreLine : lore) {
                    for (String blacklistedLore : blacklistedLoreContains) {
                        if (loreLine.contains(blacklistedLore)) {
                            return true;
                        }
                    }
                }
            }

            // Check NBT data (this is a simplified approach - in practice you might need reflection or NBT-API)
            try {
                String itemAsString = item.toString();
                for (String nbtKey : blacklistedNBTKeys) {
                    if (itemAsString.contains(nbtKey)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error checking NBT data for item: " + e.getMessage());
            }
        }

        return false;
    }

    public boolean isBlacklisted(Material material) {
        return blacklistedItems.contains(material);
    }

    public Set<Material> getBlacklistedItems() {
        return new HashSet<>(blacklistedItems);
    }

    public void reloadBlacklist() {
        loadBlacklist();
    }
}