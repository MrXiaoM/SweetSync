package top.mrxiaom.sweet.sync.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.sync.database.AbstractByteArrayDatabase;
import top.mrxiaom.sweet.sync.nms.IPlayerData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class SyncDumpContext {
    private final @NotNull UUID uuid;
    private final @Nullable Player player;
    private final Connection connection;
    private final IPlayerData data;

    @ApiStatus.Internal
    public SyncDumpContext(@NotNull UUID uuid, @Nullable Player player, Connection connection, IPlayerData data) {
        this.uuid = uuid;
        this.player = player;
        this.connection = connection;
        this.data = data;
    }

    public @NotNull UUID uuid() {
        return uuid;
    }

    public @Nullable Player player() {
        return player;
    }

    public Connection connection() {
        return connection;
    }

    public void pushByteArray(AbstractByteArrayDatabase database, byte[] byteArray) throws SQLException {
        database.push(connection(), uuid(), byteArray);
    }

    public byte @Nullable [] pullByteArray(AbstractByteArrayDatabase database) throws SQLException {
        return database.pull(connection(), uuid());
    }

    public IPlayerData data() {
        return data;
    }
}
