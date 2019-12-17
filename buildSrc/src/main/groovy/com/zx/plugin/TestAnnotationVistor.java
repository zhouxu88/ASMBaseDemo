package com.zx.plugin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author 周旭
 * @company 伊柯夫
 * @e-mail 374952705@qq.com
 * @time 2019/12/16
 * @descripe
 */


public class TestAnnotationVistor extends AnnotationVisitor {
    public TestAnnotationVistor( AnnotationVisitor av) {
        super(Opcodes.ASM6, av);
    }

    @Override
    public void visit(String name, Object value) {
        super.visit(name, value);
        System.out.println("--------AnnotationVisitor visit start-------");
        System.out.println("visit  visit----"+name);
        System.out.println("visit  value----"+value);
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
        super.visitEnum(name, desc, value);
        System.out.println("visitEnum  name----"+name);
        System.out.println("visitEnum  desc----"+desc);
        System.out.println("visitEnum  value----"+value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
        System.out.println("visitAnnotation  name----"+name);
        System.out.println("visitAnnotation  desc----"+desc);
        return super.visitAnnotation(name, desc);
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        System.out.println("visitArray  name----"+name);
        return super.visitArray(name);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        System.out.println("--------AnnotationVisitor visit end-------");
    }
}
