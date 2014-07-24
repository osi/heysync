package org.fotap.heysync;

import org.jetlang.channels.Channel;
import org.jetlang.channels.MemoryChannel;
import org.jetlang.channels.Subscriber;
import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.core.DisposingExecutor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fotap.heysync.Cast.as;
import static org.fotap.heysync.Validation.isAsynchronable;

/**
 * A dynamic implementation of a {@link Asynchronous} interface.
 * <p/>
 * Method invocations on the interface are dispatched on <a href="http://jetlang.org">jetlang</a> channels to
 * all registered subscribers.
 * <p/>
 * Method invocations on a dispatcher will result in the same method being invoked on all registered receivers.
 * <p/>
 * If creating multiple instances of a protocol, consider using a {@link Factory} to re-use generated classes
 *
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
public class Protocol<T> {
    private final Map<Method, Channel<Object>> channels;
    private final ClassCreatingClassLoader<T> loader;
    private final Class<T> type;
    private final T proxy;

    public static <T> Protocol<T> create(Class<T> type) {
        validate(type);
        return new Protocol<>(type, new ClassCreatingClassLoader<>(type));
    }

    /**
     * Create multiple protocols that share the same class loader for generated code
     */
    public static class Factory<T> {
        private final ClassCreatingClassLoader<T> loader;
        private final Class<T> type;

        public static <T> Factory<T> create(Class<T> type) {
            validate(type);
            return new Factory<>(type);
        }

        private Factory(Class<T> type) {
            this.type = type;
            this.loader = new ClassCreatingClassLoader<>(type);
        }

        public Protocol<T> create() {
            return new Protocol<>(type, loader);
        }
    }

    private Protocol(Class<T> type, ClassCreatingClassLoader<T> loader) {
        this.type = type;
        this.channels = createChannels(loader.methods());
        this.loader = loader;
        this.proxy = loader.publisher(channels);
    }

    private Map<Method, Channel<Object>> createChannels(List<Method> methods) {
        Map<Method, Channel<Object>> channels = new HashMap<>(methods.size());
        for (Method method : methods) {
            channels.put(method, new MemoryChannel<>());
        }
        return channels;
    }

    /**
     * Get the interface that represents this protocol that can be used to publish messages
     *
     * @return Publisher instance
     */
    public T publisher() {
        return proxy;
    }

    /**
     * Subscribe the specified receiver to this protocol on the specified executor
     *
     * @param executor Executor to use for execution tasks
     * @param receiver Receiver
     * @return Disposable that can be used to cancel the subscription
     */
    public Disposable subscribe(DisposingExecutor executor, T receiver) {
        CompositeDisposable disposables = new CompositeDisposable();
        for (Map.Entry<Method, Channel<Object>> entry : channels.entrySet()) {
            Method method = entry.getKey();
            Subscriber<Object> subscriber = entry.getValue();
            Callback<Object> callback = loader.callbackFor(method, receiver);
            if (method.isAnnotationPresent(LatestOnly.class)) {
                disposables.add(subscriber.subscribe(new LatestOnlySubscriber<>(executor, callback)));
            } else {
                disposables.add(subscriber.subscribe(executor, callback));
            }
        }

        return disposables;
    }

    public <ParamType> Channel<ParamType> channelFor(Method method, Class<ParamType> parameterType) {
        if (method.getParameterTypes().length > 1) {
            if (!Object[].class.equals(parameterType)) {
                throw new IllegalArgumentException("Must specify java.lang.Object[] as parameter type on multiple arg method: "
                        + method.toGenericString());
            }
        } else if (method.getParameterTypes().length == 0) {
            if (!Object.class.equals(parameterType)) {
                throw new IllegalArgumentException("Must specify java.lang.Object as parameter type on no-arg method: "
                        + method.toGenericString());
            }
        } else if (!method.getParameterTypes()[0].equals(parameterType)) {
            throw new IllegalArgumentException("Specified parameter type " + parameterType.getName()
                    + " is not what the method requires: " + method.toGenericString());
        }

        Channel<Object> channel = channels.get(method);

        if (null == channel) {
            throw new IllegalArgumentException(method.toGenericString() + " is not a method on " + type.getName());
        }

        return as(channel);
    }

    private static <T> void validate(Class<T> type) {
        if (!isAsynchronable(type)) {
            throw new IllegalArgumentException(
                    String.format("Cannot create a protocol for %s. " +
                                    "It must be an interface that is marked with the %s annotation",
                            type.getName(),
                            Asynchronous.class.getName()));
        }
    }
}
