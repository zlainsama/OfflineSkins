package lain.mods.skins.init.forge;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SkullBlock;

public class Hooks {

    public static ResourceLocation getLocationCape(AbstractClientPlayer player, ResourceLocation result) {
        ResourceLocation loc = Proxy.INSTANCE.getLocationCape(player.getGameProfile());
        if (loc != null)
            return loc;
        return result;
    }

    public static ResourceLocation getLocationSkin(AbstractClientPlayer player, ResourceLocation result) {
        ResourceLocation loc = Proxy.INSTANCE.getLocationSkin(player.getGameProfile());
        if (loc != null)
            return loc;
        return result;
    }

    public static ResourceLocation getLocationSkin_SkullRenderer(SkullBlock.Type type, GameProfile profile, ResourceLocation result) {
        if (SkullBlock.Types.PLAYER == type && profile != null) {
            ResourceLocation loc = Proxy.INSTANCE.getLocationSkin(profile);
            if (loc != null)
                return loc;
        }
        return result;
    }

    public static ResourceLocation getLocationSkin_TabOverlay(GameProfile profile, ResourceLocation result) {
        ResourceLocation loc = Proxy.INSTANCE.getLocationSkin(profile);
        if (loc != null)
            return loc;
        return result;
    }

    public static String getSkinType(AbstractClientPlayer player, String result) {
        String type = Proxy.INSTANCE.getSkinType(player.getGameProfile());
        if (type != null)
            return type;
        return result;
    }

}
