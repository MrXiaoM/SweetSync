package top.mrxiaom.sweet.sync.database;

import top.mrxiaom.pluginbase.database.IDatabase;
import top.mrxiaom.sweet.sync.SweetSync;

import java.sql.Connection;
import java.sql.SQLException;

public class InventoryDatabase extends AbstractByteArrayDatabase implements IDatabase {
    public InventoryDatabase(SweetSync plugin) {
        super(plugin, "player_inventory");
        register();
    }

    @Override
    public void reload(Connection conn, String tablePrefix) throws SQLException {
        createTable(conn, tablePrefix);
    }
}
