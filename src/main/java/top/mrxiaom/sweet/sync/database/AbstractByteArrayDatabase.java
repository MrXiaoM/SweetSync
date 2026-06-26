package top.mrxiaom.sweet.sync.database;

import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.sync.SweetSync;
import top.mrxiaom.sweet.sync.func.AbstractPluginHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public abstract class AbstractByteArrayDatabase extends AbstractPluginHolder {
    protected String TABLE_NAME;
    private final String tableNameTail;
    protected AbstractByteArrayDatabase(SweetSync plugin, String tableNameTail) {
        super(plugin);
        this.tableNameTail = tableNameTail;
    }

    public void createTable(Connection conn, String tablePrefix) throws SQLException {
        TABLE_NAME = tablePrefix + tableNameTail;
        String BLOB_TYPE = plugin.options.database().isMySQL()
                ? "VARBINARY(16000)" // 16KB
                : "BLOB";
        try (PreparedStatement ps = conn.prepareStatement(
                "CREATE TABLE if NOT EXISTS `" + TABLE_NAME + "`(" +
                        "`player_id` VARCHAR(36) PRIMARY KEY," +
                        "`content` " + BLOB_TYPE +
                        ");"
        )) {
            ps.execute();
        }
    }

    public void push(Connection conn, UUID playerId, byte[] content) throws SQLException {
        String tail = plugin.options.database().isMySQL()
                ? "ON DUPLICATE KEY UPDATE `content`=VALUES(`content`)"
                : "ON CONFLICT(`player_id`) DO UPDATE SET `content`=excluded.content";
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO `" + TABLE_NAME + "` (`player_id`,`content`) VALUES(?, ?) " + tail + ";"
        )) {
            ps.setString(1, playerId.toString());
            ps.setBytes(2, content);
            ps.executeUpdate();
        }
    }

    public byte @Nullable [] pull(Connection conn, UUID playerId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM `" + TABLE_NAME + "` WHERE `player_id`=?"
        )) {
            ps.setString(1, playerId.toString());
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBytes("content");
                }
            }
        }
        return null;
    }
}
