package io.github.racoondog.norbit;

import io.github.racoondog.norbit.listeners.IListener;
import io.github.racoondog.norbit.listeners.LambdaListener;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * Default implementation of {@link IEventBus}.
 */
public class EventBus implements IEventBus {
    private record LambdaFactoryInfo(String packagePrefix, LambdaListener.Factory factory) {}

    private final Map<Object, List<IListener>> listenerCache;
    private final Map<Class<?>, List<IListener>> staticListenerCache;

    private final Map<Class<?>, List<IListener>> listenerMap;
    private final Supplier<List<IListener>> listenerListFactory;

    private final List<LambdaFactoryInfo> lambdaFactoryInfos = new CopyOnWriteArrayList<>();

    public EventBus(Map<Object, List<IListener>> listenerCache, Map<Class<?>, List<IListener>> staticListenerCache, Map<Class<?>, List<IListener>> listenerMap, Supplier<List<IListener>> listenerListFactory) {
        this.listenerCache = listenerCache;
        this.staticListenerCache = staticListenerCache;
        this.listenerMap = listenerMap;
        this.listenerListFactory = listenerListFactory;
    }

    public static EventBus threadSafe() {
        return new EventBus(Collections.synchronizedMap(new IdentityHashMap<>()), new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), CopyOnWriteArrayList::new);
    }

    public static EventBus threadUnsafe() {
        return new EventBus(new IdentityHashMap<>(), new HashMap<>(), new HashMap<>(), ArrayList::new);
    }

    @Override
    public void registerLambdaFactory(String packagePrefix, LambdaListener.Factory factory) {
        lambdaFactoryInfos.add(new LambdaFactoryInfo(packagePrefix, factory));
    }

    @Override
    public <T> T post(T event) {
        List<IListener> listeners = listenerMap.get(event.getClass());

        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).call(event);
            }
        }

        return event;
    }

    @Override
    public <T extends ICancellable> T post(T event) {
        List<IListener> listeners = listenerMap.get(event.getClass());

        if (listeners != null) {
            event.setCancelled(false);

            for (IListener listener : listeners) {
                listener.call(event);
                if (event.isCancelled()) break;
            }
        }

        return event;
    }

    @Override
    public void subscribe(Object object) {
        if (listenerCache.containsKey(object)) return; // Prevent duplicate subscription
        subscribe(getInstanceListeners(object));
    }

    @Override
    public void subscribe(Class<?> klass) {
        subscribe(getStaticListeners(klass));
    }

    @Override
    public void subscribe(IListener listener) {
        insert(listenerMap.computeIfAbsent(listener.getTarget(), klass -> listenerListFactory.get()), listener);
    }

    private void subscribe(List<IListener> listeners) {
        for (IListener listener : listeners) subscribe(listener);
    }

    private void insert(List<IListener> listeners, IListener listener) {
        // Binary search algorithm
        int low = 0;
        int high = listeners.size() - 1;

        while (low < high) {
            int mid = (low + high) >>> 1;
            int priority = listeners.get(mid).getPriority();

            if (priority > listener.getPriority()) {
                low = mid + 1;
            } else if (priority < listener.getPriority()) {
                high = mid - 1;
            } else {
                listeners.add(mid, listener);
                return;
            }
        }

        listeners.add(low, listener);
    }

    @Override
    public void unsubscribe(Object object) {
        List<IListener> listeners = listenerCache.get(object);
        if (listeners != null) {
            unsubscribe(listeners);
            listenerCache.remove(object);
        }
    }

    @Override
    public void unsubscribe(Class<?> klass) {
        List<IListener> listeners = staticListenerCache.get(klass);
        if (listeners != null) unsubscribe(listeners);
    }

    @Override
    public void unsubscribe(IListener listener) {
        List<IListener> l = listenerMap.get(listener.getTarget());
        if (l != null) l.remove(listener);
    }

    private void unsubscribe(List<IListener> listeners) {
        for (IListener listener : listeners) unsubscribe(listener);
    }

    private List<IListener> getStaticListeners(Class<?> klass) {
        return staticListenerCache.computeIfAbsent(klass, o -> getListeners(o, null, true));
    }

    private List<IListener> getInstanceListeners(Object object) {
        return listenerCache.computeIfAbsent(object, o -> getListeners(o.getClass(), o, false));
    }

    private List<IListener> getListeners(Class<?> klass, Object object, boolean staticOnly) {
        List<IListener> listeners = new ArrayList<>();
        getListeners(listeners, klass, object, staticOnly);
        return listeners;
    }

    private void getListeners(List<IListener> listeners, Class<?> klass, Object object, boolean staticOnly) {
        while (klass != null) {
            LambdaListener.Factory factory = null;

            for (var method : klass.getDeclaredMethods()) {
                if (isValid(method, staticOnly)) {
                    if (factory == null) factory = getLambdaFactory(klass); //Lazy-loaded
                    listeners.add(new LambdaListener(factory, klass, object, method));
                }
            }

            klass = klass.getSuperclass();
        }
    }

    private boolean isValid(Method method, boolean staticOnly) {
        if (staticOnly && !Modifier.isStatic(method.getModifiers())) return false;
        if (!method.isAnnotationPresent(EventHandler.class)) return false;
        if (method.getReturnType() != void.class) return false;
        if (method.getParameterCount() != 1) return false;

        return !method.getParameters()[0].getType().isPrimitive();
    }

    private LambdaListener.Factory getLambdaFactory(Class<?> klass) {
        for (LambdaFactoryInfo info : lambdaFactoryInfos) {
            if (klass.getName().startsWith(info.packagePrefix)) return info.factory;
        }

        throw new NoLambdaFactoryException(klass);
    }
}
