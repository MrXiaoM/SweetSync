package top.mrxiaom.sweet.sync.database;

import top.mrxiaom.pluginbase.database.IDatabase;
import top.mrxiaom.sweet.sync.SweetSync;

import java.sql.Connection;
import java.sql.SQLException;

public class EnderChestDatabase extends AbstractByteArrayDatabase implements IDatabase {
    public EnderChestDatabase(SweetSync plugin) {
        super(plugin, "player_ender_chest");
        register();
    }

    @Override
    public void reload(Connection conn, String tablePrefix) throws SQLException {
        createTable(conn, tablePrefix);
    }
}
