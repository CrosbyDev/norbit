package test;

import meteordevelopment.orbit.EventHandler;
import org.junit.jupiter.api.Test;
import test.util.NorbitTests;
import test.util.TestEvent;

public class UnsubscribeTest {
    @Test
    public void executeTest() {
        NorbitTests.test(eventBus -> {
            eventBus.subscribe(this);

            TestEvent event = new TestEvent();
            eventBus.post(event);

            eventBus.unsubscribe(this);

            eventBus.post(event);
        }).withInitialization().execute();
    }

    @EventHandler
    private void onEvent(TestEvent event) {
        event.ensureRanOnce("Listener was not unsubscribed correctly.");
    }
}
