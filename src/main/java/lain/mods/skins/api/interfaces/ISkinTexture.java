package lain.mods.skins.api.interfaces;

import java.nio.ByteBuffer;

public interface ISkinTexture {

    /**
     * @return the ByteBuffer for this ISkinTexture. may be null.
     */
    ByteBuffer getData();

}
