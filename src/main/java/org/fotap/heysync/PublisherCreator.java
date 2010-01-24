package org.fotap.heysync;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.fotap.heysync.AsmHelper.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class PublisherCreator<T> extends ClassCreator<T> {
    private final List<Method> methods;

    PublisherCreator(Class<T> type, Collection<Method> methods) {
        super(type, Type.getType("L" + Type.getInternalName(type) + "$Medium;"));
        this.methods = new ArrayList<Method>(methods);
    }

    public List<Method> methods() {
        return methods;
    }

    @Override
    protected void createFields() {
        for (Method method : methods) {
            createField(writer, method);
        }
    }

    private void createField(ClassWriter writer, Method method) {
        writer.visitField(ACC_PRIVATE + ACC_FINAL,
                          fieldNameFor(method),
                          publisherType.getDescriptor(),
                          //Could put the generic signature... "Lorg/jetlang/channels/Publisher<Ljava/lang/String;>;",
                          null,
                          null)
            .visitEnd();
    }

    @Override
    protected void createConstructor() {
        GeneratorAdapter adapter = new GeneratorAdapter(ACC_PUBLIC,
                                                        asmConstructorMethod(methods.size()),
                                                        null,
                                                        null,
                                                        writer);
        adapter.loadThis();
        adapter.invokeConstructor(objectType, defaultConstructor);
        int arg = 0;

        for (Method method : methods) {
            adapter.loadThis();
            adapter.loadArg(arg++);
            adapter.putField(outputType(), fieldNameFor(method), publisherType);
        }

        adapter.returnValue();
        adapter.endMethod();
    }

    private org.objectweb.asm.commons.Method asmConstructorMethod(int parameters) {
        StringBuilder builder = new StringBuilder().append("void <init> (");

        for (int i = 0; i < parameters; i++) {
            builder.append(publisherType.getClassName()).append(",");
        }

        builder.deleteCharAt(builder.length() - 1).append(")");
        return org.objectweb.asm.commons.Method.getMethod(builder.toString());
    }

    @Override
    protected void implementMethods() {
        for (Method method : methods) {
            implement(method);
        }
    }

    private void implement(Method method) {
        GeneratorAdapter adapter = new GeneratorAdapter(ACC_PUBLIC, AsmHelper.asmMethod(method), null, null, writer);
        adapter.loadThis();
        adapter.getField(outputType(), fieldNameFor(method), publisherType);
        adapter.loadArg(0);
        adapter.invokeInterface(publisherType, publishMethod);
        adapter.returnValue();
        adapter.endMethod();
    }

    private String fieldNameFor(Method method) {
        // fails for multiple methods with the same name
        return method.getName() + "Publisher";
    }
}