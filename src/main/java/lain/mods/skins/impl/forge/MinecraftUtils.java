package lain.mods.skins.impl.forge;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.Minecraft;

import java.net.Proxy;

public class MinecraftUtils {

    public static Proxy getProxy() {
        return Minecraft.getInstance().getProxy();
    }

    public static MinecraftSessionService getSessionService() {
        return Minecraft.getInstance().getMinecraftSessionService();
    }

}
