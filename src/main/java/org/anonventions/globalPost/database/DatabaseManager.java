/*─────────────────────────────────────────────────────────────────────────────
 *  org/anonventions/globalPost/database/DatabaseManager.java
 *───────────────────────────────────────────────────────────────────────────*/
package org.anonventions.globalPost.database;

import org.anonventions.globalPost.GlobalPost;
import org.anonventions.globalPost.models.Mail;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Handles all DB I/O (SQLite or MySQL). <br>
 * WHERE clauses use LOWER() for destination_server so canonical names work
 * even on legacy rows.
 */
public class DatabaseManager {

    private final GlobalPost plugin;
    private       Connection connection;

    public DatabaseManager(GlobalPost plugin) { this.plugin = plugin; }

    /*------------------------------------------------------------------------*/
    public boolean initialize() {
        try {
            if ("mysql".equalsIgnoreCase(plugin.getConfigManager().getDatabaseType()))
                initializeMySQL();
            else initializeSQLite();

            createTables();
            return true;
        }
        catch (SQLException ex) {
            plugin.getLogger().severe("DB init failed: " + ex.getMessage());
            return false;
        }
    }

    private void initializeSQLite() throws SQLException {
        File data = plugin.getDataFolder();
        if (!data.exists()) data.mkdirs();
        String url = "jdbc:sqlite:" + data + "/" + plugin.getConfigManager().getSQLiteFile();
        connection = DriverManager.getConnection(url);
    }

    private void initializeMySQL() throws SQLException {
        String h = plugin.getConfigManager().getMySQLHost();
        int    p = plugin.getConfigManager().getMySQLPort();
        String d = plugin.getConfigManager().getMySQLDatabase();
        String u = plugin.getConfigManager().getMySQLUsername();
        String pw= plugin.getConfigManager().getMySQLPassword();

        String url = "jdbc:mysql://" + h + ":" + p + "/" + d + "?useSSL=false&autoReconnect=true";
        connection = DriverManager.getConnection(url, u, pw);
    }

    /*------------------------------------------------------------------------*/
    private void createTables() throws SQLException {
        String auto = "mysql".equalsIgnoreCase(plugin.getConfigManager().getDatabaseType()) ? "AUTO_INCREMENT" : "AUTOINCREMENT";

        String sql = """
            CREATE TABLE IF NOT EXISTS mails (
              id INTEGER PRIMARY KEY %s,
              sender_uuid        VARCHAR(36) NOT NULL,
              sender_name        VARCHAR(16) NOT NULL,
              recipient_uuid     VARCHAR(36) NOT NULL,
              recipient_name     VARCHAR(16) NOT NULL,
              source_server      VARCHAR(32) NOT NULL,
              destination_server VARCHAR(32) NOT NULL,
              items              TEXT        NOT NULL,
              message            TEXT,
              sent_at            TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
              collected          BOOLEAN     DEFAULT 0,
              collected_at       TIMESTAMP   NULL
            )""".formatted(auto);

        try (Statement st = connection.createStatement()) { st.execute(sql); }
    }

    /*------------------------------------------------------------------------*/
    public CompletableFuture<Boolean> saveMail(Mail mail) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                INSERT INTO mails (sender_uuid,sender_name,recipient_uuid,recipient_name,
                                   source_server,destination_server,items,message)
                VALUES (?,?,?,?,?,?,?,?)
            """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, mail.getSenderUUID().toString());
                ps.setString(2, mail.getSenderName());
                ps.setString(3, mail.getRecipientUUID().toString());
                ps.setString(4, mail.getRecipientName());
                ps.setString(5, mail.getSourceServer());
                ps.setString(6, mail.getDestinationServer());
                ps.setString(7, ItemSerializer.serializeItems(mail.getItems()));
                ps.setString(8, mail.getMessage());
                return ps.executeUpdate() > 0;
            }
            catch (SQLException ex) {
                plugin.getLogger().severe("saveMail: " + ex);
                return false;
            }
        });
    }

    /*------------------------------------------------------------------------*/
    public CompletableFuture<List<Mail>> getUnreadMails(UUID uuid, String serverCanonical) {
        return CompletableFuture.supplyAsync(() -> {

            String sql = """
                SELECT * FROM mails
                WHERE recipient_uuid = ? AND LOWER(destination_server) = ? AND collected = 0
                ORDER BY sent_at
            """;
            List<Mail> list = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, serverCanonical);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) list.add(rowToMail(rs));
            }
            catch (SQLException ex) { plugin.getLogger().severe("getUnreadMails: " + ex); }
            return list;
        });
    }

    /*------------------------------------------------------------------------*/
    public CompletableFuture<Boolean> markMailAsCollected(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE mails SET collected = 1, collected_at = CURRENT_TIMESTAMP WHERE id = ?")) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
            catch (SQLException ex) { plugin.getLogger().severe("markCollected: " + ex); return false; }
        });
    }

    /*------------------------------------------------------------------------*/
    public CompletableFuture<Integer> getMailCount(UUID uuid, String serverCanonical) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT COUNT(*) FROM mails WHERE recipient_uuid = ? AND LOWER(destination_server) = ? AND collected = 0")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, serverCanonical);
                ResultSet rs = ps.executeQuery();
                return rs.next() ? rs.getInt(1) : 0;
            }
            catch (SQLException ex) { plugin.getLogger().severe("getMailCount: " + ex); return 0; }
        });
    }

    /*------------------------------------------------------------------------*/
    private Mail rowToMail(ResultSet rs) throws SQLException {
        Mail m = new Mail();
        m.setId(rs.getInt("id"));
        m.setSenderUUID(UUID.fromString(rs.getString("sender_uuid")));
        m.setSenderName(rs.getString("sender_name"));
        m.setRecipientUUID(UUID.fromString(rs.getString("recipient_uuid")));
        m.setRecipientName(rs.getString("recipient_name"));
        m.setSourceServer(rs.getString("source_server"));
        m.setDestinationServer(rs.getString("destination_server"));
        m.setItems(ItemSerializer.deserializeItems(rs.getString("items")));
        m.setMessage(rs.getString("message"));
        m.setSentAt(rs.getTimestamp("sent_at"));
        m.setCollected(rs.getBoolean("collected"));
        m.setCollectedAt(rs.getTimestamp("collected_at"));
        return m;
    }

    /*------------------------------------------------------------------------*/
    public void close() {
        if (connection != null) try { connection.close(); }
        catch (SQLException ex) { plugin.getLogger().severe("DB close: " + ex); }
    }
}
