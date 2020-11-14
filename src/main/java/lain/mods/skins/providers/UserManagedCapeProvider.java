package lain.mods.skins.providers;

import lain.lib.SharedPool;
import lain.mods.skins.api.interfaces.IPlayerProfile;
import lain.mods.skins.api.interfaces.ISkin;
import lain.mods.skins.api.interfaces.ISkinProvider;
import lain.mods.skins.impl.Shared;
import lain.mods.skins.impl.SkinData;
import lain.mods.skins.impl.fabric.ImageUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.function.Function;

public class UserManagedCapeProvider implements ISkinProvider {

    private final File _dirN;
    private final File _dirU;
    private Function<ByteBuffer, ByteBuffer> _filter;

    public UserManagedCapeProvider(Path workDir) {
        _dirN = new File(workDir.toFile(), "capes");
        _dirN.mkdirs();
        _dirU = new File(_dirN, "uuid");
        _dirU.mkdirs();
    }

    @Override
    public ISkin getSkin(IPlayerProfile profile) {
        SkinData skin = new SkinData();
        if (_filter != null)
            skin.setSkinFilter(_filter);
        SharedPool.execute(() -> {
            byte[] data = null;
            if (!Shared.isOfflinePlayer(profile.getPlayerID(), profile.getPlayerName()))
                data = readFile(_dirU, "%s.png", profile.getPlayerID().toString().replaceAll("-", ""));
            if (data == null && !Shared.isBlank(profile.getPlayerName()))
                data = readFile(_dirN, "%s.png", profile.getPlayerName());
            if (data != null)
                skin.put(data, "cape");
        });
        return skin;
    }

    private byte[] readFile(File dir, String filename) {
        byte[] contents;
        if ((contents = Shared.blockyReadFile(new File(dir, filename), null, null)) != null && ImageUtils.validateData(contents))
            return contents;
        return null;
    }

    private byte[] readFile(File dir, String filename, Object... args) {
        return readFile(dir, String.format(filename, args));
    }

    public UserManagedCapeProvider withFilter(Function<ByteBuffer, ByteBuffer> filter) {
        _filter = filter;
        return this;
    }

}
