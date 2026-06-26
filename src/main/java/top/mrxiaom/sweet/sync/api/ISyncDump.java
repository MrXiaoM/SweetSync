package top.mrxiaom.sweet.sync.api;

import org.jetbrains.annotations.NotNull;

public interface ISyncDump<E extends ISyncDump.IExecutor> {
    E dump(@NotNull SyncDumpContext context) throws Exception;

    interface IExecutor {
        void push() throws Exception;
    }
}
