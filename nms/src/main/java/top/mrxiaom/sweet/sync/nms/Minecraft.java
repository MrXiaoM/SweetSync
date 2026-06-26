package top.mrxiaom.sweet.sync.nms;

import top.mrxiaom.sweet.sync.nms.mojang_26_1.MinecraftMojang26_1;
import top.mrxiaom.sweet.sync.nms.spigot_1_21_11.MinecraftSpigot1_21_11;

import java.util.logging.Logger;

public class Minecraft {
    private static IMinecraft instance;

    private static int parseVersion(String[] split, int index, int def) {
        if (split.length > index) {
            try {
                return Integer.parseInt(split[index]);
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

    public static boolean inst(Logger logger, String minecraftVersion) {
        String[] split = minecraftVersion.split("-", 2)[0].split("\\.", 3);
        int major = parseVersion(split, 0, 1);
        int minor = parseVersion(split, 1, 0);
        int patch = parseVersion(split, 2, 0);
        // TODO: 更多版本支持
        if (major == 1) {
            if (minor == 21) {
                if (patch < 9) {

                } else { // 1.21.9+
                    instance = new MinecraftSpigot1_21_11();
                    return true;
                }
            }
        }
        if (major == 26) {
            if (minor == 1) { // 26.1.x
                instance = new MinecraftMojang26_1();
                return true;
            }
        }
        return false;
    }

    public static IMinecraft getInstance() {
        return instance;
    }
}
