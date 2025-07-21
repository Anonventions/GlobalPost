package org.anonventions.globalPost.models;

import org.bukkit.inventory.ItemStack;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class Mail {

    private int id;
    private UUID senderUUID;
    private String senderName;
    private UUID recipientUUID;
    private String recipientName;
    private String sourceServer;
    private String destinationServer;
    private List<ItemStack> items;
    private String message;
    private Timestamp sentAt;
    private boolean collected;
    private Timestamp collectedAt;

    public Mail() {}

    public Mail(UUID senderUUID, String senderName, UUID recipientUUID, String recipientName,
                String sourceServer, String destinationServer, List<ItemStack> items, String message) {
        this.senderUUID = senderUUID;
        this.senderName = senderName;
        this.recipientUUID = recipientUUID;
        this.recipientName = recipientName;
        this.sourceServer = sourceServer;
        this.destinationServer = destinationServer;
        this.items = items;
        this.message = message;
        this.collected = false;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public UUID getSenderUUID() { return senderUUID; }
    public void setSenderUUID(UUID senderUUID) { this.senderUUID = senderUUID; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public UUID getRecipientUUID() { return recipientUUID; }
    public void setRecipientUUID(UUID recipientUUID) { this.recipientUUID = recipientUUID; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getSourceServer() { return sourceServer; }
    public void setSourceServer(String sourceServer) { this.sourceServer = sourceServer; }

    public String getDestinationServer() { return destinationServer; }
    public void setDestinationServer(String destinationServer) { this.destinationServer = destinationServer; }

    public List<ItemStack> getItems() { return items; }
    public void setItems(List<ItemStack> items) { this.items = items; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Timestamp getSentAt() { return sentAt; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }

    public boolean isCollected() { return collected; }
    public void setCollected(boolean collected) { this.collected = collected; }

    public Timestamp getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Timestamp collectedAt) { this.collectedAt = collectedAt; }
}