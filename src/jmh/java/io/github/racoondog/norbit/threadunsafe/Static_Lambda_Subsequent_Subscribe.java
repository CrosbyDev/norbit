package io.github.racoondog.norbit.threadunsafe;

import io.github.racoondog.BenchmarkEvent;
import io.github.racoondog.Constants;
import io.github.racoondog.norbit.EventBus;
import io.github.racoondog.norbit.EventHandler;
import io.github.racoondog.norbit.listeners.LambdaListener;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = Constants.WARMUP_ITERATIONS, time = Constants.WARMUP_TIME)
@Measurement(iterations = Constants.MEASUREMENT_ITERATIONS, time = Constants.MEASUREMENT_TIME)
@Fork(value = Constants.MEASUREMENT_FORKS, warmups = Constants.WARMUP_FORKS)
public class Static_Lambda_Subsequent_Subscribe {
    private EventBus norbit;

    @Setup
    public void setup() {
        LambdaListener.methodHandleCache.clear();
        norbit = EventBus.threadUnsafe();
        norbit.registerLambdaFactory("io.github.racoondog", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        // Pre-cache
        norbit.subscribe(Static_Lambda_Subsequent_Subscribe.class);
        norbit.unsubscribe(Static_Lambda_Subsequent_Subscribe.class);
    }

    @Benchmark
    public void bench() {
        for (int i = 0; i < Constants.ITERATIONS; i++) {
            norbit.subscribe(Static_Lambda_Subsequent_Subscribe.class);
            norbit.unsubscribe(Static_Lambda_Subsequent_Subscribe.class);
        }
    }

    @EventHandler
    private static void onEvent(BenchmarkEvent event) {}
}
