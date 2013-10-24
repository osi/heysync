package org.fotap.heysync;

import org.jetlang.channels.Publisher;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class AsmHelper {
    static final Type publisherType;
    static final org.objectweb.asm.commons.Method publishMethod;
    static final Type objectType;
    static final org.objectweb.asm.commons.Method defaultConstructor;
    static final org.objectweb.asm.commons.Method toString;
    static final Type stringBuilder;
    static final org.objectweb.asm.commons.Method stringBuilderConstructor;
    static final org.objectweb.asm.commons.Method appendString;
    static final org.objectweb.asm.commons.Method appendObject;

    static {
        publisherType = Type.getType(Publisher.class);
        objectType = Type.getType(Object.class);
        stringBuilder = Type.getType(StringBuilder.class);

        try {
            publishMethod = asmMethod(Publisher.class.getMethod("publish", Object.class));
            defaultConstructor = org.objectweb.asm.commons.Method.getMethod(Object.class.getConstructor());
            toString = asmMethod(Object.class.getMethod("toString"));
            appendString = asmMethod(StringBuilder.class.getMethod("append", String.class));
            appendObject = asmMethod(StringBuilder.class.getMethod("append", Object.class));
            stringBuilderConstructor = asmMethod(StringBuilder.class.getConstructor());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Required members missing", e);
        }
    }

    static org.objectweb.asm.commons.Method asmMethod(Method method) {
        return org.objectweb.asm.commons.Method.getMethod(method);
    }

    static org.objectweb.asm.commons.Method asmMethod(Constructor constructor) {
        return org.objectweb.asm.commons.Method.getMethod(constructor);
    }

    static org.objectweb.asm.commons.Method asmMethod(String declaration) {
        return org.objectweb.asm.commons.Method.getMethod(declaration);
    }
}
