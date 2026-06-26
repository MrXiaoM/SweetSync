package top.mrxiaom.sweet.sync.nms;

import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

@ApiStatus.Internal
public interface IPlayerData {
    byte[] dumpEquipments() throws IOException;
    void restoreEquipments(byte[] byteArray) throws IOException;

    byte[] dumpInventory() throws IOException;
    void restoreInventory(byte[] byteArray) throws IOException;

    byte[] dumpEnderChest() throws IOException;
    void restoreEnderChest(byte[] byteArray) throws IOException;

    void save() throws IOException;
}
