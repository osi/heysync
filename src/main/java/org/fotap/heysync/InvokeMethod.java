package org.fotap.heysync;

import org.jetlang.core.Callback;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class InvokeMethod<T> implements Callback<Object[]> {
    private final Method method;
    private final T receiver;

    public InvokeMethod(Method method, T receiver) {
        this.method = method;
        this.receiver = receiver;
    }

    @Override
    public void onMessage(Object[] message) {
        try {
            method.invoke(receiver, message);
        } catch (Exception e) {
            // TODO better error handling
            throw new RuntimeException(e);
        }
    }
}
