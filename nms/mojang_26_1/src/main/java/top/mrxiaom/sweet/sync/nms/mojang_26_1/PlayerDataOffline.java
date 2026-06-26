package top.mrxiaom.sweet.sync.nms.mojang_26_1;

import net.minecraft.nbt.*;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.PlayerDataStorage;
import top.mrxiaom.sweet.sync.nms.IPlayerData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static top.mrxiaom.sweet.sync.nms.mojang_26_1.CodecHelper.*;

public class PlayerDataOffline implements IPlayerData {
    private final PlayerDataStorage playerIo;
    private final UUID uuid;
    private final CompoundTag player;
    public PlayerDataOffline(PlayerDataStorage playerIo, UUID uuid, CompoundTag player) {
        this.playerIo = playerIo;
        this.uuid = uuid;
        this.player = player;
    }

    @Override
    public byte[] dumpEquipments() throws IOException {
        CompoundTag equipment = player.getCompoundOrEmpty("equipment");
        return dumpTag(equipment);
    }

    @Override
    public void restoreEquipments(byte[] byteArray) throws IOException {
        restoreTag(byteArray)
                .asCompound()
                .ifPresent(compoundTag -> player.put("equipment", compoundTag));
    }

    @Override
    public byte[] dumpInventory() throws IOException {
        ListTag inventory = player.getListOrEmpty("Inventory");
        return dumpTag(inventory);
    }

    @Override
    public void restoreInventory(byte[] byteArray) throws IOException {
        restoreTag(byteArray)
                .asList()
                .ifPresent(listTag -> player.put("Inventory", listTag));
    }

    @Override
    public byte[] dumpEnderChest() throws IOException {
        ListTag enderChest = player.getListOrEmpty("EnderItems");
        return dumpTag(enderChest);
    }

    @Override
    public void restoreEnderChest(byte[] byteArray) throws IOException {
        restoreTag(byteArray)
                .asList()
                .ifPresent(listTag -> player.put("EnderItems", listTag));
    }

    /**
     * @see net.minecraft.world.level.storage.PlayerDataStorage#save(net.minecraft.world.entity.player.Player)
     */
    @Override
    public void save() throws IOException {
        String stringUUID = uuid.toString();
        Path path = playerIo.getPlayerDir().toPath();
        Path path1 = Files.createTempFile(path, stringUUID + "-", ".dat");
        NbtIo.writeCompressed(player, path1);
        Path path2 = path.resolve(stringUUID + ".dat");
        Path path3 = path.resolve(stringUUID + ".dat_old");
        Util.safeReplaceFile(path2, path1, path3);
    }
}
