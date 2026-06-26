package top.mrxiaom.sweet.sync.func;

import top.mrxiaom.sweet.sync.SweetSync;

@SuppressWarnings({"unused"})
public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<SweetSync> {
    public AbstractPluginHolder(SweetSync plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(SweetSync plugin, boolean register) {
        super(plugin, register);
    }
}
