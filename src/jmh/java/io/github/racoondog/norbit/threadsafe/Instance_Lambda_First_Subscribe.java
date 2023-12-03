package io.github.racoondog.norbit.threadsafe;

import io.github.racoondog.BenchmarkEvent;
import io.github.racoondog.Constants;
import io.github.racoondog.norbit.EventBus;
import io.github.racoondog.norbit.listeners.LambdaListener;
import meteordevelopment.orbit.EventHandler;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = Constants.WARMUP_ITERATIONS, time = Constants.WARMUP_TIME)
@Measurement(iterations = Constants.MEASUREMENT_ITERATIONS, time = Constants.MEASUREMENT_TIME)
@Fork(value = Constants.MEASUREMENT_FORKS, warmups = Constants.WARMUP_FORKS)
public class Instance_Lambda_First_Subscribe {
    private EventBus norbit;

    @Setup(Level.Invocation)
    public void setup() {
        LambdaListener.methodHandleCache.clear();
        norbit = EventBus.threadSafe();
        norbit.registerLambdaFactory("io.github.racoondog", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
    }

    @Benchmark
    public void bench() {
        norbit.subscribe(this);
        norbit.unsubscribe(this);
    }

    @EventHandler
    private void onEvent(BenchmarkEvent event) {}
}
