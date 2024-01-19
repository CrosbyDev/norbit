package io.github.racoondog.orbit;

import io.github.racoondog.util.BenchmarkEvent;
import io.github.racoondog.Constants;
import io.github.racoondog.util.JmhUtils;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.EventPriority;
import meteordevelopment.orbit.listeners.ConsumerListener;
import meteordevelopment.orbit.listeners.IListener;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = Constants.WARMUP_ITERATIONS, time = Constants.WARMUP_TIME)
@Measurement(iterations = Constants.MEASUREMENT_ITERATIONS, time = Constants.MEASUREMENT_TIME)
@Fork(value = Constants.MEASUREMENT_FORKS, warmups = Constants.WARMUP_FORKS)
public class Subscribe {
    private EventBus orbit;
    private IListener listener;

    @Setup(Level.Invocation)
    public void setup() {
        orbit = new EventBus();

        for (float i = 0; i < Constants.LISTENERS; i++) {
            int priority = JmhUtils.lerp(i / Constants.LISTENERS, EventPriority.LOWEST, EventPriority.HIGHEST);
            orbit.subscribe(new ConsumerListener<>(BenchmarkEvent.class, priority, event -> event.blackhole().consume(Integer.bitCount(Integer.parseInt("123")))));
        }

        listener = new ConsumerListener<>(BenchmarkEvent.class, event -> event.blackhole().consume(Integer.bitCount(Integer.parseInt("123"))));
    }

    @Benchmark
    public void bench() {
        orbit.subscribe(listener);
    }
}
