package org.fotap.heysync;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

public class Dumper {
    public static void main(final String[] args) throws Exception {
        ClassReader cr;
        cr = new ClassReader("org.fotap.heysync.ABImpl");
        Printer printer = new ASMifier();
//        Printer printer = new Textifier();
        cr.accept(new TraceClassVisitor(null, printer, new PrintWriter(System.out)), ClassReader.SKIP_DEBUG);
    }

}
