package lain.mods.skins.api;

import net.minecraft.client.entity.AbstractClientPlayer;

public interface ISkinProvider
{

    /**
     * @param player
     * @return You can return null if no result.
     */
    ISkin getSkin(AbstractClientPlayer player);

}
