package lain.mods.skins.providers;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Function;
import lain.mods.skins.api.interfaces.IPlayerProfile;
import lain.mods.skins.api.interfaces.ISkin;
import lain.mods.skins.api.interfaces.ISkinProvider;
import lain.mods.skins.impl.Shared;
import lain.mods.skins.impl.SkinData;
import lain.mods.skins.impl.forge.MinecraftUtils;

public class CustomServerCachedSkinProvider implements ISkinProvider
{

    private File _dirN;
    private File _dirU;
    private Function<ByteBuffer, ByteBuffer> _filter;
    private String _host;

    public CustomServerCachedSkinProvider(Path workDir, String host)
    {
        _dirN = new File(workDir.toFile(), "skins");
        _dirN.mkdirs();
        _dirU = new File(_dirN, "uuid");
        _dirU.mkdirs();
        _host = host;

        for (File file : _dirN.listFiles())
            if (file.isFile())
                file.delete();
        for (File file : _dirU.listFiles())
            if (file.isFile())
                file.delete();
    }

    @Override
    public ISkin getSkin(IPlayerProfile profile)
    {
        SkinData skin = new SkinData();
        if (_filter != null)
            skin.setSkinFilter(_filter);
        Shared.pool.execute(() -> {
            byte[] data = null;
            UUID uuid = profile.getPlayerID();
            String name = profile.getPlayerName();
            if (!Shared.isOfflinePlayer(profile.getPlayerID(), profile.getPlayerName()))
                data = CachedDownloader.create().setLocal(_dirU, uuid.toString()).setRemote("%s/skins/%s", _host, uuid).setDataStore(Shared.store).setProxy(MinecraftUtils.getProxy()).setValidator(SkinData::validateData).read();
            if (data == null && !Shared.isBlank(name))
                data = CachedDownloader.create().setLocal(_dirN, name).setRemote("%s/skins/%s", _host, name).setDataStore(Shared.store).setProxy(MinecraftUtils.getProxy()).setValidator(SkinData::validateData).read();
            if (data != null)
                skin.put(data, SkinData.judgeSkinType(data));
        });
        return skin;
    }

    public CustomServerCachedSkinProvider withFilter(Function<ByteBuffer, ByteBuffer> filter)
    {
        _filter = filter;
        return this;
    }

}
