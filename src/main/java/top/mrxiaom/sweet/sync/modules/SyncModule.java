package top.mrxiaom.sweet.sync.modules;

import org.jetbrains.annotations.NotNull;
import top.mrxiaom.sweet.sync.api.ISyncDump;
import top.mrxiaom.sweet.sync.api.ISyncRestore;
import top.mrxiaom.sweet.sync.api.SyncDumpContext;
import top.mrxiaom.sweet.sync.api.SyncRestoreContext;

public class SyncModule<D extends ISyncDump.IExecutor, R extends ISyncRestore.IExecutor> {
    private final @NotNull String id;
    private final @NotNull ISyncDump<D> dumpExecutor;
    private final @NotNull ISyncRestore<R> restoreExecutor;

    public SyncModule(@NotNull String id, @NotNull ISyncDump<D> dumpExecutor, @NotNull ISyncRestore<R> restoreExecutor) {
        this.id = id;
        this.dumpExecutor = dumpExecutor;
        this.restoreExecutor = restoreExecutor;
    }

    public @NotNull String id() {
        return id;
    }

    public D dump(@NotNull SyncDumpContext context) throws Exception {
        return dumpExecutor.dump(context);
    }

    public R pull(@NotNull SyncRestoreContext context) throws Exception {
        return restoreExecutor.pull(context);
    }
}
