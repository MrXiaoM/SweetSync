package top.mrxiaom.sweet.sync.nms.mojang_26_1;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.io.*;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CodecHelper {
    private static final Logger logger = Logger.getLogger("SweetSync");

    protected static byte[] dumpTag(Tag tag) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(stream)
        ) {
            NbtIo.writeAnyTag(tag, data);
            return stream.toByteArray();
        }
    }

    protected static Tag restoreTag(byte[] byteArray) throws IOException {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(byteArray);
             DataInputStream data = new DataInputStream(stream)
        ) {
            return NbtIo.readAnyTag(data, NbtAccounter.unlimitedHeap());
        }
    }

    protected static <T> Optional<Tag> serializeNbt(Codec<T> codec, DynamicOps<Tag> ops, T slot) {
        DataResult<Tag> result = codec.encodeStart(ops, slot);
        Optional<Tag> optional;
        switch (result) {
            case DataResult.Success<Tag> success:
                optional = Optional.of(success.value());
                break;
            case DataResult.Error<Tag> error:
                optional = error.partialValue();
                String message = error.message();
                // TODO: 记录错误
                logger.log(Level.WARNING, "编码 " + codec + " 时出现错误: " + message);
                break;
        }
        return optional;
    }

    protected static <T> Optional<T> deserializeNbt(Codec<T> codec, DynamicOps<Tag> ops, Tag tag) {
        DataResult<Pair<T, Tag>> result = codec.decode(ops, tag);
        Optional<Pair<T, Tag>> optional;
        switch (result) {
            case DataResult.Success<Pair<T, Tag>> success:
                optional = Optional.of(success.value());
                break;
            case DataResult.Error<Pair<T, Tag>> error:
                optional = error.partialValue();
                String message = error.message();
                // TODO: 记录错误
                logger.log(Level.WARNING, "解码 " + codec + " 时出现错误: " + message);
                break;
        }
        return optional.map(Pair::getFirst);
    }

}
