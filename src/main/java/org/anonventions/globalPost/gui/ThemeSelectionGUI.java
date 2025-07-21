package org.anonventions.globalPost.gui;

import org.anonventions.globalPost.GlobalPost;
import org.anonventions.globalPost.ui.ThemeManager;
import org.anonventions.globalPost.ui.UITheme;
import org.anonventions.globalPost.ui.UIUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for selecting and previewing themes
 */
public class ThemeSelectionGUI implements Listener {

    private final GlobalPost plugin;
    private final Player player;
    private final ThemeManager themeManager;
    private final Inventory inventory;

    // Layout constants
    private static final int[] THEME_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int PREVIEW_SLOT = 22;
    private static final int BACK_SLOT = 45;
    private static final int APPLY_SLOT = 53;
    private static final int INFO_SLOT = 4;

    public ThemeSelectionGUI(GlobalPost plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.themeManager = plugin.getThemeManager();
        
        UITheme currentTheme = themeManager.getPlayerTheme(player);
        this.inventory = Bukkit.createInventory(null, 54, 
            currentTheme.getPrimaryColor() + "🎨 " + currentTheme.getAccentColor() + "§lTheme Selection");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        updateInventory();
    }

    private void updateInventory() {
        inventory.clear();
        UITheme currentTheme = themeManager.getPlayerTheme(player);
        
        // Create border
        createBorder(currentTheme);
        
        // Display available themes
        displayThemes();
        
        // Control buttons
        createControlButtons(currentTheme);
        
        // Info panel
        createInfoPanel(currentTheme);
        
        // Preview area
        createPreviewArea(currentTheme);
    }

    private void createBorder(UITheme theme) {
        ItemStack border = UIUtils.createBorder(theme.getBorderMaterial(), " ");
        
        // Top and bottom borders
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(i + 45, border);
        }
        
