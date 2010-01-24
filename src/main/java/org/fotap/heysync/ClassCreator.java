package org.fotap.heysync;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import static org.fotap.heysync.AsmHelper.objectType;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
abstract class ClassCreator<T> {
    private final Class<T> type;
    private final Type outputType;
    protected ClassWriter writer;

    protected ClassCreator(Class<T> type, Type outputType) {
        this.type = type;
        this.outputType = outputType;
    }

    Class<T> type() {
        return type;
    }

    Type outputType() {
        return outputType;
    }

    byte[] bytes() {
        return createClass();
    }

    private byte[] createClass() {
        writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(V1_6,
                     ACC_PUBLIC + ACC_SUPER,
                     outputType.getInternalName(),
                     null,
                     objectType.getInternalName(),
                     new String[]{Type.getInternalName(type)});

        createFields();
        createConstructor();
        implementMethods();

        writer.visitEnd();

        return writer.toByteArray();
    }

    protected abstract void createFields();

    protected abstract void createConstructor();

    protected abstract void implementMethods();
}
