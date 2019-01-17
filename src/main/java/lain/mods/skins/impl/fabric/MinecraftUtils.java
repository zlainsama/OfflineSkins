package lain.mods.skins.impl.fabric;

import java.net.Proxy;
import java.util.Map;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import lain.mods.skins.api.interfaces.IPlayerProfile;
import net.minecraft.client.MinecraftClient;

public class MinecraftUtils
{

    public static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getProfileTextures(IPlayerProfile profile)
    {
        try
        {
            return MinecraftClient.getInstance().getSkinProvider().method_4654((GameProfile) profile.getOriginal());
        }
        catch (Throwable t)
        {
            return null;
        }
    }

    public static Proxy getProxy()
    {
        return MinecraftClient.getInstance().getNetworkProxy();
    }

    public static MinecraftSessionService getSessionService()
    {
        return MinecraftClient.getInstance().getSessionService();
    }

}
