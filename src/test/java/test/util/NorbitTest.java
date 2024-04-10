package test.util;

import io.github.racoondog.norbit.EventBus;
import org.opentest4j.AssertionFailedError;

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface NorbitTest {
    void test(EventBus eventBus) throws AssertionFailedError;

    default NorbitTest withSetup(UnaryOperator<EventBus> setup) {
        return eventBus -> test(setup.apply(eventBus));
    }
}
