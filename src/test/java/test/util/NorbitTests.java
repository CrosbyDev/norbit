package test.util;

import io.github.racoondog.norbit.EventBus;
import meteordevelopment.orbit.listeners.ConsumerListener;
import org.opentest4j.AssertionFailedError;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class NorbitTests {
    /**
     * @param testCase the JUnit test case to run
     * @return test builder object
     */
    public static NorbitTestBuilder test(NorbitTest testCase) {
        return new NorbitTestBuilder(testCase);
    }

    public static class NorbitTestBuilder {
        private NorbitTest testCase;
        private Class<?> eventClass = TestEvent.class;

        public NorbitTestBuilder(NorbitTest testCase) {
            this.testCase = testCase;
        }

        /**
         * @param eventClass the custom event class to use
         */
        public NorbitTestBuilder ofEvent(Class<?> eventClass) {
            this.eventClass = eventClass;
            return this;
        }

        /**
         * whether to register a lambda factory
         */
        public NorbitTestBuilder withInitialization() {
            testCase = testCase.withSetup(NorbitTests::initialize);
            return this;
        }

        /**
         * executes the test case with the given parameters
         * @throws AssertionFailedError if test case fails
         */
        public void execute() throws AssertionFailedError {
            testCase.test(EventBus.threadSafe());
            testCase.test(fill(EventBus.threadSafe(), eventClass));

            testCase.test(EventBus.threadUnsafe());
            testCase.test(fill(EventBus.threadUnsafe(), eventClass));

            // wacky combinations
            testCase.test(new EventBus(new WeakHashMap<>(), new HashMap<>(), new LinkedHashMap<>(), ArrayList::new));
            testCase.test(fill(new EventBus(new WeakHashMap<>(), new HashMap<>(), new LinkedHashMap<>(), ArrayList::new), eventClass));
        }
    }

    private static EventBus initialize(EventBus eventBus) {
        eventBus.registerLambdaFactory("test", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        return eventBus;
    }

    /**
     * Fills the {@link EventBus} with {@code 512} listeners of different priorities.
     */
    private static EventBus fill(EventBus eventBus, Class<?> eventClass) {
        for (int i = -256; i < 256; i++) {
            eventBus.subscribe(new ConsumerListener<>(eventClass, i, e -> {}));
        }
        return eventBus;
    }

    public static boolean isPrioritySorted(List<Integer> priorities) {
        int curr = Integer.MAX_VALUE;
        for (int prio : priorities) {
            if (prio > curr) return false;
            curr = prio;
        }
        return true;
    }
}
