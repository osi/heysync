package org.fotap.heysync;

import org.jetlang.core.Callback;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.reflect.Method;

import static org.fotap.heysync.AsmHelper.*;
import static org.objectweb.asm.Opcodes.*;


/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class CallbackCreator<T> extends ClassCreator<Callback<T>> {
    private final Method method;
    private final Type receiverType;
    private final Type parameterType;

    public CallbackCreator(Type outputType, Method method) {
        super(AsmHelper.<T>callback(), outputType);
        this.method = method;
        this.receiverType = Type.getType(method.getDeclaringClass());
        this.parameterType = Type.getType(method.getParameterTypes()[0]);
    }

    @Override
    protected void createFields() {
        writer.visitField(ACC_PRIVATE + ACC_FINAL,
                          "receiver",
                          receiverType.getDescriptor(),
                          null,
                          null).visitEnd();
    }

    @Override
    protected void createConstructor() {
        String declaration = "void <init> (" + method.getDeclaringClass().getName() + ")";
        GeneratorAdapter adapter = method(ACC_PUBLIC, asmMethod(declaration));
        adapter.loadThis();
        adapter.invokeConstructor(objectType, asmObjectConstructor);
        adapter.loadThis();
        adapter.loadArg(0);
        adapter.putField(outputType(), "receiver", receiverType);
        adapter.returnValue();
        adapter.endMethod();
    }

    private GeneratorAdapter method(int access, org.objectweb.asm.commons.Method method) {
        return new GeneratorAdapter(access,
                                    method,
                                    null,
                                    null,
                                    writer);
    }

    @Override
    protected void implementMethods() {
        org.objectweb.asm.commons.Method stronglyTypedMethod =
            asmMethod("void onMessage (" + parameterType.getClassName() + ")");

        stronglyTyped(stronglyTypedMethod);
        synthetic(stronglyTypedMethod);
    }

    private void synthetic(org.objectweb.asm.commons.Method stronglyTypedMethod) {
        GeneratorAdapter adapter = method(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC,
                                          asmMethod("void onMessage (" + Object.class.getName() + ")"));
        adapter.loadThis();
        adapter.loadArg(0);
        adapter.checkCast(parameterType);
        adapter.invokeVirtual(outputType(), stronglyTypedMethod);
        adapter.returnValue();
        adapter.endMethod();
    }

    private void stronglyTyped(org.objectweb.asm.commons.Method method) {
        GeneratorAdapter adapter = method(ACC_PUBLIC, method);
        adapter.loadThis();
        adapter.getField(outputType(), "receiver", receiverType);
        adapter.loadArg(0);
        adapter.invokeInterface(receiverType, asmMethod(this.method));
        adapter.returnValue();
        adapter.endMethod();
    }
}
