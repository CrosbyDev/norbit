package test;

import meteordevelopment.orbit.listeners.ConsumerListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.util.NorbitTests;
import test.util.TestEvent;

public class ConsumerListenerTest {
    @Test
    public void executeTest() {
        NorbitTests.test(eventBus -> {
            eventBus.subscribe(new ConsumerListener<>(TestEvent.class, TestEvent::callback));

            TestEvent event = new TestEvent();
            eventBus.post(event);

            Assertions.assertTrue(event.wasRan(), "ConsumerListener was not ran.");
        }).withInitialization().execute();
    }
}
