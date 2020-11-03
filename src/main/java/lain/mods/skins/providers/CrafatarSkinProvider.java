package lain.mods.skins.providers;

import lain.lib.SharedPool;
import lain.mods.skins.api.interfaces.IPlayerProfile;
import lain.mods.skins.api.interfaces.ISkin;
import lain.mods.skins.api.interfaces.ISkinProvider;
import lain.mods.skins.impl.Shared;
import lain.mods.skins.impl.SkinData;
import lain.mods.skins.impl.fabric.ImageUtils;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Function;

public class CrafatarSkinProvider implements ISkinProvider {

    private Function<ByteBuffer, ByteBuffer> _filter;

    @Override
    public ISkin getSkin(IPlayerProfile profile) {
        SkinData skin = new SkinData();
        if (_filter != null)
            skin.setSkinFilter(_filter);
        SharedPool.execute(() -> {
            if (!Shared.isOfflinePlayer(profile.getPlayerID(), profile.getPlayerName())) {
                Shared.downloadSkin(String.format("https://crafatar.com/skins/%s", profile.getPlayerID()), Runnable::run).thenApply(Optional::get).thenAccept(data -> {
                    if (ImageUtils.validateData(data))
                        skin.put(data, ImageUtils.judgeSkinType(data));
                });
            }
        });
        return skin;
    }

    public CrafatarSkinProvider withFilter(Function<ByteBuffer, ByteBuffer> filter) {
        _filter = filter;
        return this;
    }

}
