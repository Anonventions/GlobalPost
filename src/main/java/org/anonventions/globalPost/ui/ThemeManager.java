package org.anonventions.globalPost.ui;

import org.anonventions.globalPost.GlobalPost;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages UI themes and player preferences
 */
public class ThemeManager {
    
    private final GlobalPost plugin;
    private final Map<String, UITheme> themes;
    private final Map<UUID, String> playerThemes;
    private UITheme defaultTheme;
    
    public ThemeManager(GlobalPost plugin) {
        this.plugin = plugin;
        this.themes = new HashMap<>();
        this.playerThemes = new HashMap<>();
        
        initializeDefaultThemes();
    }
    
    private void initializeDefaultThemes() {
        // Modern Theme (Default)
        UITheme modern = new UITheme(
                "modern",
                "§b§lModern §7Theme",
                "§b", "§3", "§f", "§c", "§a", "§e", "§f", "§7",
                Material.CYAN_STAINED_GLASS_PANE,
                Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                Material.WRITTEN_BOOK,
                Material.BARRIER,
                Material.LIME_CONCRETE,
                Material.RED_CONCRETE,
                Material.COMPASS,
                Material.EMERALD_BLOCK,
                Material.REDSTONE_BLOCK,
                Sound.UI_BUTTON_CLICK,
                Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                Sound.ENTITY_VILLAGER_NO,
                Sound.BLOCK_CHEST_OPEN,
                Sound.BLOCK_CHEST_CLOSE
        );
        
        // Dark Theme
        UITheme dark = new UITheme(
                "dark",
                "§8§lDark §7Theme",
                "§8", "§0", "§f", "§c", "§a", "§6", "§f", "§7",
                Material.BLACK_STAINED_GLASS_PANE,
                Material.GRAY_STAINED_GLASS_PANE,
                Material.ENCHANTED_BOOK,
                Material.COAL_BLOCK,
                Material.ARROW,
                Material.SPECTRAL_ARROW,
                Material.ENDER_EYE,
                Material.DIAMOND_BLOCK,
                Material.COAL_BLOCK,
                Sound.UI_BUTTON_CLICK,
                Sound.BLOCK_ANVIL_PLACE,
                Sound.ENTITY_ENDERMAN_TELEPORT,
                Sound.BLOCK_ENDER_CHEST_OPEN,
                Sound.BLOCK_ENDER_CHEST_CLOSE
        );
        
        // Neon Theme
        UITheme neon = new UITheme(
                "neon",
                "§d§lNeon §5Theme",
                "§d", "§5", "§f", "§c", "§a", "§e", "§f", "§7",
                Material.MAGENTA_STAINED_GLASS_PANE,
                Material.PURPLE_STAINED_GLASS_PANE,
                Material.ENCHANTED_BOOK,
                Material.PURPLE_CONCRETE,
                Material.MAGENTA_GLAZED_TERRACOTTA,
                Material.PURPLE_GLAZED_TERRACOTTA,
                Material.RECOVERY_COMPASS,
                Material.NETHERITE_BLOCK,
                Material.OBSIDIAN,
                Sound.BLOCK_AMETHYST_BLOCK_CHIME,
                Sound.BLOCK_AMETHYST_CLUSTER_PLACE,
                Sound.ENTITY_ENDERMAN_SCREAM,
                Sound.BLOCK_BEACON_ACTIVATE,
                Sound.BLOCK_BEACON_DEACTIVATE
        );
        
        // Nature Theme
        UITheme nature = new UITheme(
                "nature",
                "§a§lNature §2Theme",
                "§a", "§2", "§f", "§c", "§a", "§e", "§f", "§7",
                Material.GREEN_STAINED_GLASS_PANE,
                Material.LIME_STAINED_GLASS_PANE,
                Material.MAP,
                Material.DEAD_BUSH,
                Material.OAK_LEAVES,
                Material.SPRUCE_LEAVES,
                Material.SUNFLOWER,
                Material.EMERALD_BLOCK,
                Material.REDSTONE_BLOCK,
                Sound.BLOCK_GRASS_STEP,
                Sound.ENTITY_ITEM_PICKUP,
                Sound.ENTITY_WOLF_GROWL,
                Sound.BLOCK_WOODEN_DOOR_OPEN,
                Sound.BLOCK_WOODEN_DOOR_CLOSE
        );
        
        // Ocean Theme
        UITheme ocean = new UITheme(
                "ocean",
                "§9§lOcean §b Theme",
                "§9", "§1", "§b", "§c", "§a", "§e", "§f", "§7",
                Material.BLUE_STAINED_GLASS_PANE,
                Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                Material.MAP,
                Material.SPONGE,
                Material.PRISMARINE,
                Material.DARK_PRISMARINE,
                Material.HEART_OF_THE_SEA,
                Material.DIAMOND_BLOCK,
                Material.LAPIS_BLOCK,
                Sound.AMBIENT_UNDERWATER_ENTER,
                Sound.ENTITY_DOLPHIN_AMBIENT,
                Sound.ENTITY_GUARDIAN_AMBIENT,
                Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP,
                Sound.AMBIENT_UNDERWATER_EXIT
        );
        
        // Fire Theme
        UITheme fire = new UITheme(
                "fire",
                "§c§lFire §6Theme",
                "§c", "§4", "§6", "§c", "§a", "§e", "§f", "§7",
                Material.RED_STAINED_GLASS_PANE,
                Material.ORANGE_STAINED_GLASS_PANE,
                Material.PAPER,
                Material.CHARCOAL,
                Material.BLAZE_POWDER,
                Material.GUNPOWDER,
                Material.FIRE_CHARGE,
                Material.GOLD_BLOCK,
                Material.NETHERRACK,
                Sound.BLOCK_FIRE_AMBIENT,
                Sound.ENTITY_BLAZE_AMBIENT,
                Sound.ENTITY_GHAST_SCREAM,
                Sound.BLOCK_FIRE_EXTINGUISH,
                Sound.ENTITY_GENERIC_EXTINGUISH_FIRE
        );
        
        // Classic Theme (Original Style)
        UITheme classic = new UITheme(
                "classic",
                "§6§lClassic §7Theme",
                "§6", "§e", "§f", "§c", "§a", "§e", "§f", "§7",
                Material.GRAY_STAINED_GLASS_PANE,
                Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                Material.PAPER,
                Material.BARRIER,
                Material.COMPASS,
                Material.COMPASS,
                Material.COMPASS,
                Material.GREEN_WOOL,
                Material.RED_WOOL,
                Sound.UI_BUTTON_CLICK,
                Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                Sound.ENTITY_VILLAGER_NO,
                Sound.BLOCK_CHEST_OPEN,
                Sound.BLOCK_CHEST_CLOSE
        );
        
        registerTheme(modern);
        registerTheme(dark);
        registerTheme(neon);
        registerTheme(nature);
        registerTheme(ocean);
        registerTheme(fire);
        registerTheme(classic);
        
        this.defaultTheme = modern;
    }
    
