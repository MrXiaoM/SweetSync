package top.mrxiaom.sweet.sync.nms;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@ApiStatus.Internal
public interface IMinecraft {

    @NotNull IPlayerData getPlayerData(@NotNull UUID uuid, @Nullable String name);

}
