package org.fotap.heysync;

import org.jetlang.channels.Publisher;
import org.jetlang.core.Callback;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class ClassCreatingClassloader extends ClassLoader {

    <T> T publisherFor(Class<T> type, Map<Method, ? extends Publisher<?>> publishers) {
        List<Method> methods = new ArrayList<Method>(publishers.keySet());
        Type outputType = publisherTypeFor(type);
        return newInstance(loadOrDefine(outputType, new PublisherCreator<T>(type, outputType, methods)),
                publisherArguments(publishers, methods));
    }

    private static Type publisherTypeFor(Class<?> type) {
        return Type.getType("L" + Type.getInternalName(type) + "$Publisher;");
    }

    <T, R> Callback<R> callbackFor(Method method, T receiver) {
        Type outputType = callbackTypeFor(method);
        Class<? extends Callback<R>> type = loadOrDefine(outputType, new CallbackCreator<R>(outputType, method));
        return newInstance(type, receiver);
    }

    private Object[] publisherArguments(Map<Method, ? extends Publisher<?>> publishers, List<Method> methods) {
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
// new ClassReader(bytes).accept(new ASMifierClassVisitor(new PrintWriter(System.out)), ClassReader.SKIP_DEBUG );
        Class<?> type = defineClass(creator.outputType().getClassName(), bytes, 0, bytes.length);
        resolveClass(type);
        return type.asSubclass(creator.type());
    }

    private <T> Class<? extends T> loadOrDefine(Type outputType, ClassCreator<T> creator) {
        Class<?> callbackClass = findLoadedClass(outputType.getClassName());
        if (null != callbackClass) {
            return Cast.as(callbackClass);
        }
        return defineClass(creator);
    }

    private Type callbackTypeFor(Method method) {
        StringBuilder builder = new StringBuilder()
                .append("L")
                .append(Type.getInternalName(method.getDeclaringClass()))
                .append("$Callback$")
                .append(method.getName());

        for (Class<?> type : method.getParameterTypes()) {
            builder.append("$").append(type.getName().replace('.', '$'));
        }

        builder.append(";");

        return Type.getType(builder.toString());
    }
}
