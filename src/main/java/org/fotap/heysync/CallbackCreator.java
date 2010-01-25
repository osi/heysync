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
    private final boolean hasParameter;

    public CallbackCreator(Type outputType, Method method) {
        super(AsmHelper.<T>callback(), outputType);
        this.method = method;
        this.receiverType = Type.getType(method.getDeclaringClass());
        this.hasParameter = method.getParameterTypes().length > 0;
        this.parameterType = hasParameter ? Type.getType(method.getParameterTypes()[0]) : objectType;
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
        adapter.invokeConstructor(objectType, defaultConstructor);
        adapter.loadThis();
        adapter.loadArg(0);
        adapter.putField(outputType(), "receiver", receiverType);
        adapter.returnValue();
        adapter.endMethod();
    }

    @Override
    protected void implementMethods() {
        org.objectweb.asm.commons.Method stronglyTypedMethod =
            asmMethod("void onMessage (" + parameterType.getClassName() + ")");

        stronglyTyped(stronglyTypedMethod);
        if (hasParameter) {
            synthetic(stronglyTypedMethod);
        }
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
        if (hasParameter) {
            adapter.loadArg(0);
        }
        adapter.invokeInterface(receiverType, asmMethod(this.method));
        adapter.returnValue();
        adapter.endMethod();
    }
}
