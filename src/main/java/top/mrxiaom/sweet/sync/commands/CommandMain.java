package top.mrxiaom.sweet.sync.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.sync.SweetSync;
import top.mrxiaom.sweet.sync.func.AbstractModule;
import top.mrxiaom.sweet.sync.func.SyncModuleManager;

import java.util.*;

import static top.mrxiaom.pluginbase.utils.CollectionUtils.startsWith;

@AutoRegister
public class CommandMain extends AbstractModule implements CommandExecutor, TabCompleter, Listener {
    public CommandMain(SweetSync plugin) {
        super(plugin);
        registerCommand("sweetsync", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (args.length == 2 && "save".equalsIgnoreCase(args[0])) {
            Player player = Util.getOnlinePlayer(args[1]).orElse(null);
            if (player == null) {
                return t(sender, "&e玩家不在线");
            }
            SyncModuleManager.inst().pushBlocking(player);
            return t(sender, "&a已保存玩家到数据库");
        }
        if (args.length == 2 && "load".equalsIgnoreCase(args[0])) {
            UUID uuid = UUID.fromString(args[1]);
            SyncModuleManager.inst().pullBlocking(uuid, null);
            return t(sender, "&a已执行还原玩家数据，详见控制台");
        }

        if (args.length >= 1 && "reload".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.sync.reload")) {
            if (args.length >= 2 && "database".equalsIgnoreCase(args[1])) {
                plugin.options.database().reloadConfig();
                plugin.options.database().reconnect();
                return t(sender, "&a已重新连接并配置数据库");
            }
            plugin.reloadConfig();
            return t(sender, "&a配置文件已重载");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if (sender.hasPermission("sweet.sync.reload")) {
                list.add("reload");
            }
            return startsWith(args[0], list);
        }
        return Collections.emptyList();
    }
}
