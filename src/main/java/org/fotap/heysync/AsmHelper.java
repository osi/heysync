package org.fotap.heysync;

import org.jetlang.channels.Publisher;
import org.jetlang.core.Callback;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class AsmHelper {
    static final Logger logger = LoggerFactory.getLogger(PublisherCreator.class);
    static final Type publisherType;
    static final org.objectweb.asm.commons.Method publishMethod;
    static final Type objectType;
    static final org.objectweb.asm.commons.Method defaultConstructor;
    static final org.objectweb.asm.commons.Method toString;

    static {
        publisherType = Type.getType(Publisher.class);
        objectType = Type.getType(Object.class);

        try {
            publishMethod = asmMethod(Publisher.class.getMethod("publish", Object.class));
            defaultConstructor = org.objectweb.asm.commons.Method.getMethod(Object.class.getConstructor());
            toString = asmMethod(Object.class.getMethod("toString"));
        } catch (NoSuchMethodException e) {
            AsmHelper.logger.error("Required members missing", e);
            throw new IllegalStateException("Required members missing", e);
        }
    }

    static org.objectweb.asm.commons.Method asmMethod(Method method) {
        return org.objectweb.asm.commons.Method.getMethod(method);
    }

    static <T> Class<Callback<T>> callback() {
        Class<?> callbackClass = Callback.class;
        @SuppressWarnings({"unchecked"}) Class<Callback<T>> type = (Class<Callback<T>>) callbackClass;
        return type;
    }

    static org.objectweb.asm.commons.Method asmMethod(String declaration) {
        return org.objectweb.asm.commons.Method.getMethod(declaration);
    }
}
