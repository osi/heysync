package org.fotap.heysync;

import org.jetlang.channels.Publisher;
import org.jetlang.core.Callback;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class ClassCreatingClassloader<T> extends ClassLoader {

    private final Type publisherType;
    private final Class<? extends T> publisherClass;
    private final Map<Method, Class<? extends Callback>> subscriberClasses;
    private final List<Method> methods;

    ClassCreatingClassloader(Class<T> type) {
        methods = uniqueMethodsForType(type);
        publisherType = publisherTypeFor(type);
        publisherClass = defineClass(new PublisherCreator<T>(type, publisherType, methods));
        subscriberClasses = new HashMap<>(methods.size());

        for (Method method : methods) {
            Type outputType = callbackTypeFor(method);
            subscriberClasses.put(method, defineClass(new CallbackCreator(outputType, method)));
        }
    }

    private static <T> List<Method> uniqueMethodsForType(Class<T> type) {
        Method[] methods = type.getMethods();
        List<Method> uniqueMethods = new ArrayList<Method>(methods.length);
        Set<org.objectweb.asm.commons.Method> unique = new HashSet<org.objectweb.asm.commons.Method>();
        for (Method method : type.getMethods()) {
            if (unique.add(org.objectweb.asm.commons.Method.getMethod(method))) {
                uniqueMethods.add(method);
            }
        }
        return uniqueMethods;
    }

    List<Method> methods() {
        return methods;
    }

    T publisher(Map<Method, ? extends Publisher<?>> publishers) {
        return newInstance(publisherClass, publisherArguments(publishers));
    }

    private static Type publisherTypeFor(Class<?> type) {
        return Type.getType("L" + Type.getInternalName(type) + "$Publisher;");
    }

    <T, R> Callback<R> callbackFor(Method method, T receiver) {
        return newInstance(subscriberClasses.get(method), receiver);
    }

    private Object[] publisherArguments(Map<Method, ? extends Publisher<?>> publishers) {
        List<Publisher<?>> arguments = new ArrayList<Publisher<?>>();
        for (Method method : methods) {
            arguments.add(publishers.get(method));
        }
        return arguments.toArray();
    }

    private <T> T newInstance(Class<? extends T> type, Object... initargs) {
        return new Instantiator<T>(type, initargs).newInstance();
    }

    private <T> Class<? extends T> defineClass(ClassCreator<T> creator) {
        byte[] bytes = creator.bytes();
//        new ClassReader(bytes).accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out)),
//                ClassReader.SKIP_DEBUG);
        Class<?> type = defineClass(creator.outputType().getClassName(), bytes, 0, bytes.length);
        resolveClass(type);
        return type.asSubclass(creator.type());
    }

    private static Type callbackTypeFor(Method method) {
        StringBuilder builder = new StringBuilder(256)
                .append("L")
                .append(Type.getInternalName(method.getDeclaringClass()))
                .append("$Callback$")
                .append(method.getName());

        for (Class<?> type : method.getParameterTypes()) {
            builder.append("$").append(type.getName().replace('.', '$').replace(':', '$').replace('[', '$').replace(']', '$'));
        }

        builder.append(";");

        return Type.getType(builder.toString());
    }
}
