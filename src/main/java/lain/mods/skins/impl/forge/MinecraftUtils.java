package lain.mods.skins.impl.forge;

import java.net.Proxy;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.Minecraft;

public class MinecraftUtils
{

    public static Proxy getProxy()
    {
        return Minecraft.getInstance().getProxy();
    }

    public static MinecraftSessionService getSessionService()
    {
        return Minecraft.getInstance().getSessionService();
    }

}
