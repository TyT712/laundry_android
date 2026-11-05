package com.example.laundrymanager.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Quản lý thực thi các tác vụ nền (Disk I/O / Database) và điều phối về Main Thread an toàn.
 */
public final class AppExecutors {
    private static final Object LOCK = new Object();
    private static AppExecutors instance;

    private final Executor diskIO;
    private final Executor mainThread;

    private AppExecutors(Executor diskIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.mainThread = mainThread;
    }

    public static AppExecutors getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new AppExecutors(
                            Executors.newFixedThreadPool(3),
                            new MainThreadExecutor()
                    );
                }
            }
        }
        return instance;
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    public void runOnDiskIO(Runnable task) {
        diskIO.execute(task);
    }

    public void runOnMainThread(Runnable task) {
        mainThread.execute(task);
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    }
}
