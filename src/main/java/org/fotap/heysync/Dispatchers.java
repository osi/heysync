package org.fotap.heysync;

import org.jetlang.core.DisposingExecutor;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class Dispatchers {
    private final ClassLoader loader = new URLClassLoader(new URL[0]);
    private final Map<Class<?>, Dispatcher<?>> dispatchers = new HashMap<Class<?>, Dispatcher<?>>();

    <T> T dispatcherFor(Class<T> type) {
        return get(type).proxy();
    }

    <T> void add(Class<?> type, T receiver, DisposingExecutor executor) {
        get(type).add(receiver, executor);
    }

    private <T> Dispatcher<T> get(Class<T> type) {
        @SuppressWarnings({"unchecked"}) Dispatcher<T> dispatcher = (Dispatcher<T>) dispatchers.get(type);

        if (null == dispatcher) {
            dispatcher = new Dispatcher<T>(type, loader);
            dispatchers.put(type, dispatcher);
        }

        return dispatcher;
    }
}
