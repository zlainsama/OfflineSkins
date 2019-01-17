package lain.mods.skins.providers;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lain.mods.skins.api.interfaces.IPlayerProfile;
import lain.mods.skins.api.interfaces.ISkin;
import lain.mods.skins.api.interfaces.ISkinProvider;
import lain.mods.skins.impl.Shared;
import lain.mods.skins.impl.SkinData;
import lain.mods.skins.impl.fabric.MinecraftUtils;

public class CustomServerCachedCapeProvider implements ISkinProvider
{

    private File _dirN;
    private File _dirU;
    private Function<ByteBuffer, ByteBuffer> _filter;
    private String _host;
    private Map<String, String> _store;

    public CustomServerCachedCapeProvider(Path workDir, String host)
    {
        _dirN = new File(workDir.toFile(), "capes");
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
        Shared.execute(() -> {
            byte[] data = null;
            UUID uuid = profile.getPlayerID();
            String name = profile.getPlayerName();
            if (!Shared.isOfflinePlayerProfile(profile))
                data = CachedReader.create().setLocal(_dirU, uuid.toString()).setRemote("%s/capes/%s", _host, uuid).setDataStore(_store).setProxy(MinecraftUtils.getProxy()).read();
            if (data == null && !Shared.isBlank(name))
                data = CachedReader.create().setLocal(_dirN, name).setRemote("%s/capes/%s", _host, name).setDataStore(_store).setProxy(MinecraftUtils.getProxy()).read();
            if (data != null)
                skin.put(data, "cape");
        });
        return skin;
    }

    public CustomServerCachedCapeProvider withFilter(Function<ByteBuffer, ByteBuffer> filter)
    {
        _filter = filter;
        return this;
    }

}
