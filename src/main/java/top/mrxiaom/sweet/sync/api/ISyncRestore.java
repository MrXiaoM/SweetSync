package top.mrxiaom.sweet.sync.api;

import org.jetbrains.annotations.NotNull;

public interface ISyncRestore<E extends ISyncRestore.IExecutor> {
    E pull(@NotNull SyncRestoreContext context) throws Exception;

    interface IExecutor {
        void restore() throws Exception;
    }
}
