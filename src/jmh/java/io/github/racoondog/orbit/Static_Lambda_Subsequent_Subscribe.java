package io.github.racoondog.orbit;

import io.github.racoondog.BenchmarkEvent;
import io.github.racoondog.Constants;
import meteordevelopment.orbit.EventBus;
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
public class Static_Lambda_Subsequent_Subscribe {
    private EventBus orbit;

    @Setup
    public void setup() {
        orbit = new EventBus();
        orbit.registerLambdaFactory("io.github.racoondog", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        // Pre-cache
        orbit.subscribe(Static_Lambda_Subsequent_Subscribe.class);
        orbit.unsubscribe(Static_Lambda_Subsequent_Subscribe.class);
    }

    @Benchmark
    public void bench() {
        for (int i = 0; i < Constants.ITERATIONS; i++) {
            orbit.subscribe(Static_Lambda_Subsequent_Subscribe.class);
            orbit.unsubscribe(Static_Lambda_Subsequent_Subscribe.class);
        }
    }

    @EventHandler
    private static void onEvent(BenchmarkEvent event) {}
}
