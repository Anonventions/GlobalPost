package org.anonventions.globalPost.database;

import org.anonventions.globalPost.GlobalPost;
import org.anonventions.globalPost.models.Mail;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final GlobalPost plugin;
    private Connection connection;

    public DatabaseManager(GlobalPost plugin) {
        this.plugin = plugin;
    }

    public boolean initialize() {
        try {
            if (plugin.getConfigManager().getDatabaseType().equalsIgnoreCase("mysql")) {
                initializeMySQL();
            } else {
                initializeSQLite();
            }

            createTables();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            return false;
        }
    }

    private void initializeSQLite() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String url = "jdbc:sqlite:" + dataFolder + "/" + plugin.getConfigManager().getSQLiteFile();
        connection = DriverManager.getConnection(url);
    }

    private void initializeMySQL() throws SQLException {
        String host = plugin.getConfigManager().getMySQLHost();
        int port = plugin.getConfigManager().getMySQLPort();
        String database = plugin.getConfigManager().getMySQLDatabase();
        String username = plugin.getConfigManager().getMySQLUsername();
        String password = plugin.getConfigManager().getMySQLPassword();

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
        connection = DriverManager.getConnection(url, username, password);
    }

    private void createTables() throws SQLException {
        String createMailsTable = """
            CREATE TABLE IF NOT EXISTS mails (
                id INTEGER PRIMARY KEY %s,
                sender_uuid VARCHAR(36) NOT NULL,
                sender_name VARCHAR(16) NOT NULL,
                recipient_uuid VARCHAR(36) NOT NULL,
                recipient_name VARCHAR(16) NOT NULL,
                source_server VARCHAR(32) NOT NULL,
                destination_server VARCHAR(32) NOT NULL,
                items TEXT NOT NULL,
                message TEXT,
                sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                collected BOOLEAN DEFAULT FALSE,
                collected_at TIMESTAMP NULL
            )
        """.formatted(plugin.getConfigManager().getDatabaseType().equalsIgnoreCase("mysql") ? "AUTO_INCREMENT" : "AUTOINCREMENT");

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createMailsTable);
        }
    }

    public CompletableFuture<Boolean> saveMail(Mail mail) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                INSERT INTO mails (sender_uuid, sender_name, recipient_uuid, recipient_name, 
                                 source_server, destination_server, items, message) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, mail.getSenderUUID().toString());
                stmt.setString(2, mail.getSenderName());
                stmt.setString(3, mail.getRecipientUUID().toString());
                stmt.setString(4, mail.getRecipientName());
                stmt.setString(5, mail.getSourceServer());
                stmt.setString(6, mail.getDestinationServer());
                stmt.setString(7, ItemSerializer.serializeItems(mail.getItems()));
                stmt.setString(8, mail.getMessage());

                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save mail: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<List<Mail>> getUnreadMails(UUID playerUUID, String serverName) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                SELECT * FROM mails 
                WHERE recipient_uuid = ? AND destination_server = ? AND collected = FALSE
                ORDER BY sent_at ASC
            """;

            List<Mail> mails = new ArrayList<>();

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUUID.toString());
                stmt.setString(2, serverName);

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Mail mail = new Mail();
                    mail.setId(rs.getInt("id"));
                    mail.setSenderUUID(UUID.fromString(rs.getString("sender_uuid")));
                    mail.setSenderName(rs.getString("sender_name"));
                    mail.setRecipientUUID(UUID.fromString(rs.getString("recipient_uuid")));
                    mail.setRecipientName(rs.getString("recipient_name"));
                    mail.setSourceServer(rs.getString("source_server"));
                    mail.setDestinationServer(rs.getString("destination_server"));
                    mail.setItems(ItemSerializer.deserializeItems(rs.getString("items")));
                    mail.setMessage(rs.getString("message"));
                    mail.setSentAt(rs.getTimestamp("sent_at"));
                    mail.setCollected(rs.getBoolean("collected"));

                    mails.add(mail);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get unread mails: " + e.getMessage());
            }

            return mails;
        });
    }

    public CompletableFuture<Boolean> markMailAsCollected(int mailId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE mails SET collected = TRUE, collected_at = CURRENT_TIMESTAMP WHERE id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, mailId);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to mark mail as collected: " + e.getMessage());
                return false;
            }
        });
    }

    public CompletableFuture<Integer> getMailCount(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM mails WHERE recipient_uuid = ? AND collected = FALSE";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUUID.toString());

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get mail count: " + e.getMessage());
            }

            return 0;
        });
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
            }
        }
    }
}