package lain.mods.skins.impl.forge.asm;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("offlineskins")
@IFMLLoadingPlugin.MCVersion("")
@IFMLLoadingPlugin.TransformerExclusions("lain.mods.skins.impl.forge.asm.")
public class Plugin implements IFMLLoadingPlugin
{

    public static boolean runtimeDeobfuscationEnabled = false;
    public static boolean isDevelopmentEnvironment = false;

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] { "lain.mods.skins.impl.forge.asm.ASMTransformer" };
    }

    @Override
    public String getModContainerClass()
    {
        return null;
    }

    @Override
    public String getSetupClass()
    {
        return "lain.mods.skins.impl.forge.asm.Setup";
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
        runtimeDeobfuscationEnabled = (Boolean) data.get("runtimeDeobfuscationEnabled");
        isDevelopmentEnvironment = (getClass().getResource("/binpatches.pack.lzma") == null);
    }

}
