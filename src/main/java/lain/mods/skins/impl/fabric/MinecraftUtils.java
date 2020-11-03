package lain.mods.skins.impl.fabric;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.MinecraftClient;

import java.net.Proxy;

public class MinecraftUtils {

    public static Proxy getProxy() {
        return MinecraftClient.getInstance().getNetworkProxy();
    }

    public static MinecraftSessionService getSessionService() {
        return MinecraftClient.getInstance().getSessionService();
    }

}
