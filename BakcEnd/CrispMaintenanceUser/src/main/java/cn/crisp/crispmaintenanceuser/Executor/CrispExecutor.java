package cn.crisp.crispmaintenanceuser.Executor;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class CrispExecutor {
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 200,
            1, TimeUnit.MINUTES,
            new LinkedBlockingDeque<>(100000),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());
    public void execute(@NotNull Runnable runnable) {
        executor.execute(runnable);
    }

    @PreDestroy
    public void destroy() {
        executor.shutdown();
    }
}
