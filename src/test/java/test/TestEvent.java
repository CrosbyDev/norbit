package test;

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
}
