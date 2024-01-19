package test;

import io.github.racoondog.norbit.EventBus;
import meteordevelopment.orbit.listeners.ConsumerListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.util.TestEvent;

public class ConsumerListenerTest {
    @Test
    public void executeTest() {
        EventBus eventBus = EventBus.threadSafe();
        eventBus.subscribe(new ConsumerListener<>(TestEvent.class, TestEvent::callback));

        TestEvent event = new TestEvent();
        eventBus.post(event);

        Assertions.assertTrue(event.wasRan(), "ConsumerListener was not ran.");
    }
}
