package io.github.racoondog.util;

public class JmhUtils {
    public static int lerp(float delta, int min, int max) {
        return min + Math.round(delta * (max - min));
    }
}
