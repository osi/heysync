package org.fotap.heysync;

import org.jetlang.channels.Publisher;
import org.jetlang.core.Callback;
import org.objectweb.asm.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class Creators extends ClassLoader {

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
            @SuppressWarnings({"unchecked"}) Class<Callback<R>> typedCallback = (Class<Callback<R>>) callbackClass;
            return typedCallback;
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

    private void foo() {
        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(V1_6,
                 ACC_PUBLIC + ACC_SUPER,
                 "org/fotap/heysync/Mouse$Medium",
                 null,
                 "java/lang/Object",
                 new String[]{"org/fotap/heysync/Mouse"});


        {
            mv = cw.visitMethod(ACC_PUBLIC,
                                "<init>",
                                "(Lorg/jetlang/channels/Publisher;)V",
                                "(Lorg/jetlang/channels/Publisher<Ljava/lang/String;>;)V",
                                null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD,
                              "org/fotap/heysync/Mouse$Medium",
                              "sayCheesePublisher",
                              "Lorg/jetlang/channels/Publisher;");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
            mv.visitLdcInsn("Dispatcher@");
            mv.visitMethodInsn(INVOKEVIRTUAL,
                               "java/lang/StringBuilder",
                               "append",
                               "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
            mv.visitLdcInsn(" for ");
            mv.visitMethodInsn(INVOKEVIRTUAL,
                               "java/lang/StringBuilder",
                               "append",
                               "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitLdcInsn(Type.getType("Lorg/fotap/heysync/Mouse;"));
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;");
            mv.visitMethodInsn(INVOKEVIRTUAL,
                               "java/lang/StringBuilder",
                               "append",
                               "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        cw.visitEnd();

//        return cw.toByteArray();
    }
}
