package org.fotap.heysync;

import org.jetlang.channels.Channel;
import org.jetlang.channels.MemoryChannel;
import org.jetlang.core.DisposingExecutor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class Dispatcher<T> {
    private final Map<Method, Channel<Object>> channels;
    private final T proxy;
    private final Creators creators;

    Dispatcher(Class<T> type, Creators creators) {
        this.creators = creators;
        channels = createChannels(type);
        proxy = creators.publisherFor(type, channels);
    }

    private Map<Method, Channel<Object>> createChannels(Class<T> type) {
        Method[] methods = type.getDeclaredMethods();
        Map<Method, Channel<Object>> channels = new HashMap<Method, Channel<Object>>(methods.length);
        for (Method method : methods) {
            channels.put(method, new MemoryChannel<Object>());
        }
        return channels;
    }

    T proxy() {
        return proxy;
    }

    <T> void add(T receiver, DisposingExecutor executor) {
        for (Map.Entry<Method, Channel<Object>> entry : channels.entrySet()) {
            Method method = entry.getKey();
            Channel<Object> channel = entry.getValue();
            channel.subscribe(executor, creators.callbackFor(method, receiver));
        }
    }
}
