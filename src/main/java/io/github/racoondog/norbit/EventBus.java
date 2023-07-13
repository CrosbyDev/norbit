package io.github.racoondog.norbit;

import io.github.racoondog.norbit.listeners.IListener;
import io.github.racoondog.norbit.listeners.LambdaListener;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default implementation of {@link IEventBus}.
 */
public class EventBus implements IEventBus {
    private record LambdaFactoryInfo(String packagePrefix, LambdaListener.Factory factory) {}

    private final Map<Object, List<IListener>> listenerCache = Collections.synchronizedMap(new IdentityHashMap<>());
    private final Map<Class<?>, List<IListener>> staticListenerCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, List<IListener>> listenerMap = new ConcurrentHashMap<>();

    private final List<LambdaFactoryInfo> lambdaFactoryInfos = new ArrayList<>();

    @Override
    public void registerLambdaFactory(String packagePrefix, LambdaListener.Factory factory) {
        synchronized (lambdaFactoryInfos) {
            lambdaFactoryInfos.add(new LambdaFactoryInfo(packagePrefix, factory));
        }
    }

    @Override
    public <T> T post(T event) {
        List<IListener> listeners = listenerMap.get(event.getClass());

        if (listeners != null) {
            for (IListener listener : listeners) listener.call(event);
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
        subscribe(getInstanceListeners(object), false);
    }

    @Override
    public void subscribe(Class<?> klass) {
        if (staticListenerCache.containsKey(klass)) return; // Prevent duplicate subscription
        subscribe(getStaticListeners(klass), true);
    }

    @Override
    public void subscribe(IListener listener) {
        subscribe(listener, false);
    }

    private void subscribe(List<IListener> listeners, boolean onlyStatic) {
        for (IListener listener : listeners) subscribe(listener, onlyStatic);
    }

    private void subscribe(IListener listener, boolean onlyStatic) {
        if (onlyStatic) {
            if (listener.isStatic()) insert(listenerMap.computeIfAbsent(listener.getTarget(), aClass -> new CopyOnWriteArrayList<>()), listener);
        } else insert(listenerMap.computeIfAbsent(listener.getTarget(), aClass -> new CopyOnWriteArrayList<>()), listener);
    }

    private void insert(List<IListener> listeners, IListener listener) {
        int i = 0;
        for (; i < listeners.size(); i++) {
            if (listener.getPriority() > listeners.get(i).getPriority()) break;
        }

        listeners.add(i, listener);
    }

    @Override
    public void unsubscribe(Object object) {
        List<IListener> listeners = listenerCache.get(object);
        if (listeners != null) {
            unsubscribe(listeners, false);
            listenerCache.remove(object);
        }
    }

    @Override
    public void unsubscribe(Class<?> klass) {
        List<IListener> listeners = staticListenerCache.get(klass);
        if (listeners != null) unsubscribe(listeners, true);
    }

    @Override
    public void unsubscribe(IListener listener) {
        unsubscribe(listener, false);
    }

    private void unsubscribe(List<IListener> listeners, boolean staticOnly) {
        for (IListener listener : listeners) unsubscribe(listener, staticOnly);
    }

    private void unsubscribe(IListener listener, boolean staticOnly) {
        List<IListener> l = listenerMap.get(listener.getTarget());

        if (l != null) {
            if (staticOnly) {
                if (listener.isStatic()) l.remove(listener);
            } else l.remove(listener);
        }
    }

    private List<IListener> getStaticListeners(Class<?> klass) {
        return staticListenerCache.computeIfAbsent(klass, o -> getListeners(o, null));
    }

    private List<IListener> getInstanceListeners(Object object) {
        return listenerCache.computeIfAbsent(object, o -> getListeners(o.getClass(), o));
    }

    private List<IListener> getListeners(Class<?> klass, Object object) {
        List<IListener> listeners = new ArrayList<>();
        getListeners(listeners, klass, object);
        return listeners;
    }

    private void getListeners(List<IListener> listeners, Class<?> klass, Object object) {
        while (klass != null) {
            for (Method method : klass.getDeclaredMethods()) {
                if (isValid(method)) {
                    listeners.add(new LambdaListener(getLambdaFactory(klass), klass, object, method));
                }
            }

            klass = klass.getSuperclass();
        }
    }

    private boolean isValid(Method method) {
        if (!method.isAnnotationPresent(EventHandler.class)) return false;
        if (method.getReturnType() != void.class) return false;
        if (method.getParameterCount() != 1) return false;

        return !method.getParameters()[0].getType().isPrimitive();
    }

    private LambdaListener.Factory getLambdaFactory(Class<?> klass) {
        synchronized (lambdaFactoryInfos) {
            for (LambdaFactoryInfo info : lambdaFactoryInfos) {
                if (klass.getName().startsWith(info.packagePrefix)) return info.factory;
            }
        }

        throw new NoLambdaFactoryException(klass);
    }
}
