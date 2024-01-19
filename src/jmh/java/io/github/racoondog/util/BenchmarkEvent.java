package io.github.racoondog.util;

import org.openjdk.jmh.infra.Blackhole;

public record BenchmarkEvent(Blackhole blackhole) {}
