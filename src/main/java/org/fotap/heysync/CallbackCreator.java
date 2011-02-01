package org.fotap.heysync;

import org.jetlang.core.Callback;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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

    private static final Map<Type, Type> primitivesToBoxedTypes = new HashMap<Type, Type>();

    static {
        primitivesToBoxedTypes.put(Type.BOOLEAN_TYPE, Type.getType(Boolean.class));
        primitivesToBoxedTypes.put(Type.BYTE_TYPE, Type.getType(Byte.class));
        primitivesToBoxedTypes.put(Type.CHAR_TYPE, Type.getType(Character.class));
        primitivesToBoxedTypes.put(Type.DOUBLE_TYPE, Type.getType(Double.class));
        primitivesToBoxedTypes.put(Type.FLOAT_TYPE, Type.getType(Float.class));
        primitivesToBoxedTypes.put(Type.INT_TYPE, Type.getType(Integer.class));
        primitivesToBoxedTypes.put(Type.LONG_TYPE, Type.getType(Long.class));
        primitivesToBoxedTypes.put(Type.SHORT_TYPE, Type.getType(Short.class));
    }

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
        Type boxed = primitivesToBoxedTypes.get(parameterType);
        org.objectweb.asm.commons.Method stronglyTypedMethod =
                asmMethod("void onMessage (" + (boxed != null ? boxed : parameterType).getClassName() + ")");

        stronglyTyped(stronglyTypedMethod);
        if (hasParameter) {
            synthetic(stronglyTypedMethod);
        }
        meaningfulToString();
    }

    private void meaningfulToString() {
        GeneratorAdapter adapter = method(ACC_PUBLIC, toString);
        adapter.newInstance(stringBuilder);
        adapter.dup();
        adapter.invokeConstructor(stringBuilder, stringBuilderConstructor);
        adapter.push("[" + methodName() + " on ");
        adapter.invokeVirtual(stringBuilder, appendString);
        loadReceiver(adapter);
        adapter.invokeVirtual(stringBuilder, appendObject);
        adapter.push("]");
        adapter.invokeVirtual(stringBuilder, appendString);
        adapter.invokeVirtual(objectType, toString);
        adapter.returnValue();
        adapter.endMethod();
    }

    private String methodName() {
        String[] strings = method.toGenericString().split(" ");
        return strings[strings.length - 1];
    }

    private void synthetic(org.objectweb.asm.commons.Method stronglyTypedMethod) {
        Type boxed = primitivesToBoxedTypes.get(parameterType);
        GeneratorAdapter adapter = method(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC,
                asmMethod("void onMessage (" + Object.class.getName() + ")"));
        adapter.loadThis();
        adapter.loadArg(0);
        if (boxed != null) {
            adapter.unbox(boxed);
            adapter.checkCast(boxed);
        } else {
            adapter.checkCast(parameterType);
        }
        adapter.invokeVirtual(outputType(), stronglyTypedMethod);
        adapter.returnValue();
        adapter.endMethod();
    }

    private void stronglyTyped(org.objectweb.asm.commons.Method method) {
        GeneratorAdapter adapter = method(ACC_PUBLIC, method);
        loadReceiver(adapter);
        if (hasParameter) {
            adapter.loadArg(0);
            if (primitivesToBoxedTypes.containsKey(parameterType)) {
                adapter.unbox(parameterType);
            }
        }
        adapter.invokeInterface(receiverType, asmMethod(this.method));
        adapter.returnValue();
        adapter.endMethod();
    }

    private void loadReceiver(GeneratorAdapter adapter) {
        adapter.loadThis();
        adapter.getField(outputType(), "receiver", receiverType);
    }
}
