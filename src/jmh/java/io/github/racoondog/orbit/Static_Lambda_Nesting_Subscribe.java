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
public class Static_Lambda_Nesting_Subscribe {
    private EventBus orbit;

    @Setup(Level.Invocation)
    public void setup() {
        orbit = new EventBus();
        orbit.registerLambdaFactory("io.github.racoondog", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
    }

    @Benchmark
    public void bench() {
        orbit.subscribe(DoubleNestedBenchmarkListener.class);
        orbit.unsubscribe(DoubleNestedBenchmarkListener.class);
    }

    private static class DoubleNestedBenchmarkListener extends NestedBenchmarkListener {
        @EventHandler
        private static void onDoubleNestedEventOne(BenchmarkEvent event) {}

        @EventHandler
        private static void onDoubleNestedEventTwo(BenchmarkEvent event) {}

        @EventHandler
        private static void onDoubleNestedEventThree(BenchmarkEvent event) {}
    }

    private static class NestedBenchmarkListener extends BenchmarkListener {
        @EventHandler
        private static void onNestedEventOne(BenchmarkEvent event) {}

        @EventHandler
        private static void onNestedEventTwo(BenchmarkEvent event) {}
    }

    private static class BenchmarkListener {
        @EventHandler
        private static void onEvent(BenchmarkEvent event) {}
    }
}
