package org.fotap.heysync;

import org.jetlang.core.DisposingExecutor;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
public class Hub {
    private final Dispatchers dispatchers = new Dispatchers();

    public <T> T dispatcherFor(Class<T> type) {
        if (isAsynchronable(type)) {
            return dispatchers.dispatcherFor(type);
        }

        throw new IllegalArgumentException(
            String.format("Cannot create a dispatcher for %s. " +
                          "It must be an interface that is marked with the %s annotation",
                          type.getName(),
                          Asynchronous.class.getName()));
    }

    public <T> void addReceiver(T receiver, DisposingExecutor executor) {
        if (!processInterfaces(receiver.getClass(), receiver, executor)) {
            throw new IllegalArgumentException(String.format("%s does not implement any %s interfaces",
                                                             receiver.getClass().getName(),
                                                             Asynchronous.class.getName()));
        }
    }

    private <T> boolean processInterfaces(Class<?> type, T receiver, DisposingExecutor executor) {
        boolean any = false;
        for (Class<?> clazz : type.getInterfaces()) {
            if (isAsynchronable(clazz)) {
                dispatchers.add(clazz, receiver, executor);
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
