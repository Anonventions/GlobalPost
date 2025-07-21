package org.anonventions.globalPost.ui;

import org.bukkit.Material;
import org.bukkit.Sound;

/**
 * Represents a UI theme with customizable colors, materials, and effects
 */
public class UITheme {
    
    private final String name;
    private final String displayName;
    
    // Colors
    private final String primaryColor;
    private final String secondaryColor;
    private final String accentColor;
    private final String errorColor;
    private final String successColor;
    private final String warningColor;
    private final String textColor;
    private final String subtitleColor;
    
    // Materials
    private final Material borderMaterial;
    private final Material buttonMaterial;
    private final Material mailMaterial;
    private final Material noMailMaterial;
    private final Material nextPageMaterial;
    private final Material previousPageMaterial;
    private final Material refreshMaterial;
    private final Material sendMaterial;
    private final Material cancelMaterial;
    
    // Sounds
    private final Sound clickSound;
    private final Sound successSound;
    private final Sound errorSound;
    private final Sound openSound;
    private final Sound closeSound;
    
    public UITheme(String name, String displayName, 
                   String primaryColor, String secondaryColor, String accentColor,
                   String errorColor, String successColor, String warningColor,
                   String textColor, String subtitleColor,
                   Material borderMaterial, Material buttonMaterial, Material mailMaterial,
                   Material noMailMaterial, Material nextPageMaterial, Material previousPageMaterial,
                   Material refreshMaterial, Material sendMaterial, Material cancelMaterial,
                   Sound clickSound, Sound successSound, Sound errorSound, 
                   Sound openSound, Sound closeSound) {
        this.name = name;
        this.displayName = displayName;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.accentColor = accentColor;
        this.errorColor = errorColor;
        this.successColor = successColor;
        this.warningColor = warningColor;
        this.textColor = textColor;
        this.subtitleColor = subtitleColor;
        this.borderMaterial = borderMaterial;
        this.buttonMaterial = buttonMaterial;
        this.mailMaterial = mailMaterial;
        this.noMailMaterial = noMailMaterial;
        this.nextPageMaterial = nextPageMaterial;
        this.previousPageMaterial = previousPageMaterial;
        this.refreshMaterial = refreshMaterial;
        this.sendMaterial = sendMaterial;
        this.cancelMaterial = cancelMaterial;
        this.clickSound = clickSound;
        this.successSound = successSound;
        this.errorSound = errorSound;
        this.openSound = openSound;
        this.closeSound = closeSound;
    }
    
    // Getters
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getPrimaryColor() { return primaryColor; }
    public String getSecondaryColor() { return secondaryColor; }
    public String getAccentColor() { return accentColor; }
    public String getErrorColor() { return errorColor; }
    public String getSuccessColor() { return successColor; }
    public String getWarningColor() { return warningColor; }
    public String getTextColor() { return textColor; }
    public String getSubtitleColor() { return subtitleColor; }
    public Material getBorderMaterial() { return borderMaterial; }
    public Material getButtonMaterial() { return buttonMaterial; }
    public Material getMailMaterial() { return mailMaterial; }
    public Material getNoMailMaterial() { return noMailMaterial; }
    public Material getNextPageMaterial() { return nextPageMaterial; }
    public Material getPreviousPageMaterial() { return previousPageMaterial; }
    public Material getRefreshMaterial() { return refreshMaterial; }
    public Material getSendMaterial() { return sendMaterial; }
    public Material getCancelMaterial() { return cancelMaterial; }
    public Sound getClickSound() { return clickSound; }
    public Sound getSuccessSound() { return successSound; }
    public Sound getErrorSound() { return errorSound; }
    public Sound getOpenSound() { return openSound; }
    public Sound getCloseSound() { return closeSound; }
}