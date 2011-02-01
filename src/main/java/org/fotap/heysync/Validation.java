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
        Class<?>[] parameters = method.getParameterTypes();

        if (!method.getReturnType().equals(Void.TYPE)) {
            throw new IllegalArgumentException(String.format(
                "Cannot create a dispatcher for %s because it does not return void",
                method.toGenericString()));
        } else if (parameters.length > 1) {
            throw new IllegalArgumentException(String.format(
                "Cannot create a dispatcher for %s because it takes more than one parameter",
                method.toGenericString()));
        }
    }

    private static boolean validateClass(Class<?> type) {
        return type.isInterface() && null != type.getAnnotation(Asynchronous.class);
    }
}
