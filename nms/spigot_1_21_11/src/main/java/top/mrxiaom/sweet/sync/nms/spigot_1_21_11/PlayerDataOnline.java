package top.mrxiaom.sweet.sync.nms.spigot_1_21_11;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.IInventory;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.WorldNBTStorage;
import top.mrxiaom.sweet.sync.nms.IPlayerData;

import java.io.*;
import java.util.EnumMap;

import static top.mrxiaom.sweet.sync.nms.spigot_1_21_11.CodecHelper.*;

public class PlayerDataOnline implements IPlayerData {
    private final WorldNBTStorage playerIo;
    private final EntityPlayer player;
    public PlayerDataOnline(WorldNBTStorage playerIo, EntityPlayer player) {
        this.playerIo = playerIo;
        this.player = player;
    }

    private byte[] dumpContainer(IInventory container) throws IOException {
        return dumpContainer(container, container.b());
    }

    private byte[] dumpContainer(IInventory container, int invSize) throws IOException {
        NBTTagList list = new NBTTagList();
        RegistryOps<NBTBase> ops = RegistryOps.a(DynamicOpsNBT.a, player.A().J_());
        for (int i = 0; i < invSize; ++i) {
            ItemStack itemstack = container.a(i);
            if (!itemstack.f()) {
                ItemStackWithSlot slot = new ItemStackWithSlot(i, itemstack);
                serializeNbt(ItemStackWithSlot.a, ops, slot).ifPresent(list::add);
            }
        }
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(stream)
        ) {
            NBTCompressedStreamTools.a(list, data);
            return stream.toByteArray();
        }
    }

    private void restoreContainer(IInventory container, byte[] byteArray) throws IOException {
        restoreContainer(container, byteArray, container.b());
    }

    private void restoreContainer(IInventory container, byte[] byteArray, int invSize) throws IOException {
        NBTTagList list;
        try (ByteArrayInputStream stream =  new ByteArrayInputStream(byteArray);
             DataInputStream data = new DataInputStream(stream)
        ) {
            list = NBTCompressedStreamTools.b(data, NBTReadLimiter.c()).t_().orElseThrow();
        }
        container.a();
        RegistryOps<NBTBase> ops = RegistryOps.a(DynamicOpsNBT.a, player.A().J_());
        for (NBTBase tag : list) {
            deserializeNbt(ItemStackWithSlot.a, ops, tag).ifPresent(slot -> {
                if (slot.a() < invSize) {
                    container.a(slot.a(), slot.b());
                }
            });
        }
    }

    @Override
    public byte[] dumpEquipments() throws IOException {
        RegistryOps<NBTBase> ops = RegistryOps.a(DynamicOpsNBT.a, player.A().J_());
        NBTTagCompound equipment = new NBTTagCompound();
        for (EnumItemSlot slot : PlayerInventory.i.values()) {
            ItemStack item = player.a(slot);
            if (item != null && !item.f()) {
                String key = slot.c();
                serializeNbt(ItemStack.b, ops, item).ifPresent(tag -> equipment.a(key, tag));
            }
        }
        return dumpTag(equipment);
    }

    @Override
    public void restoreEquipments(byte[] byteArray) throws IOException {
        // 在 Paper 上，player.equipment 是 protected 的，所以通过 Inventory 去间接调用
        PlayerInventory inventory = player.gK();
        Int2ObjectMap<EnumItemSlot> map = PlayerInventory.i;
        map.forEach((i, slot) -> inventory.a(i, ItemStack.l));
        restoreTag(byteArray).s_().ifPresent(equipment -> {
            RegistryOps<NBTBase> ops = RegistryOps.a(DynamicOpsNBT.a, player.A().J_());
            EnumMap<EnumItemSlot, ItemStack> items = new EnumMap<>(EnumItemSlot.class);
            for (EnumItemSlot slot : EnumItemSlot.values()) {
                String key = slot.c();
                equipment.m(key)
                        .flatMap(tag -> deserializeNbt(ItemStack.b, ops, tag))
                        .ifPresent(item -> items.put(slot, item));
            }
            map.forEach((i, slot) -> {
                ItemStack item = items.get(slot);
                if (item != null && !item.f()) {
                    inventory.a(i, item);
                }
            });
        });
    }

    @Override
    public byte[] dumpInventory() throws IOException {
        PlayerInventory inventory = player.gK();
        int invSize = inventory.b() - PlayerInventory.i.size();
        return dumpContainer(inventory, invSize);
    }

    @Override
    public void restoreInventory(byte[] byteArray) throws IOException {
        PlayerInventory inventory = player.gK();
        int invSize = inventory.b() - PlayerInventory.i.size();
        restoreContainer(inventory, byteArray, invSize);
    }

    @Override
    public byte[] dumpEnderChest() throws IOException {
        return dumpContainer(player.gZ());
    }

    @Override
    public void restoreEnderChest(byte[] byteArray) throws IOException {
        restoreContainer(player.gZ(), byteArray);
    }

    @Override
    public void save() {
        playerIo.a(player);
    }
}
