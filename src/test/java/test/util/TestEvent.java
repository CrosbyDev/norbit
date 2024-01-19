package test.util;

import org.junit.jupiter.api.Assertions;

public final class TestEvent {
    private boolean callback = false;

    public void callback() {
        this.callback = true;
    }

    public void reset() {
        this.callback = false;
    }

    public boolean wasRan() {
        return this.callback;
    }

    public void ensureRanOnce(String errorMessage) {
        if (wasRan()) Assertions.fail(errorMessage);
        callback();
    }
}
