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
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CustomServerSkinProvider implements ISkinProvider {

    private Function<ByteBuffer, ByteBuffer> _filter;
    private String _host;

    @Override
    public ISkin getSkin(IPlayerProfile profile) {
        SkinData skin = new SkinData();
        if (_filter != null)
            skin.setSkinFilter(_filter);
        SharedPool.execute(() -> {
            if (Shared.isOfflinePlayer(profile.getPlayerID(), profile.getPlayerName())) {
                Shared.downloadSkin(String.format("%s/skins/%s", _host, profile.getPlayerName()), Runnable::run).thenApply(Optional::get).thenAccept(data -> {
                    if (ImageUtils.validateData(data))
                        skin.put(data, ImageUtils.judgeSkinType(data));
                });
            } else {
                Shared.downloadSkin(String.format("%s/skins/%s", _host, profile.getPlayerID()), Runnable::run).handle((r, t) -> {
                    if (r != null && r.isPresent())
                        return CompletableFuture.completedFuture(r);
                    return Shared.downloadSkin(String.format("%s/skins/%s", _host, profile.getPlayerName()), Runnable::run);
                }).thenCompose(Function.identity()).thenApply(Optional::get).thenAccept(data -> {
                    if (ImageUtils.validateData(data))
                        skin.put(data, ImageUtils.judgeSkinType(data));
                });
            }
        });
        return skin;
    }

    public CustomServerSkinProvider setHost(String host) {
        _host = host;
        return this;
    }

    public CustomServerSkinProvider withFilter(Function<ByteBuffer, ByteBuffer> filter) {
        _filter = filter;
        return this;
    }

}
