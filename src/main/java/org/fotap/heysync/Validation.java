package org.fotap.heysync;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class Validation {
    static boolean isAsynchronable(Class<?> type) {
        return validateClass(type) && validateMethods(type);
    }

    private static boolean validateMethods(Class<?> type) {
        for (Method method : type.getMethods()) {
            validateMethod(method);
        }

        return true;
    }

    static void validateMethod(Method method) {
        if (!method.getReturnType().equals(Void.TYPE)) {
            throw new IllegalArgumentException(String.format(
                "Cannot create a dispatcher for %s because it does not return void",
                method.toGenericString()));
        }
    }

    private static boolean validateClass(Class<?> type) {
        return type.isInterface() && null != type.getAnnotation(Asynchronous.class);
    }
}
