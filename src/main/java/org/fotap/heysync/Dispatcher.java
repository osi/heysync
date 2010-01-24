package org.fotap.heysync;

import org.jetlang.channels.Channel;
import org.jetlang.channels.MemoryChannel;
import org.jetlang.channels.Publisher;
import org.jetlang.channels.Subscriber;
import org.jetlang.core.DisposingExecutor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class Dispatcher<T> {
    private final Map<Method, Subscriber<Object>> subscribers;
    private final Map<Method, Publisher<Object>> publishers;
    private final Class<T> type;
    private final Creators creators;
    private boolean receiversAdded;
    private T proxy;

    Dispatcher(Class<T> type, Creators creators) {
        this.type = type;
        this.creators = creators;

        Map<Method, Channel<Object>> channels = createChannels(type);
        this.subscribers = new HashMap<Method, Subscriber<Object>>(channels);
        this.publishers = new HashMap<Method, Publisher<Object>>(channels);
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
        if (null == proxy) {
            proxy = creators.publisherFor(type, publishers);
        }
        return proxy;
    }

    void add(T receiver, DisposingExecutor executor) {
        receiversAdded = true;
        for (Map.Entry<Method, Subscriber<Object>> entry : subscribers.entrySet()) {
            Method method = entry.getKey();
            Subscriber<Object> subscriber = entry.getValue();
            subscriber.subscribe(executor, creators.callbackFor(method, receiver));
        }
    }

    void add(Method method, Publisher<?> publisher) {
        if (null != proxy) {
            throw new IllegalStateException("Cannot add explicit Publisher after retrieving the dispatcher");
        }

        publishers.put(method,
                       new DualPublisher<Object>(Cast.<Publisher<Object>>as(publisher), publishers.get(method)));
    }

    void add(Method method, Subscriber<?> subscriber) {
        if (receiversAdded) {
            throw new IllegalStateException("Cannot add explicit Subscriber after adding receivers");
        }

        subscribers.put(method,
                        new DualSubscriber<Object>(Cast.<Subscriber<Object>>as(subscriber), subscribers.get(method)));
    }
}
