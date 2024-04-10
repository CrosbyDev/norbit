package test;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.listeners.ConsumerListener;
import org.junit.jupiter.api.Test;
import test.util.NorbitTests;
import test.util.TestEvent;

public class ResubscribeTest {
    @Test
    public void executeTest() {
        NorbitTests.test(eventBus -> {
            // non-static
            Instance instance = new Instance();
            eventBus.subscribe(instance);
            eventBus.subscribe(instance);

            eventBus.post(new TestEvent());

            eventBus.unsubscribe(instance);

            // static
            eventBus.subscribe(Static.class);
            eventBus.subscribe(Static.class);

            eventBus.post(new TestEvent());

            eventBus.unsubscribe(Static.class);

            // consumer listener
            ConsumerListener<TestEvent> consumerListener = new ConsumerListener<>(TestEvent.class, event -> event.ensureRanOnce("Identical consumer listeners subscribed twice."));
            eventBus.subscribe(consumerListener);
            eventBus.subscribe(consumerListener);

            eventBus.post(new TestEvent());

            eventBus.unsubscribe(consumerListener);
        }).withInitialization().execute();
    }

    private static class Instance {
        @EventHandler
        private void onEvent(TestEvent event) {
            event.ensureRanOnce("Identical listeners subscribed twice.");
        }
    }

    private static class Static {
        @EventHandler
        private static void onEvent(TestEvent event) {
            event.ensureRanOnce("Identical static listeners subscribed twice.");
        }
    }
}
