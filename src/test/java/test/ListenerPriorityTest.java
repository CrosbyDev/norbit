package test;

import io.github.racoondog.norbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.util.NorbitTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListenerPriorityTest {
    @Test
    public void executeTest() {
        EventBus eventBus = NorbitTests.create();
        eventBus.subscribe(this);

        TestPriorityEvent event = new TestPriorityEvent();
        eventBus.post(event);

        Integer[] priorities = event.priorities.toArray(new Integer[0]);
        Integer[] correctPriorities = new Integer[]{EventPriority.HIGHEST, EventPriority.HIGH, EventPriority.MEDIUM, EventPriority.LOW, EventPriority.LOWEST};

        Assertions.assertArrayEquals(
                priorities,
                correctPriorities,
                () -> "Listener were ran out of priority-based order: " + Arrays.toString(priorities) + " != " + Arrays.toString(correctPriorities)
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEventHighest(TestPriorityEvent event) {
        event.priorities.add(EventPriority.HIGHEST);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onEventHigh(TestPriorityEvent event) {
        event.priorities.add(EventPriority.HIGH);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @EventHandler(priority = EventPriority.MEDIUM)
    private void onEventMedium(TestPriorityEvent event) {
        event.priorities.add(EventPriority.MEDIUM);
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onEventLow(TestPriorityEvent event) {
        event.priorities.add(EventPriority.LOW);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onEventLowest(TestPriorityEvent event) {
        event.priorities.add(EventPriority.LOWEST);
    }

    private static class TestPriorityEvent {
        public final List<Integer> priorities = new ArrayList<>();
    }
}
