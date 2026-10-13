package com.example.myapplication;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

public final class CoachViewModel extends AndroidViewModel {
    private final SqliteCoachRepository store;
    private final CoachService service;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler mainThread = new Handler(Looper.getMainLooper());

    public CoachViewModel(@NonNull Application application) {
        super(application);
        store = new SqliteCoachRepository(application);
        service = new CoachService(store);
    }

    public <T> void submit(Function<CoachService, T> task, Consumer<T> onSuccess,
                           Consumer<RuntimeException> onError) {
        // Keep database reads and writes in order, away from the screen thread.
        worker.execute(() -> {
            try {
                T result = task.apply(service);
                // Android views are updated only after returning to the main thread.
                mainThread.post(() -> onSuccess.accept(result));
            } catch (RuntimeException error) {
                mainThread.post(() -> onError.accept(error));
            }
        });
    }

    @Override
    protected void onCleared() {
        // Close only after queued database work has finished.
        worker.execute(store::close);
        worker.shutdown();
        super.onCleared();
    }
}
