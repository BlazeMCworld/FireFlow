package de.blazemcworld.fireflow.util;

import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;

public class TodoListExecutor implements Executor {

    private final Queue<Runnable> todo = new ConcurrentLinkedDeque<>();

    @Override
    public void execute(@NotNull Runnable runnable) {
        todo.add(runnable);
    }

    public void work() {
        while (!todo.isEmpty()) todo.poll().run();
    }
}
