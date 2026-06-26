package top.mrxiaom.sweet.sync;

import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.paper.PaperFactory;
import top.mrxiaom.pluginbase.utils.Versioning;
import top.mrxiaom.pluginbase.utils.inventory.InventoryFactory;
import top.mrxiaom.pluginbase.utils.item.ItemEditor;
import top.mrxiaom.pluginbase.utils.scheduler.FoliaLibScheduler;
import top.mrxiaom.pluginbase.utils.ClassLoaderWrapper;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.pluginbase.resolver.DefaultLibraryResolver;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweet.sync.database.EnderChestDatabase;
import top.mrxiaom.sweet.sync.database.EquipmentDatabase;
import top.mrxiaom.sweet.sync.database.InventoryDatabase;
import top.mrxiaom.sweet.sync.nms.Minecraft;

public class SweetSync extends BukkitPlugin {
    public static SweetSync getInstance() {
        return (SweetSync) BukkitPlugin.getInstance();
    }
    public SweetSync() throws Exception {
        super(options()
                .bungee(false)
                .adventure(true)
                .database(true)
                .reconnectDatabaseWhenReloadConfig(false)
                .scanIgnore("top.mrxiaom.sweet.sync.libs")
        );
        this.scheduler = new FoliaLibScheduler(this);

        try {
            //noinspection ResultOfMethodCallIgnored
            getDescription().getLibraries();
        } catch (LinkageError ignored) {
            info("正在检查依赖库状态");
            File librariesDir = ClassLoaderWrapper.isSupportLibraryLoader
                    ? new File("libraries")
                    : new File(this.getDataFolder(), "libraries");
            DefaultLibraryResolver resolver = new DefaultLibraryResolver(getLogger(), librariesDir);

            YamlConfiguration overrideLibraries = ConfigUtils.load(resolve("./.override-libraries.yml"));
            for (String key : overrideLibraries.getKeys(false)) {
                resolver.getStartsReplacer().put(key, overrideLibraries.getString(key));
            }
            resolver.addResolvedLibrary(BuildConstants.RESOLVED_LIBRARIES);

            List<URL> libraries = resolver.doResolve();
            info("正在添加 " + libraries.size() + " 个依赖库到类加载器");
            for (URL library : libraries) {
                this.classLoader.addURL(library);
            }
        }
    }

    @Override
    public @NotNull ItemEditor initItemEditor() {
        return PaperFactory.createItemEditor();
    }

    @Override
    public @NotNull InventoryFactory initInventoryFactory() {
        return PaperFactory.createInventoryFactory();
    }

    private InventoryDatabase inventoryDatabase;
    private EquipmentDatabase equipmentDatabase;
    private EnderChestDatabase enderChestDatabase;

    public InventoryDatabase getInventoryDatabase() {
        return inventoryDatabase;
    }

    public EquipmentDatabase getEquipmentDatabase() {
        return equipmentDatabase;
    }

    public EnderChestDatabase getEnderChestDatabase() {
        return enderChestDatabase;
    }

    @Override
    protected void beforeLoad() {
        String mcVersion = Versioning.getMinecraftVersion();
        if (!Minecraft.inst(getLogger(), mcVersion)) {
            throw new UnsupportedOperationException("当前 Minecraft 版本 " + mcVersion + " 不受本插件支持");
        }
    }

    @Override
    protected void beforeEnable() {
        options.registerDatabase(
                inventoryDatabase = new InventoryDatabase(this),
                equipmentDatabase = new EquipmentDatabase(this),
                enderChestDatabase = new EnderChestDatabase(this)
        );
    }

    @Override
    protected void afterEnable() {
        getLogger().info("SweetSync 加载完毕");
    }
}
