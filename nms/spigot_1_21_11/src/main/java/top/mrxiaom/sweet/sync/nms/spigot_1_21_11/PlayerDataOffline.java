package top.mrxiaom.sweet.sync.nms.spigot_1_21_11;

import net.minecraft.nbt.*;
import net.minecraft.util.SystemUtils;
import net.minecraft.world.level.storage.WorldNBTStorage;
import top.mrxiaom.sweet.sync.nms.IPlayerData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static top.mrxiaom.sweet.sync.nms.spigot_1_21_11.CodecHelper.*;

public class PlayerDataOffline implements IPlayerData {
    private final WorldNBTStorage playerIo;
    private final UUID uuid;
    private final NBTTagCompound player;
    public PlayerDataOffline(WorldNBTStorage playerIo, UUID uuid, NBTTagCompound player) {
        this.playerIo = playerIo;
        this.uuid = uuid;
        this.player = player;
    }

    @Override
    public byte[] dumpEquipments() throws IOException {
        NBTTagCompound equipment = player.n("equipment");
        return dumpTag(equipment);
    }

    @Override
    public void restoreEquipments(byte[] byteArray) throws IOException {
        restoreTag(byteArray)
                .s_()
                .ifPresent(compoundTag -> player.a("equipment", compoundTag));
    }

    @Override
    public byte[] dumpInventory() throws IOException {
        NBTTagList inventory = player.p("Inventory");
        return dumpTag(inventory);
    }

    @Override
    public void restoreInventory(byte[] byteArray) throws IOException {
        restoreTag(byteArray)
                .t_()
                .ifPresent(listTag -> player.a("Inventory", listTag));
    }

    @Override
    public byte[] dumpEnderChest() throws IOException {
        NBTTagList enderChest = player.p("EnderItems");
        return dumpTag(enderChest);
    }

    @Override
    public void restoreEnderChest(byte[] byteArray) throws IOException {
        restoreTag(byteArray)
                .t_()
                .ifPresent(listTag -> player.a("EnderItems", listTag));
    }

    /**
     * @see net.minecraft.world.level.storage.WorldNBTStorage#a(net.minecraft.world.entity.player.EntityHuman)
     */
    @Override
    public void save() throws IOException {
        String stringUUID = uuid.toString();
        Path path = playerIo.getPlayerDir().toPath();
        Path path1 = Files.createTempFile(path, stringUUID + "-", ".dat");
        NBTCompressedStreamTools.a(player, path1);
        Path path2 = path.resolve(stringUUID + ".dat");
        Path path3 = path.resolve(stringUUID + ".dat_old");
        SystemUtils.a(path2, path1, path3);
    }
}
