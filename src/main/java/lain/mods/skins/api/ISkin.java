package lain.mods.skins.api;

import net.minecraft.util.ResourceLocation;

public interface ISkin
{

    ResourceLocation getSkinLocation();

    /**
     * @return "default" for classical 4-pixel arms; "slim" for 3-pixel slim arms.
     */
    String getSkinType();

    /**
     * @return You should only return true when (1. your texture object is already uploaded to the {@link net.minecraft.client.renderer.texture.TextureManager}) or (2. your texture can be loaded by {@link net.minecraft.client.renderer.texture.SimpleTexture} object).
     */
    boolean isSkinReady();

    /**
     * You should do your cleanup when this is called
     */
    void onRemoval();

}
