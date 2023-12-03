package test;

import io.github.racoondog.norbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;

public class UnsubscribeTest {
    @Test
    public void executeTest() {
        EventBus eventBus = EventBus.threadSafe();
        eventBus.registerLambdaFactory("test", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        eventBus.subscribe(this);

        TestEvent event = new TestEvent();
        eventBus.post(event);

        eventBus.unsubscribe(this);

        eventBus.post(event);
    }

    @EventHandler
    private void onEvent(TestEvent event) {
        if (event.wasRan()) Assertions.fail();
        event.callback();
    }
}
