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
        PublisherCreator<T> publisherCreator = new PublisherCreator<T>(type, publishers.keySet());
        Class<? extends T> publisherClass = defineClass(publisherCreator);
        Object[] initargs = publisherArguments(publisherCreator, publishers);
        return newInstance(publisherClass, initargs);
    }

    private <T> Object[] publisherArguments(PublisherCreator<T> publisherCreator,
                                            Map<Method, ? extends Publisher<?>> publishers)
    {
        List<Publisher<?>> arguments = new ArrayList<Publisher<?>>();
        for (Method method : publisherCreator.methods()) {
            arguments.add(publishers.get(method));
        }
        return arguments.toArray();
    }

    <T, R> Callback<R> callbackFor(Method method, T receiver) {
        Class<? extends Callback<R>> type = loadCallbackClass(method);
        return newInstance(type, receiver);
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

    private <R> Class<? extends Callback<R>> loadCallbackClass(Method method) {
        Type type = callbackTypeFor(method);
        Class<?> callbackClass = findLoadedClass(type.getInternalName());
        if (null != callbackClass) {
            return Cast.as(callbackClass);
        }
        return defineClass(new CallbackCreator<R>(type, method));
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
