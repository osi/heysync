package org.fotap.heysync;

import org.jetlang.channels.Publisher;
import org.jetlang.channels.Subscriber;
import org.jetlang.core.DisposingExecutor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class Dispatchers {
    private final Creators creators = new Creators();
    private final Map<Class<?>, Dispatcher<?>> dispatchers = new HashMap<Class<?>, Dispatcher<?>>();

    <T> T dispatcherFor(Class<T> type) {
        return get(type).proxy();
    }

    <T> void add(Class<T> type, T receiver, DisposingExecutor executor) {
        get(type).add(receiver, executor);
    }

    void add(Method method, Publisher<?> publisher) {
        get(method.getDeclaringClass()).add(method, publisher);
    }

    void add(Method method, Subscriber<?> subscriber) {
        get(method.getDeclaringClass()).add(method, subscriber);
    }

    private <T> Dispatcher<T> get(Class<T> type) {
        Dispatcher<T> dispatcher = Cast.as(dispatchers.get(type));

        if (null == dispatcher) {
            dispatcher = new Dispatcher<T>(type, creators);
            dispatchers.put(type, dispatcher);
        }

        return dispatcher;
    }
}
