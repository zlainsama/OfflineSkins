package lain.mods.skins.init.neoforge;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;

public class Hooks {

    public static PlayerSkin getSkin(GameProfile profile, PlayerSkin result) {
        PlayerSkin skin = Proxy.INSTANCE.getSkin(profile);
        if (skin != null)
            return skin;
        return result;
    }

    public static PlayerSkin getSkin(PlayerInfo info, PlayerSkin result) {
        return getSkin(info.getProfile(), result);
    }

    public static ResourceLocation getSkinLocation(GameProfile profile, ResourceLocation result) {
        ResourceLocation location = Proxy.INSTANCE.getLocationSkin(profile);
        if (location != null)
            return location;
        return result;
    }

    public static ResourceLocation getSkinLocation(PlayerInfo info, ResourceLocation result) {
        return getSkinLocation(info.getProfile(), result);
    }

    public static ResourceLocation getSkinLocation(SkullBlock.Type type, GameProfile profile, ResourceLocation result) {
        if (type == SkullBlock.Types.PLAYER && profile != null)
            return getSkinLocation(profile, result);
        return result;
    }

    public static ResourceLocation getSkinLocation(SkullBlock.Type type, ResolvableProfile profile, ResourceLocation result) {
        if (type == SkullBlock.Types.PLAYER && profile != null && profile.gameProfile() != null)
            return getSkinLocation(profile.gameProfile(), result);
        return result;
    }

    public static ResourceLocation getCapeLocation(GameProfile profile, ResourceLocation result) {
        ResourceLocation location = Proxy.INSTANCE.getLocationCape(profile);
        if (location != null)
            return location;
        return result;
    }

    public static ResourceLocation getCapeLocation(PlayerInfo info, ResourceLocation result) {
        return getCapeLocation(info.getProfile(), result);
    }

    public static String getModelName(GameProfile profile, String result) {
        String name = Proxy.INSTANCE.getSkinType(profile);
        if (name != null)
            return name;
        return result;
    }

    public static String getModelName(PlayerInfo info, String result) {
        return getModelName(info.getProfile(), result);
    }

}
