package lain.mods.skins.asm;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("OfflineSkins")
@IFMLLoadingPlugin.MCVersion("")
@IFMLLoadingPlugin.TransformerExclusions("lain.mods.skins.asm.")
public class Plugin implements IFMLLoadingPlugin
{

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] { "lain.mods.skins.asm.ASMTransformer" };
    }

    @Override
    public String getModContainerClass()
    {
        return "lain.mods.skins.OfflineSkins";
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> arg0)
    {
    }

}
