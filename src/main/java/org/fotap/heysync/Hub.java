package org.fotap.heysync;

import org.jetlang.core.DisposingExecutor;

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
                String.format("%s does not have a %s annotation", type.getName(), Asynchronous.class.getName()));
    }

    public <T> void addReceiver(T receiver, DisposingExecutor executor) {
        processInterfaces(receiver.getClass(), receiver, executor);
    }

    private <T> void processInterfaces(Class<?> type, T receiver, DisposingExecutor executor) {
        for (Class<?> clazz : type.getInterfaces()) {
            if (isAsynchronable(clazz)) {
                dispatchers.add(clazz, receiver, executor);
            }

            processInterfaces(clazz, receiver, executor);
        }
    }

    private static boolean isAsynchronable(Class<?> type) {
        return type.isInterface() && null != type.getAnnotation(Asynchronous.class);
    }
}
