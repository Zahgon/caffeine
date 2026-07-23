/*
 * Copyright 2019 Ben Manes. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.benmanes.caffeine.cache;

import static java.util.Objects.requireNonNull;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A scheduler that submits a task to an executor after a given delay.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
@NullMarked
@FunctionalInterface
public interface Scheduler {

    /**
     * Returns a future that will submit the task to the executor after the given delay.
     *
     * @param executor the executor to run the task
     * @param command the runnable task to schedule
     * @param delay how long to delay, in units of {@code unit}
     * @param unit a {@code TimeUnit} determining how to interpret the {@code delay} parameter
     * @return a scheduled future representing the pending submission of the task
     */
    Future<? extends @Nullable Object> schedule(Executor executor, Runnable command, long delay, TimeUnit unit);

    static Scheduler disabledScheduler() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static Scheduler systemScheduler() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static Scheduler forScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    static Scheduler guardedScheduler(Scheduler scheduler) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}

enum SystemScheduler implements Scheduler {

    INSTANCE;

    @Override
    public Future<?> schedule(Executor executor, Runnable command, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}

final class ExecutorServiceScheduler implements Scheduler, Serializable {

    private static final Logger logger = System.getLogger(ExecutorServiceScheduler.class.getName());

    private static final long serialVersionUID = 1;

    @SuppressWarnings("serial")
    final ScheduledExecutorService scheduledExecutorService;

    ExecutorServiceScheduler(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = requireNonNull(scheduledExecutorService);
    }

    @Override
    public Future<? extends @Nullable Object> schedule(Executor executor, Runnable command, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}

final class GuardedScheduler implements Scheduler, Serializable {

    private static final Logger logger = System.getLogger(GuardedScheduler.class.getName());

    private static final long serialVersionUID = 1;

    @SuppressWarnings("serial")
    final Scheduler delegate;

    GuardedScheduler(Scheduler delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public Future<? extends @Nullable Object> schedule(Executor executor, Runnable command, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}

enum DisabledScheduler implements Scheduler {

    INSTANCE;

    @Override
    public Future<? extends @Nullable Object> schedule(Executor executor, Runnable command, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}

@SuppressWarnings("CheckedExceptionNotThrown")
enum DisabledFuture implements Future<@Nullable Void> {

    INSTANCE;

    static Future<? extends @Nullable Object> instance() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isDone() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public Void get(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    @Override
    @Nullable
    public Void get() {
        throw new UnsupportedOperationException("STUB: not implemented");
    }
}
