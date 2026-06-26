package top.mrxiaom.sweet.sync.nms.spigot_1_21_11;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.*;

import java.io.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CodecHelper {
    private static final Logger logger = Logger.getLogger("SweetSync");

    protected static byte[] dumpTag(NBTBase tag) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(stream)
        ) {
            NBTCompressedStreamTools.a(tag, data);
            return stream.toByteArray();
        }
    }

    protected static NBTBase restoreTag(byte[] byteArray) throws IOException {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(byteArray);
             DataInputStream data = new DataInputStream(stream)
        ) {
            return NBTCompressedStreamTools.b(data, NBTReadLimiter.c());
        }
    }

    protected static <T> Optional<NBTBase> serializeNbt(Codec<T> codec, DynamicOps<NBTBase> ops, T slot) {
        DataResult<NBTBase> result = codec.encodeStart(ops, slot);
        Optional<NBTBase> optional;
        switch (result) {
            case DataResult.Success<NBTBase> success:
                optional = Optional.of(success.value());
                break;
            case DataResult.Error<NBTBase> error:
                optional = error.partialValue();
                String message = error.message();
                // TODO: 记录错误
                logger.log(Level.WARNING, "编码 " + codec + " 时出现错误: " + message);
                break;
        }
        return optional;
    }

    protected static <T> Optional<T> deserializeNbt(Codec<T> codec, DynamicOps<NBTBase> ops, NBTBase tag) {
        DataResult<Pair<T, NBTBase>> result = codec.decode(ops, tag);
        Optional<Pair<T, NBTBase>> optional;
        switch (result) {
            case DataResult.Success<Pair<T, NBTBase>> success:
                optional = Optional.of(success.value());
                break;
            case DataResult.Error<Pair<T, NBTBase>> error:
                optional = error.partialValue();
                String message = error.message();
                // TODO: 记录错误
                logger.log(Level.WARNING, "解码 " + codec + " 时出现错误: " + message);
                break;
        }
        return optional.map(Pair::getFirst);
    }

}
