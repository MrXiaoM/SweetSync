package top.mrxiaom.sweet.sync.func;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.sync.SweetSync;
import top.mrxiaom.sweet.sync.api.*;
import top.mrxiaom.sweet.sync.modules.SyncModule;
import top.mrxiaom.sweet.sync.nms.IMinecraft;
import top.mrxiaom.sweet.sync.nms.IPlayerData;
import top.mrxiaom.sweet.sync.nms.Minecraft;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@AutoRegister
public class SyncModuleManager extends AbstractModule implements Listener {
    private final IMinecraft minecraft = Minecraft.getInstance();
    private final Map<String, SyncModule<?, ?>> modules = new HashMap<>();
    private final List<SyncModule<?, ?>> enabledModules = new ArrayList<>();
    private EnumLoginPhase loginPhase;
    private int lockTimeout = 10;
    public SyncModuleManager(SweetSync plugin) {
        super(plugin);
        registerBuiltInModules();
        registerEvents();
    }

    private void registerBuiltInModules() {
        registerModule(new SyncModule<>("inventory",
                context -> {
                    byte[] equipments = context.data().dumpEquipments();
                    byte[] inventory = context.data().dumpInventory();
//                    info("  装备数据大小: " + (equipments == null ? "空" : equipments.length));
//                    info("  背包数据大小: " + (inventory == null ? "空" : inventory.length));

                    return () -> {
                        context.pushByteArray(plugin.getEquipmentDatabase(), equipments);
                        context.pushByteArray(plugin.getInventoryDatabase(), inventory);
                    };
                },
                context -> {
                    byte[] equipments = context.pullByteArray(plugin.getEquipmentDatabase());
                    byte[] inventory = context.pullByteArray(plugin.getInventoryDatabase());
//                    info("  装备数据大小: " + (equipments == null ? "空" : equipments.length));
//                    info("  背包数据大小: " + (inventory == null ? "空" : inventory.length));
                    return () -> {
                        if (equipments != null) {
                            context.data().restoreEquipments(equipments);
                        }
                        if (inventory != null) {
                            context.data().restoreInventory(inventory);
                        }
                    };
                }));
        registerModule(new SyncModule<>("ender-chest",
                context -> {
                    byte[] enderChest = context.data().dumpEnderChest();
//                    info("  末影箱数据大小: " + (enderChest == null ? "空" : enderChest.length));

                    return () -> {
                        context.pushByteArray(plugin.getEnderChestDatabase(), enderChest);
                    };
                },
                context -> {
                    byte[] enderChest = context.pullByteArray(plugin.getEnderChestDatabase());
//                    info("  末影箱数据大小: " + (enderChest == null ? "空" : enderChest.length));
                    return () -> {
                        if (enderChest != null) {
                            context.data().restoreEnderChest(enderChest);
                        }
                    };
                }));
    }

    public void registerModule(SyncModule<?, ?> module) {
        modules.put(module.id(), module);
    }

