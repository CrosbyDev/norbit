package test;

import io.github.racoondog.norbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.NoLambdaFactoryException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.util.TestEvent;

public class NoLambdaFactoryTest {
    @Test
    public void executeTest() {
        EventBus eventBus = EventBus.threadSafe();

        Assertions.assertThrows(NoLambdaFactoryException.class, () -> eventBus.subscribe(this));
    }

    @EventHandler
    private void onEvent(TestEvent event) {
        Assertions.fail();
    }
}
