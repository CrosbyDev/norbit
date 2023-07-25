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
public class Static_Lambda_First_Subscribe {
    private EventBus norbit;

    @Setup(Level.Invocation)
    public void setup() {
        norbit = new EventBus();
        norbit.registerLambdaFactory("io.github.racoondog", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
    }

    @Benchmark
    public void bench() {
        norbit.subscribe(Static_Lambda_First_Subscribe.class);
        norbit.unsubscribe(Static_Lambda_First_Subscribe.class);
    }

    @EventHandler
    private static void onEvent(BenchmarkEvent event) {}
}
