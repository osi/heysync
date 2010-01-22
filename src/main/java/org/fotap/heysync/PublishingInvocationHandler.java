package org.fotap.heysync;

import org.jetlang.channels.Channel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class PublishingInvocationHandler implements InvocationHandler {
    private final Map<Method, Channel<Object[]>> channels;

    public PublishingInvocationHandler(Map<Method, Channel<Object[]>> channels) {
        this.channels = channels;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        channels.get(method).publish(args);
        return null;
    }
}
