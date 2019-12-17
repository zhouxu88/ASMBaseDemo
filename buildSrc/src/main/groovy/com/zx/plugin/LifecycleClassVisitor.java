package com.zx.plugin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author 周旭
 * @company 伊柯夫
 * @e-mail 374952705@qq.com
 * @time 2019/12/16
 * @descripe
 */


public class LifecycleClassVisitor extends ClassVisitor {
    private String className;

    public LifecycleClassVisitor(ClassVisitor cv) {
        /**
         * 参数1：ASM API版本，源码规定只能为4，5，6
         * 参数2：ClassVisitor不能为 null
         */
        super(Opcodes.ASM6, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        System.out.println("------ClassVisitor visit start-------");
        System.out.println(" visit className-------" + name);
        System.out.println(" visit superName-------" + superName);
        this.className = name;
    }



    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        System.out.println("ClassVisitor visitMethod name-------" + name);
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

        if (name.startsWith("on")) {
            //处理onXX()方法
            return new LifecycleMethodVisitor(mv, className, name);
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        System.out.println("------ClassVisitor visit end-------");
    }


}

