package test;

import io.github.racoondog.norbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import org.junit.jupiter.api.Test;
import test.util.NorbitTests;
import test.util.TestEvent;

public class UnsubscribeTest {
    @Test
    public void executeTest() {
        EventBus eventBus = NorbitTests.create();
        eventBus.subscribe(this);

        TestEvent event = new TestEvent();
        eventBus.post(event);

        eventBus.unsubscribe(this);

        eventBus.post(event);
    }

    @EventHandler
    private void onEvent(TestEvent event) {
        event.ensureRanOnce("Listener was not unsubscribed correctly.");
    }
}
