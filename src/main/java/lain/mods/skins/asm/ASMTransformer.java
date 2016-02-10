package lain.mods.skins.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ASMTransformer implements IClassTransformer
{

    class transformer001 extends ClassVisitor
    {

        class method001 extends MethodVisitor
        {

            public method001(MethodVisitor mv)
            {
                super(Opcodes.ASM5, mv);
            }

            @Override
            public void visitInsn(int opcode)
            {
                if (opcode == Opcodes.ARETURN)
                {
                    this.visitVarInsn(Opcodes.ASTORE, 1);
                    this.visitVarInsn(Opcodes.ALOAD, 0);
                    this.visitVarInsn(Opcodes.ALOAD, 1);
                    this.visitMethodInsn(Opcodes.INVOKESTATIC, "lain/mods/skins/asm/Hooks", "getLocationCape", "(Lnet/minecraft/client/entity/AbstractClientPlayer;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;", false);
                }
                super.visitInsn(opcode);
            }

        }

        class method002 extends MethodVisitor
        {

            public method002(MethodVisitor mv)
            {
                super(Opcodes.ASM5, mv);
            }

            @Override
            public void visitInsn(int opcode)
            {
                if (opcode == Opcodes.ARETURN)
                {
                    this.visitVarInsn(Opcodes.ASTORE, 1);
                    this.visitVarInsn(Opcodes.ALOAD, 0);
                    this.visitVarInsn(Opcodes.ALOAD, 1);
                    this.visitMethodInsn(Opcodes.INVOKESTATIC, "lain/mods/skins/asm/Hooks", "getLocationSkin", "(Lnet/minecraft/client/entity/AbstractClientPlayer;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;", false);
                }
                super.visitInsn(opcode);
            }

        }

        class method003 extends MethodVisitor
        {

            public method003(MethodVisitor mv)
            {
                super(Opcodes.ASM5, mv);
            }

            @Override
            public void visitInsn(int opcode)
            {
                if (opcode == Opcodes.ARETURN)
                {
                    this.visitVarInsn(Opcodes.ASTORE, 1);
                    this.visitVarInsn(Opcodes.ALOAD, 0);
                    this.visitVarInsn(Opcodes.ALOAD, 1);
                    this.visitMethodInsn(Opcodes.INVOKESTATIC, "lain/mods/skins/asm/Hooks", "getSkinType", "(Lnet/minecraft/client/entity/AbstractClientPlayer;Ljava/lang/String;)Ljava/lang/String;", false);
                }
                super.visitInsn(opcode);
            }

        }

        ObfHelper m001 = ObfHelper.newMethod("func_110303_q", "net/minecraft/client/entity/AbstractClientPlayer", "()Lnet/minecraft/util/ResourceLocation;").setDevName("getLocationCape");
        ObfHelper m002 = ObfHelper.newMethod("func_110306_p", "net/minecraft/client/entity/AbstractClientPlayer", "()Lnet/minecraft/util/ResourceLocation;").setDevName("getLocationSkin");
        ObfHelper m003 = ObfHelper.newMethod("func_175154_l", "net/minecraft/client/entity/AbstractClientPlayer", "()Ljava/lang/String;").setDevName("getSkinType");

        public transformer001(ClassVisitor cv)
        {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
        {
            if (m001.match(name, desc))
                return new method001(super.visitMethod(access, name, desc, signature, exceptions));
            if (m002.match(name, desc))
                return new method002(super.visitMethod(access, name, desc, signature, exceptions));
            if (m003.match(name, desc))
                return new method003(super.visitMethod(access, name, desc, signature, exceptions));
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes)
    {
        if ("net.minecraft.client.entity.AbstractClientPlayer".equals(transformedName))
            return transform001(bytes);
        return bytes;
    }

    private byte[] transform001(byte[] bytes)
    {
        ClassReader classReader = new ClassReader(bytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classReader.accept(new transformer001(classWriter), ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }

}
