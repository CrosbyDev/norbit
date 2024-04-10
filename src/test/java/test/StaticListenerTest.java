package test;

import meteordevelopment.orbit.EventHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.util.NorbitTests;
import test.util.TestEvent;

public class StaticListenerTest {
    @Test
    public void executeTest() {
        NorbitTests.test(eventBus -> {
            eventBus.subscribe(StaticListenerTest.class);

            TestEvent event = new TestEvent();
            eventBus.post(event);

            Assertions.assertTrue(event.wasRan(), "Static listener was not ran.");
        }).withInitialization().execute();
    }

    @EventHandler
    public void onEventNonStatic(TestEvent event) {
        Assertions.fail();
    }

    @EventHandler
    public static void onEventStatic(TestEvent event) {
        event.callback();
    }
}
