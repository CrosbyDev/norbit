package io.github.racoondog.norbit.threadunsafe;

import io.github.racoondog.BenchmarkEvent;
import io.github.racoondog.Constants;
import io.github.racoondog.norbit.EventBus;
import meteordevelopment.orbit.listeners.ConsumerListener;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = Constants.WARMUP_ITERATIONS, time = Constants.WARMUP_TIME)
@Measurement(iterations = Constants.MEASUREMENT_ITERATIONS, time = Constants.MEASUREMENT_TIME)
@Fork(value = Constants.MEASUREMENT_FORKS, warmups = Constants.WARMUP_FORKS)
public class Consumer_Post {
    private EventBus norbit;

    @Setup
    public void setup() {
        norbit = EventBus.threadUnsafe();
        norbit.subscribe(new ConsumerListener<BenchmarkEvent>(BenchmarkEvent.class, event -> event.blackhole().consume(Integer.bitCount(Integer.parseInt("123")))));
    }

    @Benchmark
    public void bench(Blackhole bh) {
        for (int i = 0; i < Constants.ITERATIONS; i++) {
            norbit.post(new BenchmarkEvent(bh));
        }
    }
}
