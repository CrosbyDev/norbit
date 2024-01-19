package io.github.racoondog.norbit;

import meteordevelopment.orbit.listeners.IListener;

import java.util.List;

public class ListOperations {
    /**
     * {@link List#remove(Object)} with referential equality and short-circuit based on priority
     */
    public static IListener remove(List<IListener> listeners, IListener listener) {
        int priority = listener.getPriority();
        for (int i = 0; i < listeners.size(); i++) {
            IListener o = listeners.get(i);
            if (o == listener) return listeners.remove(i);
            if (o.getPriority() < priority) break;
        }
        return null;
    }

    /**
     * {@link List#contains(Object)} with referential equality and short-circuit based on priority
     */
    public static boolean contains(List<IListener> listeners, IListener listener) {
        int priority = listener.getPriority();
        for (int i = 0; i < listeners.size(); i++) {
            IListener o = listeners.get(i);
            if (o == listener) return true;
            if (o.getPriority() < priority) break;
        }
        return false;
    }

    /**
     * Binary search insertion based on priority
     */
    public static void insert(List<IListener> listeners, IListener listener) {
        int index = binarySearch(listeners, listener);
        listeners.add(index, listener);
    }

    private static int binarySearch(List<IListener> listeners, IListener listener) {
        int low = 0;
        int high = listeners.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            IListener midElement = listeners.get(mid);

            int comp = Integer.compare(listener.getPriority(), midElement.getPriority());

            if (comp == 0) return mid;
            else if (comp < 0) low = mid + 1;
            else high = mid - 1;
        }

        return low;
    }
}
