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
        Type callbackType = getCallbackType();
        org.objectweb.asm.commons.Method stronglyTypedMethod =
                asmMethod("void onMessage (" + callbackType.getClassName() + ")");

        stronglyTyped(stronglyTypedMethod, method.getParameterTypes());
        if (!callbackType.equals(objectType)) {
            synthetic(stronglyTypedMethod, callbackType);
        }
        meaningfulToString();
    }

    private Type getCallbackType() {
        if (method.getParameterTypes().length == 0) {
            return objectType;
        } else if (method.getParameterTypes().length == 1) {
            Type paramType = Type.getType(method.getParameterTypes()[0]);
            Type boxed = primitivesToBoxedTypes.get(paramType);
            return boxed != null ? boxed : paramType;
        } else {
            return Type.getType(Object[].class);
        }
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

    private void synthetic(org.objectweb.asm.commons.Method stronglyTypedMethod, Type callbackType) {
        GeneratorAdapter adapter = method(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC,
                asmMethod("void onMessage (" + Object.class.getName() + ")"));
        adapter.loadThis();
        adapter.loadArg(0);
        adapter.checkCast(callbackType);
        adapter.invokeVirtual(outputType(), stronglyTypedMethod);
        adapter.returnValue();
        adapter.endMethod();
    }

    private void stronglyTyped(org.objectweb.asm.commons.Method method, Class<?>[] paramTypes) {
        GeneratorAdapter adapter = method(ACC_PUBLIC, method);
        loadReceiver(adapter);
        loadArguments(adapter, paramTypes);
        adapter.invokeInterface(receiverType, asmMethod(this.method));
        adapter.returnValue();
        adapter.endMethod();
    }

    private void loadReceiver(GeneratorAdapter adapter) {
        adapter.loadThis();
        adapter.getField(outputType(), "receiver", receiverType);
    }

    private void loadArguments(GeneratorAdapter adapter, Class<?>[] paramTypes) {
        if (paramTypes.length == 1) {
            adapter.loadArg(0);
            Type type = Type.getType(paramTypes[0]);
            if (primitivesToBoxedTypes.containsKey(type)) {
                adapter.unbox(type);
            }
        } else if (paramTypes.length > 1) {
            for (int i = 0; i < paramTypes.length; i++) {
                adapter.loadArg(0);
                adapter.push(i);
                adapter.arrayLoad(objectType);
                Type type = Type.getType(paramTypes[i]);
                Type boxed = primitivesToBoxedTypes.get(type);
                if (boxed != null) {
                    adapter.checkCast(boxed);
                    adapter.unbox(type);
                } else {
                    adapter.checkCast(type);
                }
            }
        } // else paramTypes.length == 0: nothing to do
    }
}
