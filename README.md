# GlobalPost 📮

[![Version](https://img.shields.io/badge/version-1.0-blue.svg)](https://github.com/Anonventions/GlobalPost/releases)
[![Minecraft](https://img.shields.io/badge/minecraft-1.20.1-green.svg)](https://www.minecraft.net/)
[![License](https://img.shields.io/badge/license-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17+-orange.svg)](https://www.oracle.com/java/)

A comprehensive cross-server mailbox system for Minecraft servers running under a Velocity proxy. Send items between servers with ease!

## ✨ Features

- 🌐 **Cross-Server Mail**: Send items between different servers in your network
- 📦 **Item Blacklist**: Prevent specific items from being sent through mail
- 💾 **Persistent Storage**: Mail is stored in database even when players are offline
- 🎨 **GUI Interface**: Beautiful inventory-based mail interface
- ⚡ **Async Operations**: All database operations are asynchronous to prevent lag
- 🔧 **Configurable Channels**: Set up one-way or two-way mail channels between servers
- 🔒 **Permission System**: Control who can send and receive mail
- 📊 **Database Support**: Both SQLite and MySQL supported
- 🔄 **Hot Reload**: Reload configuration without restarting

## 📋 Requirements

- **Minecraft Server**: Paper/Spigot 1.20.1+
- **Java**: Java 17 or higher
- **Proxy**: Velocity proxy (for cross-server functionality)
- **Database**: SQLite (included) or MySQL server

## 🚀 Installation

1. Download the latest `GlobalPost.jar` from the [releases page](https://github.com/Anonventions/GlobalPost/releases)
2. Place the plugin in the `plugins` folder of **each server** in your network
3. Start your servers to generate the configuration files
4. Configure the plugin (see configuration section below)
5. Restart your servers or use `/post reload`

## ⚙️ Configuration

The plugin creates a `config.yml` file in the `plugins/GlobalPost/` folder. Here's a detailed breakdown of each section:

### 🗄️ Database Configuration

```yaml
database:
  type: sqlite  # Options: sqlite, mysql
  sqlite:
    file: globalpost.db  # SQLite database file name
  mysql:
    host: localhost      # MySQL server host
    port: 3306          # MySQL server port
    database: globalpost # MySQL database name
    username: root       # MySQL username
    password: password   # MySQL password
```

**Database Type Options:**
- **`sqlite`**: Simple file-based database (recommended for small networks)
- **`mysql`**: MySQL database server (recommended for large networks)

**SQLite Configuration:**
- `file`: Name of the SQLite database file (created automatically)

**MySQL Configuration:**
- `host`: IP address or hostname of your MySQL server
- `port`: Port number (usually 3306)
- `database`: Name of the MySQL database to use
- `username`: MySQL user with read/write permissions
- `password`: Password for the MySQL user

### 🖥️ Server Identification

```yaml
server:
  name: server1  # Unique name for this server
```

**Important Notes:**
- Each server must have a **unique name**
- Use descriptive names like `survival`, `creative`, `skyblock`, etc.
- This name is used to identify the server in the mail system

### 🔗 Channel Configuration

```yaml
channels:
  server1:        # From server1
    - server2     # Can send to server2
    - server3     # Can send to server3
  server2:        # From server2
    - server1     # Can send to server1
  server3:        # From server3
    - server1     # Can send to server1
```

**Channel Setup Examples:**

**Two-way communication between all servers:**
```yaml
channels:
  survival:
    - creative
    - skyblock
  creative:
    - survival
    - skyblock
  skyblock:
    - survival
    - creative
```

**One-way communication (hub system):**
```yaml
channels:
  hub:
    - survival
    - creative
    - skyblock
  survival:
    - hub
  creative:
    - hub
  skyblock:
    - hub
```

**Restricted communication:**
```yaml
channels:
  survival:
    - creative      # Survival can only send to creative
  creative:
    - survival      # Creative can only send to survival
  # skyblock has no channels (isolated)
```

### 🚫 Item Blacklist

```yaml
blacklist:
  items:
    - SHULKER_BOX
    - WHITE_SHULKER_BOX
    - ENCHANTED_BOOK
    - BEDROCK
    - COMMAND_BLOCK
    - BARRIER
```

**How to configure:**
- Use exact Minecraft material names (in UPPERCASE)
- Find material names at: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html
- Add any item you want to prevent from being sent through mail

**Common blacklist items:**
- All shulker boxes (to prevent item duplication)
- Enchanted books (if you want to restrict powerful enchantments)
- Creative-only items (bedrock, barriers, command blocks)
- Valuable items (netherite, diamonds) - if desired

### 🎛️ Mail System Settings

```yaml
settings:
  max_items_per_mail: 27      # Maximum items per mail (1-54)
  max_mails_per_player: 50    # Maximum unread mails per player
  mail_expiry_days: 30        # Days before mail expires (0 = never)
```

**Setting Explanations:**

- **`max_items_per_mail`**: 
  - Controls how many items can be sent in a single mail
  - Range: 1-54 (54 = full double chest)
  - Recommended: 27 (single chest)

- **`max_mails_per_player`**: 
  - Prevents mailbox spam
  - When limit is reached, oldest mail is deleted
  - Set to 0 for unlimited

- **`mail_expiry_days`**: 
  - Automatically deletes old mail
  - Set to 0 to disable expiry
  - Recommended: 30 days

## 🎮 Usage

### 📝 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/post` | Open your mailbox GUI | `globalpost.use` |
| `/post send <server> [player]` | Send mail to specific server/player | `globalpost.send` |
| `/post check` | Check your unread mail count | `globalpost.use` |
| `/post reload` | Reload plugin configuration | `globalpost.admin` |

### 🖱️ GUI Interface

**Mailbox GUI (`/post`):**
- Click on mail items to collect them
- Use the "Send Mail" button to open the send GUI
- Use the "Refresh" button to update your mailbox

**Send Mail GUI (`/post send <server>`):**
- Place items in the grid to send them
- Click "Send Mail" to confirm sending
- Click "Cancel" to return items to your inventory

### 👥 Sending Mail

**To yourself on another server:**
```
/post send creative
```

**To another player on another server:**
```
/post send survival PlayerName
```

**Using the GUI:**
1. Use `/post send <server> [player]` to open the send GUI
2. Place items in the inventory slots
3. Click the green "Send Mail" button
4. Items will be sent to the destination server

### 📬 Receiving Mail

**Automatic notifications:**
- Players receive notifications when they join if they have unread mail
- Notifications show the number of unread mails

**Collecting mail:**
1. Use `/post` to open your mailbox
2. Click on mail items to collect them
3. Items are automatically added to your inventory
4. Mail is marked as collected and removed from your mailbox

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `globalpost.use` | Access to basic mail commands | `true` |
| `globalpost.send` | Permission to send mail | `true` |
| `globalpost.admin` | Access to admin commands | `op` |

### Permission Examples

**For regular players:**
```yaml
permissions:
  globalpost.use: true
  globalpost.send: true
```

**For staff members:**
```yaml
permissions:
  globalpost.use: true
  globalpost.send: true
  globalpost.admin: true
```

**To restrict mail sending:**
```yaml
permissions:
  globalpost.use: true
  globalpost.send: false  # Can receive but not send
```

## 🔧 Advanced Configuration

### 🌐 Velocity Proxy Setup

1. Ensure all servers are properly connected to your Velocity proxy
2. Configure plugin messaging channels in Velocity's `velocity.toml`:

```toml
[servers]
survival = "127.0.0.1:25566"
creative = "127.0.0.1:25567"
skyblock = "127.0.0.1:25568"
```

### 📊 Database Optimization

**For MySQL:**
```sql
CREATE DATABASE globalpost;
CREATE USER 'globalpost'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON globalpost.* TO 'globalpost'@'%';
FLUSH PRIVILEGES;
```

**For large networks:**
- Use MySQL instead of SQLite
- Configure database connection pooling
- Regular database maintenance and cleanup

### 🔄 Hot Reload

The plugin supports hot reloading of configuration:
```
/post reload
```

This reloads:
- Configuration file
- Item blacklist
- Server channels
- Mail settings

## 🚨 Troubleshooting

### Common Issues

**"Invalid destination server" error:**
- Check that the destination server is listed in your `channels` configuration
- Ensure server names match exactly (case-sensitive)
- Verify the destination server is online and has the plugin installed

**Database connection errors:**
- For MySQL: Check host, port, username, and password
- Ensure the database exists and user has proper permissions
- Check firewall settings

**Items not being sent:**
- Verify items are not on the blacklist
- Check that player has `globalpost.send` permission
- Ensure destination server is configured properly

**Mail not appearing:**
- Check that servers are connected to the same database
- Verify server names are configured correctly
- Try `/post reload` on both servers

### 📝 Logs

The plugin logs important information to the server console:
- Database connection status
- Mail sending/receiving events
- Configuration errors
- Player actions

Enable debug logging by setting log level to `DEBUG` in your server configuration.

## 🤝 Support

If you encounter any issues:

1. Check the [troubleshooting section](#-troubleshooting)
2. Review your configuration file
3. Check server logs for error messages
4. Create an issue on the [GitHub repository](https://github.com/Anonventions/GlobalPost/issues)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Credits

- **Developer**: [Anonventions](https://github.com/Anonventions)
- **Contributors**: [See contributors](https://github.com/Anonventions/GlobalPost/contributors)

---

**Made with ❤️ for the Minecraft community**
