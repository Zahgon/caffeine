/*
 * Copyright 2016 Wim Deblauwe. All Rights Reserved.
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
package com.github.benmanes.caffeine.examples.writebehind.rxjava;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

/**
 * This class implements write-behind semantics for caching. The provided {@code writeAction} is
 * invoked periodically every {@code bufferTime} interval, receiving a {@linkplain Map} containing
 * the updated keys and corresponding values in the cache since the last invocation.
 * <p>
 * In scenarios where a key is updated multiple times within the same period, a coalescing function
 * is responsible for determining the value to retain.
 * <p>
 * A practical use case for this class is user activity tracking on a website. Instead of
 * immediately updating the database for every user action, you can aggregate updates and write them
 * to the database in batches at regular intervals (e.g., every x seconds), recording only the most
 * recent timestamp.
 *
 * @param <K> the type of the key in the cache
 * @param <V> the type of the value in the cache
 * @author wim.deblauwe@gmail.com (Wim Deblauwe)
 */
public final class WriteBehindCacheWriter<K, V> implements BiConsumer<K, V> {

    private final Subject<Entry<K, V>> subject;

    private WriteBehindCacheWriter(Builder<K, V> builder) {
        subject = PublishSubject.<Entry<K, V>>create().toSerialized();
        subject.buffer(builder.bufferTime.toNanos(), TimeUnit.NANOSECONDS).map(entries -> entries.stream().collect(toMap(Entry::getKey, Entry::getValue, builder.coalescer))).subscribe(builder.writeAction::accept);
    }

    @Override
    public void accept(K key, V value) {
        throw new UnsupportedOperationException("STUB: not implemented");
    }

    public static final class Builder<K, V> {

        private Consumer<Map<K, V>> writeAction;

        private BinaryOperator<V> coalescer;

        private Duration bufferTime;

        @CanIgnoreReturnValue
        public Builder<K, V> bufferTime(Duration duration) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @CanIgnoreReturnValue
        public Builder<K, V> writeAction(Consumer<Map<K, V>> writeAction) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        @CanIgnoreReturnValue
        public Builder<K, V> coalesce(BinaryOperator<V> coalescer) {
            throw new UnsupportedOperationException("STUB: not implemented");
        }

        public WriteBehindCacheWriter<K, V> build() {
            throw new UnsupportedOperationException("STUB: not implemented");
        }
    }
}
