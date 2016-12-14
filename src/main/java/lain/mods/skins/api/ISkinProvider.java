package lain.mods.skins.api;

import com.mojang.authlib.GameProfile;

public interface ISkinProvider
{

    /**
     * @param profile
     * @return You can return null if no result.
     */
    ISkin getSkin(GameProfile profile);

}
