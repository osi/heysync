package org.fotap.heysync;

import org.jetlang.channels.Publisher;
import org.jetlang.channels.Subscriber;
import org.jetlang.core.DisposingExecutor;

import java.lang.reflect.Method;

/**
 * A communications hub. Manages <i>dispatchers</i> and <i>receivers</i> for classes. Behind the scenes method
 * invocations are tunneled along <a href="http://jetlang.org">jetlang</a> channels.
 * <p/>
 * Method invocations on a dispatcher will result in the same method being invoked on all registered receivers.
 *
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
public class Hub {
    private final Dispatchers dispatchers = new Dispatchers();

    /**
     * Get the dispatcher for the specified type.
     *
     * @param type Class to get dispatcher for. It must be an interface marked with the {@link Asynchronous} annotation
     * @return Dispatcher instance.
     */
    public <T> T dispatcherFor(Class<T> type) {
        if (!isAsynchronable(type)) {
            throw new IllegalArgumentException(
                String.format("Cannot create a dispatcher for %s. " +
                              "It must be an interface that is marked with the %s annotation",
                              type.getName(),
                              Asynchronous.class.getName()));
        }

        return dispatchers.dispatcherFor(type);
    }

    /**
     * Add the specified class as a receiver. It must implement at least one {@link Asynchronous} interface.
     *
     * @param receiver Receiver to add
     * @param executor DisposingExecutor to use for executing invocations
     */
    public <T> void addReceiver(T receiver, DisposingExecutor executor) {
        if (!processInterfaces(receiver.getClass(), receiver, executor)) {
            throw new IllegalArgumentException(String.format("%s does not implement any %s interfaces",
                                                             receiver.getClass().getName(),
                                                             Asynchronous.class.getName()));
        }
    }

    /**
     * Add an explicit receiver to calls of a method. The publisher will be invoked for every call of the supplied
     * method on the dispatcher.
     *
     * @param method    Method to add explicit receiver to
     * @param publisher Publisher that will receive invocations of the supplied method
     */
    public <T> void addPublisher(Method method, Publisher<T> publisher) {
        if (!isAsynchronable(method.getDeclaringClass())) {
            throw new IllegalArgumentException(
                String.format("Cannot add %s as a receiver. It must be part of an interface that is " +
                              "marked with the %s annotation",
                              method.toGenericString(),
                              Asynchronous.class.getName()));
        }

        validateMethod(method);

        dispatchers.add(method, publisher);
    }

    /**
     * Add an explicit subscriber to. The subscriber will be invoked for every receiver that is added for the
     * supplied method
     *
     * @param method     Method to add explicit subscriber to
     * @param subscriber Subscriber that will receive subscriptions for all receivers
     */
    public <T> void addSubscriber(Method method, Subscriber<T> subscriber) {
        if (!isAsynchronable(method.getDeclaringClass())) {
            throw new IllegalArgumentException(
                String.format("Cannot add %s as a publisher. It must be part of an interface that is " +
                              "marked with the %s annotation",
                              method.toGenericString(),
                              Asynchronous.class.getName()));
        }

        validateMethod(method);

        dispatchers.add(method, subscriber);
    }

    private <T> boolean processInterfaces(Class<?> type, T receiver, DisposingExecutor executor) {
        boolean any = false;
        for (Class<?> clazz : type.getInterfaces()) {
            if (isAsynchronable(clazz)) {
                dispatchers.add(Cast.<Class<T>>as(clazz), receiver, executor);
                any = true;
            }

            if (processInterfaces(clazz, receiver, executor)) {
                any = true;
            }
        }

        return any;
    }

    private static boolean isAsynchronable(Class<?> type) {
        return validateClass(type) && validateMethods(type);
    }

    private static boolean validateMethods(Class<?> type) {
        for (Method method : type.getMethods()) {
            validateMethod(method);
        }

        return true;
    }

    private static void validateMethod(Method method) {
        Class<?>[] parameters = method.getParameterTypes();

        if (!method.getReturnType().equals(Void.TYPE)) {
            throw new IllegalArgumentException(String.format(
                "Cannot create a dispatcher for %s because it does not return void",
                method.toGenericString()));
        } else if (parameters.length != 1) {
            throw new IllegalArgumentException(String.format(
                "Cannot create a dispatcher for %s because its parameter count is not equal to one",
                method.toGenericString()));
        } else if (parameters[0].isPrimitive()) {
            throw new IllegalArgumentException(String.format(
                "Cannot create a dispatcher for %s because its parameter is a primitive type",
                method.toGenericString()));
        }
    }

    private static boolean validateClass(Class<?> type) {
        return type.isInterface() && null != type.getAnnotation(Asynchronous.class);
    }
}
