package top.mrxiaom.sweet.sync.nms.mojang_26_1;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.PlayerDataStorage;
import top.mrxiaom.sweet.sync.nms.IPlayerData;

import java.io.*;
import java.util.EnumMap;

import static top.mrxiaom.sweet.sync.nms.mojang_26_1.CodecHelper.*;

public class PlayerDataOnline implements IPlayerData {
    private final PlayerDataStorage playerIo;
    private final ServerPlayer player;
    public PlayerDataOnline(PlayerDataStorage playerIo, ServerPlayer player) {
        this.playerIo = playerIo;
        this.player = player;
    }

    private byte[] dumpContainer(Container container) throws IOException {
        return dumpContainer(container, container.getContainerSize());
    }

    private byte[] dumpContainer(Container container, int invSize) throws IOException {
        ListTag list = new ListTag();
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, player.level().registryAccess());
        for (int i = 0; i < invSize; ++i) {
            ItemStack itemstack = container.getItem(i);
            if (!itemstack.isEmpty()) {
                ItemStackWithSlot slot = new ItemStackWithSlot(i, itemstack);
                serializeNbt(ItemStackWithSlot.CODEC, ops, slot).ifPresent(list::add);
            }
        }
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(stream)
        ) {
            NbtIo.writeAnyTag(list, data);
            return stream.toByteArray();
        }
    }

    private void restoreContainer(Container container, byte[] byteArray) throws IOException {
        restoreContainer(container, byteArray, container.getContainerSize());
    }

    private void restoreContainer(Container container, byte[] byteArray, int invSize) throws IOException {
        ListTag list;
        try (ByteArrayInputStream stream =  new ByteArrayInputStream(byteArray);
             DataInputStream data = new DataInputStream(stream)
        ) {
            list = NbtIo.readAnyTag(data, NbtAccounter.unlimitedHeap()).asList().orElseThrow();
        }
        container.clearContent();
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, player.level().registryAccess());
        for (Tag tag : list) {
            deserializeNbt(ItemStackWithSlot.CODEC, ops, tag).ifPresent(slot -> {
                if (slot.slot() < invSize) {
                    container.setItem(slot.slot(), slot.stack());
                }
            });
        }
    }

    @Override
    public byte[] dumpEquipments() throws IOException {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, player.level().registryAccess());
        CompoundTag equipment = new CompoundTag();
        for (EquipmentSlot slot : Inventory.EQUIPMENT_SLOT_MAPPING.values()) {
            ItemStack item = player.getItemBySlot(slot);
            if (item != null && !item.isEmpty()) {
                String key = slot.getSerializedName();
                serializeNbt(ItemStack.CODEC, ops, item).ifPresent(tag -> equipment.put(key, tag));
            }
        }
        return dumpTag(equipment);
    }

    @Override
    public void restoreEquipments(byte[] byteArray) throws IOException {
        // 在 Paper 上，player.equipment 是 protected 的，所以通过 Inventory 去间接调用
        Inventory inventory = player.getInventory();
        Int2ObjectMap<EquipmentSlot> map = Inventory.EQUIPMENT_SLOT_MAPPING;
        map.forEach((i, _) -> inventory.setItem(i, ItemStack.EMPTY));
        restoreTag(byteArray).asCompound().ifPresent(equipment -> {
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, player.level().registryAccess());
            EnumMap<EquipmentSlot, ItemStack> items = new EnumMap<>(EquipmentSlot.class);
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                String key = slot.getSerializedName();
                equipment.getCompound(key)
                        .flatMap(tag -> deserializeNbt(ItemStack.CODEC, ops, tag))
                        .ifPresent(item -> items.put(slot, item));
            }
            map.forEach((i, slot) -> {
                ItemStack item = items.get(slot);
                if (item != null && !item.isEmpty()) {
                    inventory.setItem(i, item);
                }
            });
        });
    }

    @Override
    public byte[] dumpInventory() throws IOException {
        Inventory inventory = player.getInventory();
        int invSize = inventory.getContainerSize() - Inventory.EQUIPMENT_SLOT_MAPPING.size();
        return dumpContainer(inventory, invSize);
    }

    @Override
    public void restoreInventory(byte[] byteArray) throws IOException {
        Inventory inventory = player.getInventory();
        int invSize = inventory.getContainerSize() - Inventory.EQUIPMENT_SLOT_MAPPING.size();
        restoreContainer(inventory, byteArray, invSize);
    }

    @Override
    public byte[] dumpEnderChest() throws IOException {
        return dumpContainer(player.getEnderChestInventory());
    }

    @Override
    public void restoreEnderChest(byte[] byteArray) throws IOException {
        restoreContainer(player.getEnderChestInventory(), byteArray);
    }

    @Override
    public void save() {
        playerIo.save(player);
    }
}
