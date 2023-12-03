package io.github.racoondog.norbit.threadunsafe;

import io.github.racoondog.BenchmarkEvent;
import io.github.racoondog.Constants;
import io.github.racoondog.norbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = Constants.WARMUP_ITERATIONS, time = Constants.WARMUP_TIME)
@Measurement(iterations = Constants.MEASUREMENT_ITERATIONS, time = Constants.MEASUREMENT_TIME)
@Fork(value = Constants.MEASUREMENT_FORKS, warmups = Constants.WARMUP_FORKS)
public class Instance_Multiple_Identical_Lambda_Post {
    private EventBus norbit;

    @Setup
    public void setup() {
        norbit = EventBus.threadUnsafe();
        norbit.registerLambdaFactory("io.github.racoondog", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        for (int i = 0; i < Constants.LISTENERS; i++) {
            norbit.subscribe(new BenchmarkListener());
        }
    }

    @Benchmark
    public void bench(Blackhole bh) {
        for (int i = 0; i < Constants.ITERATIONS; i++) {
            norbit.post(new BenchmarkEvent(bh));
        }
    }

    private static class BenchmarkListener {
        @EventHandler
        private void onEvent(BenchmarkEvent event) {
            event.blackhole().consume(Integer.bitCount(Integer.parseInt("123")));
        }
    }
}
