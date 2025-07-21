package org.anonventions.globalPost.messaging;

import org.anonventions.globalPost.GlobalPost;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PluginMessageHandler implements PluginMessageListener {

    private final GlobalPost plugin;

    public PluginMessageHandler(GlobalPost plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("globalpost:main")) {
            return;
        }

        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(message);
            DataInputStream in = new DataInputStream(stream);

            String subChannel = in.readUTF();

            switch (subChannel) {
                case "MailNotification":
                    handleMailNotification(in, player);
                    break;
                case "PlayerLookup":
                    handlePlayerLookup(in, player);
                    break;
                default:
                    plugin.getLogger().warning("Unknown plugin message subchannel: " + subChannel);
                    break;
            }

        } catch (IOException e) {
            plugin.getLogger().severe("Error handling plugin message: " + e.getMessage());
        }
    }

    private void handleMailNotification(DataInputStream in, Player player) throws IOException {
        String recipientName = in.readUTF();
        int mailCount = in.readInt();

        Player recipient = plugin.getServer().getPlayer(recipientName);
        if (recipient != null) {
            recipient.sendMessage("§6[Mail] §aYou have " + mailCount + " new mail(s)! Use /post to check.");
        }
    }

    private void handlePlayerLookup(DataInputStream in, Player player) throws IOException {
        String requestId = in.readUTF();
        String playerName = in.readUTF();

        Player target = plugin.getServer().getPlayer(playerName);
        boolean online = target != null;

        // Send response back through plugin messaging
        // This would be implemented based on your specific proxy setup
    }
}