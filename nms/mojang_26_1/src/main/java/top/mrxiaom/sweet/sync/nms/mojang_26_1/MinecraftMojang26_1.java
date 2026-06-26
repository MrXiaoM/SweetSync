package top.mrxiaom.sweet.sync.nms.mojang_26_1;

import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.sync.nms.IMinecraft;
import top.mrxiaom.sweet.sync.nms.IPlayerData;

import java.util.Optional;
import java.util.UUID;

public class MinecraftMojang26_1 implements IMinecraft {
    private final MinecraftServer server;
    public MinecraftMojang26_1() {
        this.server = ((CraftServer) Bukkit.getServer()).getServer();
    }

    @Override
    public @NotNull IPlayerData getPlayerData(@NotNull UUID uuid, @Nullable String name) {
        PlayerDataStorage playerIo = server.playerDataStorage;
        ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(uuid);
        if (onlinePlayer != null) {
            return new PlayerDataOnline(playerIo, onlinePlayer);
        } else {
            String playerName = name == null ? "" : name;
            Optional<CompoundTag> loaded = playerIo.load(new NameAndId(uuid, playerName));
            CompoundTag compoundTag = loaded.orElseGet(() -> {
                ServerLevel overworld = server.overworld();
                LevelData.RespawnData respawnData = overworld.getRespawnData();
                ServerLevel dimension = server.getLevel(respawnData.dimension());
                ServerLevel spawnWorld = dimension == null ? overworld : dimension;

                CompoundTag data = new CompoundTag();

                data.putLong("WorldUUIDMost", spawnWorld.uuid.getMostSignificantBits());
                data.putLong("WorldUUIDLeast", spawnWorld.uuid.getLeastSignificantBits());

                ListTag position = new ListTag();
                position.add(DoubleTag.valueOf(respawnData.pos().getX()));
                position.add(DoubleTag.valueOf(respawnData.pos().getY()));
                position.add(DoubleTag.valueOf(respawnData.pos().getZ()));
                data.put("Pos", position);

                ListTag rotation = new ListTag();
                rotation.add(FloatTag.valueOf(respawnData.yaw()));
                rotation.add(FloatTag.valueOf(respawnData.pitch()));
                data.put("Rotation", rotation);

                return data;
            });
            return new PlayerDataOffline(playerIo, uuid, compoundTag);
        }
    }
}
