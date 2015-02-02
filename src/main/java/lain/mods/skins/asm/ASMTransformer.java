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
                    this.visitMethodInsn(Opcodes.INVOKESTATIC, "lain/mods/skins/OfflineSkins", "getLocationSkin", "(Lnet/minecraft/client/entity/AbstractClientPlayer;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;", false);
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
                    this.visitMethodInsn(Opcodes.INVOKESTATIC, "lain/mods/skins/OfflineSkins", "getLocationCape", "(Lnet/minecraft/client/entity/AbstractClientPlayer;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;", false);
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
                    this.visitMethodInsn(Opcodes.INVOKESTATIC, "lain/mods/skins/OfflineSkins", "getSkinType", "(Lnet/minecraft/client/entity/AbstractClientPlayer;Ljava/lang/String;)Ljava/lang/String;", false);
                }
                super.visitInsn(opcode);
            }

        }

        String mN001 = "i"; // getLocationSkin
        String mD001 = "()Loa;"; // ()Lnet/minecraft/util/ResourceLocation;
        String mN002 = "k"; // getLocationCape
        String mD002 = "()Loa;"; // ()Lnet/minecraft/util/ResourceLocation;
        String mN003 = "l"; // getSkinType
        String mD003 = "()Ljava/lang/String;"; // ()Ljava/lang/String;

        public transformer001(ClassVisitor cv)
        {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
        {
            if (mN001.equals(name) && mD001.equals(desc))
                return new method001(super.visitMethod(access, name, desc, signature, exceptions));
            if (mN002.equals(name) && mD002.equals(desc))
                return new method002(super.visitMethod(access, name, desc, signature, exceptions));
            if (mN003.equals(name) && mD003.equals(desc))
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
