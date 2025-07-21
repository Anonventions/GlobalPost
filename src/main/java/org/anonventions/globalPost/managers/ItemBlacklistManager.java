package org.anonventions.globalPost.managers;

import org.anonventions.globalPost.GlobalPost;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class ItemBlacklistManager {

    private final GlobalPost plugin;
    private final Set<Material> blacklistedItems;

    public ItemBlacklistManager(GlobalPost plugin) {
        this.plugin = plugin;
        this.blacklistedItems = new HashSet<>();
        loadBlacklist();
    }

    private void loadBlacklist() {
        blacklistedItems.clear();

        for (String itemName : plugin.getConfigManager().getBlacklistedItems()) {
            try {
                Material material = Material.valueOf(itemName.toUpperCase());
                blacklistedItems.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in blacklist: " + itemName);
            }
        }

        plugin.getLogger().info("Loaded " + blacklistedItems.size() + " blacklisted items");
    }

    public boolean isBlacklisted(ItemStack item) {
        return item != null && blacklistedItems.contains(item.getType());
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