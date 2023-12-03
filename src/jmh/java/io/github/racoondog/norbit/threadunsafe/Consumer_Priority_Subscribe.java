package io.github.racoondog.norbit.threadunsafe;

import io.github.racoondog.BenchmarkEvent;
import io.github.racoondog.Constants;
import io.github.racoondog.norbit.EventBus;
import meteordevelopment.orbit.listeners.ConsumerListener;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = Constants.WARMUP_ITERATIONS, time = Constants.WARMUP_TIME)
@Measurement(iterations = Constants.MEASUREMENT_ITERATIONS, time = Constants.MEASUREMENT_TIME)
@Fork(value = Constants.MEASUREMENT_FORKS, warmups = Constants.WARMUP_FORKS)
public class Consumer_Priority_Subscribe {
    private EventBus norbit;
    private int priorityStep;

    @Setup(Level.Invocation)
    public void setup() {
        norbit = EventBus.threadUnsafe();
        priorityStep = (int) Math.floor(1000f / Constants.LISTENERS);
    }

    @Benchmark
    public void bench() {
        for (int i = 0; i < Constants.LISTENERS; i++) {
            int priority = -500 + i * priorityStep;
            norbit.subscribe(new ConsumerListener<BenchmarkEvent>(BenchmarkEvent.class, priority, event -> event.blackhole().consume(Integer.bitCount(Integer.parseInt("123")))));
        }
    }
}