        // Side borders
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }
    }

    private void displayThemes() {
        List<UITheme> themes = new ArrayList<>(themeManager.getAllThemes());
        UITheme currentTheme = themeManager.getPlayerTheme(player);
        
        for (int i = 0; i < Math.min(themes.size(), THEME_SLOTS.length); i++) {
            UITheme theme = themes.get(i);
            boolean isSelected = theme.getName().equals(currentTheme.getName());
            
            ItemStack themeItem = createThemeItem(theme, isSelected);
            inventory.setItem(THEME_SLOTS[i], themeItem);
        }
    }

    private ItemStack createThemeItem(UITheme theme, boolean isSelected) {
        Material material = theme.getButtonMaterial();
        
        List<String> lore = new ArrayList<>();
        lore.add(theme.getAccentColor() + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        lore.add(theme.getTextColor() + "Theme Description:");
        lore.add(theme.getSubtitleColor() + "A beautiful " + theme.getName() + " themed interface");
        lore.add("");
        lore.add(theme.getTextColor() + "Color Scheme:");
        lore.add(theme.getSubtitleColor() + "• Primary: " + theme.getPrimaryColor() + "●●●");
        lore.add(theme.getSubtitleColor() + "• Secondary: " + theme.getSecondaryColor() + "●●●");
        lore.add(theme.getSubtitleColor() + "• Accent: " + theme.getAccentColor() + "●●●");
        lore.add(theme.getSubtitleColor() + "• Success: " + theme.getSuccessColor() + "●●●");
        lore.add(theme.getSubtitleColor() + "• Error: " + theme.getErrorColor() + "●●●");
        lore.add("");
        lore.add(theme.getTextColor() + "Features:");
        lore.add(theme.getSubtitleColor() + "• Custom color scheme");
        lore.add(theme.getSubtitleColor() + "• Themed materials and sounds");
        lore.add(theme.getSubtitleColor() + "• Optimized for readability");
        lore.add("");
        
        if (isSelected) {
            lore.add(theme.getSuccessColor() + "✓ Currently Selected");
            lore.add(theme.getSubtitleColor() + "This is your active theme");
        } else {
            lore.add(theme.getAccentColor() + "► Click to preview this theme");
            lore.add(theme.getSuccessColor() + "► Double-click to apply theme");
        }
        
        lore.add(theme.getAccentColor() + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        
        String title = theme.getDisplayName();
        if (isSelected) {
            title += " " + theme.getSuccessColor() + "✓";
        }
        
        ItemStack item = UIUtils.createItem(material, title, lore);
        
        if (isSelected) {
            item = UIUtils.addGlow(item);
        }
        
        return item;
    }

    private void createControlButtons(UITheme theme) {
        // Back button
        ItemStack back = UIUtils.createItem(
            theme.getCancelMaterial(),
            theme.getErrorColor() + "§l◀ Back to Mailbox",
            theme.getSubtitleColor() + "Return to your mailbox",
            "",
            theme.getErrorColor() + "▶ Click to go back"
        );
        inventory.setItem(BACK_SLOT, back);
        
        // Apply button (currently just shows info since theme changes are instant)
        ItemStack apply = UIUtils.createItem(
            theme.getSendMaterial(),
            theme.getSuccessColor() + "§l✓ Theme Settings",
            theme.getSubtitleColor() + "Theme changes are applied instantly",
            "",
            theme.getTextColor() + "Current theme: " + theme.getDisplayName(),
            "",
            theme.getAccentColor() + "► Click a theme above to change it"
        );
        inventory.setItem(APPLY_SLOT, apply);
    }

    private void createInfoPanel(UITheme theme) {
        ItemStack info = UIUtils.createItem(
            Material.KNOWLEDGE_BOOK,
            theme.getPrimaryColor() + "§l🎨 Theme Information",
            "",
            theme.getTextColor() + "Available Themes: " + theme.getAccentColor() + themeManager.getAllThemes().size(),
            theme.getTextColor() + "Current Theme: " + theme.getDisplayName(),
            "",
            theme.getSubtitleColor() + "Themes change the appearance of:",
            theme.getSubtitleColor() + "• Colors and text formatting",
            theme.getSubtitleColor() + "• Button and border materials",
            theme.getSubtitleColor() + "• Sound effects and feedback",
            theme.getSubtitleColor() + "• Overall visual style",
            "",
            theme.getAccentColor() + "Choose a theme that suits your style!"
        );
        inventory.setItem(INFO_SLOT, info);
    }

    private void createPreviewArea(UITheme theme) {
        // Create a preview of the current theme
        ItemStack preview = UIUtils.createItem(
            Material.PAINTING,
            theme.getPrimaryColor() + "§l🖼 Theme Preview",
            "",
            theme.getTextColor() + "Primary Color: " + theme.getPrimaryColor() + "Sample Text",
            theme.getTextColor() + "Secondary Color: " + theme.getSecondaryColor() + "Sample Text",
            theme.getTextColor() + "Accent Color: " + theme.getAccentColor() + "Sample Text",
            theme.getTextColor() + "Success Color: " + theme.getSuccessColor() + "Sample Text",
            theme.getTextColor() + "Warning Color: " + theme.getWarningColor() + "Sample Text",
            theme.getTextColor() + "Error Color: " + theme.getErrorColor() + "Sample Text",
            "",
            theme.getSubtitleColor() + "This shows how text will appear",
            theme.getSubtitleColor() + "with the currently selected theme."
        );
        inventory.setItem(PREVIEW_SLOT, preview);
    }

    public void open() {
        UITheme theme = themeManager.getPlayerTheme(player);
        themeManager.playSound(player, theme.getOpenSound());
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;

        UITheme currentTheme = themeManager.getPlayerTheme(player);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getSlot();
        
        // Handle theme selection
        for (int i = 0; i < THEME_SLOTS.length; i++) {
            if (slot == THEME_SLOTS[i]) {
                List<UITheme> themes = new ArrayList<>(themeManager.getAllThemes());
                if (i < themes.size()) {
                    UITheme selectedTheme = themes.get(i);
                    
                    // Apply the theme
                    themeManager.setPlayerTheme(player, selectedTheme.getName());
                    themeManager.playSound(player, selectedTheme.getSuccessSound());
                    
                    player.sendMessage(selectedTheme.getSuccessColor() + "✓ Theme changed to " + 
                        selectedTheme.getDisplayName() + selectedTheme.getSuccessColor() + "!");
                    player.sendMessage(selectedTheme.getSubtitleColor() + "Your mailbox will now use this theme.");
                    
                    // Update the GUI with the new theme
                    updateInventory();
                }
                return;
            }
        }
        
        // Handle control buttons
        switch (slot) {
            case BACK_SLOT:
                themeManager.playSound(player, currentTheme.getClickSound());
                clicker.closeInventory();
                new EnhancedMailboxGUI(plugin, player).open();
                break;
                
            case APPLY_SLOT:
                themeManager.playSound(player, currentTheme.getClickSound());
                clicker.sendMessage(currentTheme.getAccentColor() + "Theme settings are applied instantly when you click a theme!");
                break;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        
        UITheme theme = themeManager.getPlayerTheme(player);
        themeManager.playSound(player, theme.getCloseSound());
        
        // Unregister this listener to prevent memory leaks
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
    }
}