package lain.mods.skins.init.fabric.mixins;

import java.util.Collection;
import java.util.Iterator;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.InsnList;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;

public class IStoreInsnPoint extends InjectionPoint
{

    private final int ordinal;

    public IStoreInsnPoint(InjectionPointData data)
    {
        this.ordinal = data.getOrdinal();
    }

    @Override
    public boolean find(String desc, InsnList insns, Collection<AbstractInsnNode> nodes)
    {
        boolean any = false;
        int ordinal = 0;
        Iterator<AbstractInsnNode> iter = insns.iterator();
        while (iter.hasNext())
        {
            AbstractInsnNode insn = iter.next();
            if (insn.getOpcode() == Opcodes.ISTORE)
            {
                if (this.ordinal == -1 || this.ordinal == ordinal)
                {
                    nodes.add(insn);
                    any = true;
                }
                ordinal++;
            }
        }
        return any;
    }

}
