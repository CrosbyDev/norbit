package test;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.NoLambdaFactoryException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import test.util.NorbitAccess;
import test.util.NorbitTests;
import test.util.TestEvent;

public class NoLambdaFactoryTest {
    @Test
    public void executeTest() {
        NorbitTests.test(eventBus -> {
            Assumptions.assumeTrue(NorbitAccess.getCurrentImplType().requireLambdaFactory);

            Assertions.assertThrows(NoLambdaFactoryException.class, () -> eventBus.subscribe(this));
        }).execute();
    }

    @EventHandler
    private void onEvent(TestEvent event) {
        Assertions.fail();
    }
}