    public void registerTheme(UITheme theme) {
        themes.put(theme.getName(), theme);
    }
    
    public UITheme getTheme(String name) {
        return themes.getOrDefault(name, defaultTheme);
    }
    
    public UITheme getPlayerTheme(Player player) {
        String themeName = playerThemes.get(player.getUniqueId());
        return themeName != null ? getTheme(themeName) : defaultTheme;
    }
    
    public UITheme getPlayerTheme(UUID playerUUID) {
        String themeName = playerThemes.get(playerUUID);
        return themeName != null ? getTheme(themeName) : defaultTheme;
    }
    
    public void setPlayerTheme(Player player, String themeName) {
        if (themes.containsKey(themeName)) {
            playerThemes.put(player.getUniqueId(), themeName);
            // Could save to database/file here
        }
    }
    
    public void setPlayerTheme(UUID playerUUID, String themeName) {
        if (themes.containsKey(themeName)) {
            playerThemes.put(playerUUID, themeName);
            // Could save to database/file here
        }
    }
    
    public Set<UITheme> getAllThemes() {
        return new HashSet<>(themes.values());
    }
    
    public Set<String> getThemeNames() {
        return themes.keySet();
    }
    
    public UITheme getDefaultTheme() {
        return defaultTheme;
    }
    
    public void setDefaultTheme(String themeName) {
        UITheme theme = themes.get(themeName);
        if (theme != null) {
            this.defaultTheme = theme;
        }
    }
    
    public void playSound(Player player, Sound sound) {
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 0.7f, 1.0f);
        }
    }
}