    @Override
    public void reloadConfig(MemoryConfiguration config) {
        this.loginPhase = Util.valueOr(EnumLoginPhase.class, config.getString("load-login-phase"), EnumLoginPhase.PRE_LOGIN);
        this.lockTimeout = Math.max(1, config.getInt("lock-timeout", 10));
        this.enabledModules.clear();
        ConfigurationSection section = config.getConfigurationSection("sync-modules");
        if (section != null) for (String key : section.getKeys(false)) {
            boolean enable = section.getBoolean(key + ".enable", false);
            SyncModule<?, ?> module = modules.get(key);
            if (module != null && enable) {
                enabledModules.add(module);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        if (!EnumLoginPhase.PRE_LOGIN.equals(loginPhase)) return;
        UUID uuid = e.getUniqueId();
        String name = e.getName();
        InetAddress address = e.getAddress();
        try {
//            info("正在拉取玩家 " + name + " (" + uuid + ") 的数据");
//            long startTime = System.currentTimeMillis();
            pullBlocking(null, uuid, name, address);
//            long costTime = System.currentTimeMillis() - startTime;
//            info("玩家 " + name + " (" + uuid + ") 的数据拉取完成，耗时 " + costTime + "ms");
        } catch (Exception ex) {
            // TODO: 完善错误踢出提示
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "同步数据时出现错误");
            warn("拉取玩家 " + name + " (" + uuid + ") 的数据时出现异常", ex);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!EnumLoginPhase.JOIN_GAME.equals(loginPhase)) return;
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        InetSocketAddress address = player.getAddress();
        try {
            pullBlocking(player, uuid, name, address != null ? address.getAddress() : InetAddress.getLocalHost());
        } catch (Exception ex) {
            warn("拉取玩家 " + name + " (" + uuid + ") 的数据时出现异常", ex);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
//        UUID uuid = e.getPlayer().getUniqueId();
//        String name = e.getPlayer().getName();
//        info("正在保存玩家 " + name + " (" + uuid + ") 的数据");
//        long startTime = System.currentTimeMillis();
        pushBlocking(e.getPlayer());
//        long costTime = System.currentTimeMillis() - startTime;
//        info("玩家 " + name + " (" + uuid + ") 的数据保存完成，耗时 " + costTime + "ms");
    }

    private boolean tryLock(Connection conn, UUID uuid) throws SQLException {
        return tryLock(conn, uuid, lockTimeout);
    }

    private boolean tryLock(Connection conn, UUID uuid, int timeout) throws SQLException {
        if (plugin.options.database().isMySQL()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT GET_LOCK(?, ?);")) {
                ps.setString(1, "sweetsync_lock_" + uuid);
                ps.setInt(2, timeout);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) == 1;
                    }
                }
            }
            return false;
        }
        // 不使用 MySQL 的情况下不获取访问锁
        return true;
    }

    private void unlock(Connection conn, UUID uuid) throws SQLException {
        if (plugin.options.database().isMySQL()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT RELEASE_LOCK(?)")) {
                ps.setString(1, "sweetsync_lock_" + uuid);
                ps.execute();
            }
        }
    }

    /**
     * 同步阻塞，从数据库拉取并同步玩家的数据
     * @param uuid 玩家UUID
     * @param name 玩家名字(可选)
     */
    public boolean pullBlocking(@NotNull UUID uuid, @Nullable String name) {
        return pullBlocking(null, uuid, name, null);
    }

    private boolean pullBlocking(Player player, UUID uuid, String name, InetAddress address) {
        IPlayerData playerData = minecraft.getPlayerData(uuid, name);
        try {
//            long startTime = System.currentTimeMillis();
            List<ISyncRestore.IExecutor> executors = new ArrayList<>();
            try (Connection conn = plugin.getConnection()) {
                if (tryLock(conn, uuid)) {
                    try {
                        SyncRestoreContext context = new SyncRestoreContext(uuid, address == null ? InetAddress.getLocalHost() : address, player, conn, playerData);
                        for (SyncModule<?, ?> module : enabledModules) {
                            executors.add(module.pull(context));
                        }
                    } finally {
                        unlock(conn, uuid);
                    }
                }
            }
//            long pullTime = System.currentTimeMillis() - startTime;
//            info("  拉取数据耗时 " + pullTime + "ms");

//            startTime = System.currentTimeMillis();

            for (ISyncRestore.IExecutor executor : executors) {
                executor.restore();
            }

//            long restoreTime = System.currentTimeMillis() - startTime;
//            info("  应用数据到玩家耗时 " + restoreTime + "ms");

//            startTime = System.currentTimeMillis();

            playerData.save();

//            long saveTime = System.currentTimeMillis() - startTime;
//            info("  保存玩家数据耗时 " + saveTime + "ms");

            return true;
        } catch (Exception ex) {
            warn("拉取玩家 " + name + " (" + uuid + ") 的数据时出现异常", ex);
            return false;
        }
    }

    /**
     * 同步阻塞，将玩家的数据推送到数据库
     * @param player 在线玩家
     */
    public boolean pushBlocking(Player player) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        IPlayerData playerData = minecraft.getPlayerData(uuid, name);
//        long startTime = System.currentTimeMillis();
        try (Connection conn = plugin.getConnection()) {
//            long connTime = System.currentTimeMillis() - startTime;
//            info("  建立数据库连接耗时 " + connTime + "ms");
//            startTime = System.currentTimeMillis();
            List<ISyncDump.IExecutor> executors = new ArrayList<>();
            SyncDumpContext context = new SyncDumpContext(uuid, player, conn, playerData);
            for (SyncModule<?, ?> module : enabledModules) {
                executors.add(module.dump(context));
            }
//            long dumpTime = System.currentTimeMillis() - startTime;
//            info("  导出数据耗时 " + dumpTime + "ms");

//            startTime = System.currentTimeMillis();

            conn.setAutoCommit(false);
            if (tryLock(conn, uuid)) {
                try {
                    for (ISyncDump.IExecutor executor : executors) {
                        executor.push();
                    }
                    conn.commit();
                } finally {
                    unlock(conn, uuid);
                }
            } else {
                throw new IllegalStateException("获取玩家同步锁超时");
            }

//            long commitTime = System.currentTimeMillis() - startTime;
//            info("  提交数据耗时 " + commitTime + "ms");

            return true;
        } catch (Exception ex) {
            warn("保存玩家 " + name + " (" + uuid + ") 的数据时出现异常", ex);
            return false;
        }
    }

    public static SyncModuleManager inst() {
        return instanceOf(SyncModuleManager.class);
    }
}
