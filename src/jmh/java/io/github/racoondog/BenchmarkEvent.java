package io.github.racoondog;

import org.openjdk.jmh.infra.Blackhole;

public record BenchmarkEvent(Blackhole blackhole) {}
