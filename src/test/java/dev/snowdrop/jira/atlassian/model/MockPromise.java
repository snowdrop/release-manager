/**
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package dev.snowdrop.jira.atlassian.model;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

import io.atlassian.util.concurrent.Promise;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class MockPromise<T> implements Promise<T> {
    @Override
    public T claim() {
        return null;
    }
    
    @Override
    public Promise<T> done(Consumer<? super T> c) {
        return null;
    }
    
    @Override
    public Promise<T> fail(Consumer<Throwable> c) {
        return null;
    }
    
    @Override
    public Promise<T> then(TryConsumer<? super T> callback) {
        return null;
    }
    
    @Override
    public <B> Promise<B> map(Function<? super T, ? extends B> function) {
        return null;
    }
    
    @Override
    public <B> Promise<B> flatMap(Function<? super T, ? extends Promise<? extends B>> function) {
        return null;
    }
    
    @Override
    public Promise<T> recover(Function<Throwable, ? extends T> handleThrowable) {
        return null;
    }
    
    @Override
    public <B> Promise<B> fold(Function<Throwable, ? extends B> handleThrowable, Function<? super T, ? extends B> function) {
        return null;
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }
    
    @Override
    public boolean isCancelled() {
        return false;
    }
    
    @Override
    public boolean isDone() {
        return false;
    }
    
    @Override
    public T get() throws InterruptedException, ExecutionException {
        return null;
    }
    
    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
