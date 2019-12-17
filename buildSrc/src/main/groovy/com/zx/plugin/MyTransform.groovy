package com.zx.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

class MyTransform extends Transform {

    @Override
    String getName() {
        return "MyTransform"
    }

    //输入文件类型，有CLASSES和RESOURCES
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    //    指Transform要操作内容的范围，官方文档Scope有7种类型：
//    EXTERNAL_LIBRARIES        只有外部库
//    PROJECT                       只有项目内容
//    PROJECT_LOCAL_DEPS            只有项目的本地依赖(本地jar)
//    PROVIDED_ONLY                 只提供本地或远程依赖项
//    SUB_PROJECTS              只有子项目。
//    SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
//    TESTED_CODE                   由当前变量(包括依赖项)测试的代码
//    SCOPE_FULL_PROJECT        整个项目
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    //指明当前Transform是否支持增量编译
    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        //inputs中是传过来的输入流，其中有两种格式，一种是jar包格式一种是目录格式。
        def inputs = transformInvocation.getInputs()
        //获取到输出目录，最后将修改的文件复制到输出目录，这一步必须做不然编译会报错
        def outputProvider = transformInvocation.getOutputProvider()

        for (TransformInput input : inputs) {
            for (JarInput jarInput : input.getJarInputs()) {
                handleJarInputs(jarInput, outputProvider)
            }

            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                handleDirectoryInput(directoryInput, outputProvider)
            }
        }
    }

    /**
     * 处理Jar中的class文件
     */
    void handleJarInputs(JarInput jarInput, TransformOutputProvider outputProvider) {
        File dest = outputProvider.getContentLocation(
                jarInput.getName(),
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                Format.JAR);
        //将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
        FileUtils.copyFile(jarInput.getFile(), dest);
    }

    /**
     * 处理文件目录下的class文件
     */
    void handleDirectoryInput(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {

        //列出目录所有文件（包含子文件夹，子文件夹内文件）
        directoryInput.file.eachFileRecurse { File file ->
            def fileName = file.name
            if (checkClassFile(fileName)) {
                System.out.println('filename----' + fileName)
                //对class文件进行读取与解析
                ClassReader classReader = new ClassReader(file.bytes)
                //对class文件的写入
                ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                //访问class文件相应的内容，解析到某一个结构就会通知到ClassVisitor的相应方法
                ClassVisitor classVisitor = new LifecycleClassVisitor(classWriter)
                //依次调用 ClassVisitor接口的各个方法
                classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
                //toByteArray方法会将最终修改的字节码以 byte 数组形式返回。
                byte[] bytes = classWriter.toByteArray()

                //通过文件流写入方式覆盖掉原先的内容，实现class文件的改写。
//                FileOutputStream outputStream = new FileOutputStream( file.parentFile.absolutePath + File.separator + fileName)
                //这个地址在javac目录下
                FileOutputStream outputStream = new FileOutputStream(file.path)
                outputStream.write(bytes)
                outputStream.close()
            }
        }

        //Transform 拷贝文件到transforms目录
        File dest = outputProvider.getContentLocation(
                directoryInput.getName(),
                directoryInput.getContentTypes(),
                directoryInput.getScopes(),
                Format.DIRECTORY);
        //将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
        FileUtils.copyDirectory(directoryInput.getFile(), dest)
    }

    void copyFile() {

    }

    /**
     * 检查class文件是否符合条件
     * @param name
     * @return
     */
    boolean checkClassFile(String name) {
        return name.endsWith("Activity.class")
    }
}