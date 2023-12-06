package lain.mods.skins.providers;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import lain.lib.SharedPool;
import lain.mods.skins.api.interfaces.IPlayerProfile;
import lain.mods.skins.api.interfaces.ISkin;
import lain.mods.skins.api.interfaces.ISkinProvider;
import lain.mods.skins.impl.Shared;
import lain.mods.skins.impl.SkinData;
import lain.mods.skins.impl.forge.ImageUtils;
import lain.mods.skins.impl.forge.MinecraftUtils;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Function;

public class MojangCapeProvider implements ISkinProvider {

    private Function<ByteBuffer, ByteBuffer> _filter;

    @Override
    public ISkin getSkin(IPlayerProfile profile) {
        SkinData skin = new SkinData();
        if (_filter != null)
            skin.setSkinFilter(_filter);
        SharedPool.execute(() -> {
            if (!Shared.isOfflinePlayer(profile.getPlayerID(), profile.getPlayerName())) {
                MinecraftProfileTexture texture = MinecraftUtils.getSessionService().getTextures((GameProfile) profile.getOriginal()).cape();
                if (texture != null) {
                    Shared.downloadSkin(texture.getUrl(), Runnable::run).thenApply(Optional::get).thenAccept(data -> {
                        if (ImageUtils.validateData(data))
                            skin.put(data, "cape");
                    });
                }
            }
        });
        return skin;
    }

    public MojangCapeProvider withFilter(Function<ByteBuffer, ByteBuffer> filter) {
        _filter = filter;
        return this;
    }

}
