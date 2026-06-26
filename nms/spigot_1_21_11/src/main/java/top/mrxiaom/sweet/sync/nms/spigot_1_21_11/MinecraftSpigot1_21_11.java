package top.mrxiaom.sweet.sync.nms.spigot_1_21_11;

import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.WorldNBTStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.sync.nms.IMinecraft;
import top.mrxiaom.sweet.sync.nms.IPlayerData;

import java.util.Optional;
import java.util.UUID;

public class MinecraftSpigot1_21_11 implements IMinecraft {
    private final MinecraftServer server;
    @SuppressWarnings("deprecation")
    public MinecraftSpigot1_21_11() {
        this.server = DedicatedServer.getServer();
    }

    @Override
    public @NotNull IPlayerData getPlayerData(@NotNull UUID uuid, @Nullable String name) {
        WorldNBTStorage playerIo = server.h;
        EntityPlayer onlinePlayer = server.aj().b(uuid);
        if (onlinePlayer != null) {
            return new PlayerDataOnline(playerIo, onlinePlayer);
        } else {
            String playerName = name == null ? "" : name;
            Optional<NBTTagCompound> loaded = playerIo.a(new NameAndId(uuid, playerName));
            NBTTagCompound compoundTag = loaded.orElseGet(() -> {
                WorldServer overworld = server.N();
                WorldData.a respawnData = overworld.C();
                WorldServer dimension = server.a(respawnData.a());
                WorldServer spawnWorld = dimension == null ? overworld : dimension;

                NBTTagCompound data = new NBTTagCompound();

                data.a("WorldUUIDMost", spawnWorld.uuid.getMostSignificantBits());
                data.a("WorldUUIDLeast", spawnWorld.uuid.getLeastSignificantBits());

                NBTTagList position = new NBTTagList();

                position.add(NBTTagDouble.a(respawnData.b().u()));
                position.add(NBTTagDouble.a(respawnData.b().v()));
                position.add(NBTTagDouble.a(respawnData.b().w()));
                data.a("Pos", position);

                NBTTagList rotation = new NBTTagList();
                rotation.add(NBTTagFloat.a(respawnData.d()));
                rotation.add(NBTTagFloat.a(respawnData.e()));
                data.a("Rotation", rotation);

                return data;
            });
            return new PlayerDataOffline(playerIo, uuid, compoundTag);
        }
    }
}
