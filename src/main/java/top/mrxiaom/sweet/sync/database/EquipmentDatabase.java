package top.mrxiaom.sweet.sync.database;

import top.mrxiaom.pluginbase.database.IDatabase;
import top.mrxiaom.sweet.sync.SweetSync;

import java.sql.Connection;
import java.sql.SQLException;

public class EquipmentDatabase extends AbstractByteArrayDatabase implements IDatabase {
    public EquipmentDatabase(SweetSync plugin) {
        super(plugin, "player_equipment");
        register();
    }

    @Override
    public void reload(Connection conn, String tablePrefix) throws SQLException {
        createTable(conn, tablePrefix);
    }
}
