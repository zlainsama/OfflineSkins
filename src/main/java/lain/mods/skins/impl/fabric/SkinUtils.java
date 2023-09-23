package lain.mods.skins.impl.fabric;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import lain.mods.skins.init.fabric.FabricOfflineSkins;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class SkinUtils {

    private static final Function<GameProfile, Identifier> SKIN = (profile) -> FabricOfflineSkins.getLocationSkin(profile, null);
    private static final Function<GameProfile, Identifier> CAPE = (profile) -> FabricOfflineSkins.getLocationCape(profile, null);
    private static final Function<GameProfile, SkinTextures.Model> MODEL = (profile) -> SkinTextures.Model.fromName(FabricOfflineSkins.getSkinType(profile, null));

    private static final LoadingCache<GameProfile, Supplier<SkinTextures>> textureSuppliers = CacheBuilder
            .newBuilder()
            .expireAfterAccess(15, TimeUnit.SECONDS)
            .build(new CacheLoader<GameProfile, Supplier<SkinTextures>>() {
                @Override
                public Supplier<SkinTextures> load(GameProfile profile) throws Exception {
                    AtomicReference<SkinTextures> HOLDER = new AtomicReference<>();
                    return () -> {
                        SkinTextures textures = HOLDER.get();
                        Identifier skinTexture = SKIN.apply(profile);
                        Identifier capeTexture = CAPE.apply(profile);
                        SkinTextures.Model model = MODEL.apply(profile);
                        if (textures == null) {
                            if (skinTexture != null) {
                                if (!HOLDER.compareAndSet(null, textures = new SkinTextures(skinTexture, null, capeTexture, null, model, false)))
                                    textures = HOLDER.get();
                            }
                        } else if (skinTexture != null) {
                            if (textures.texture() != skinTexture || textures.capeTexture() != capeTexture || textures.model() != model) {
                                if (!HOLDER.compareAndSet(textures, textures = new SkinTextures(skinTexture, null, capeTexture, null, model, false)))
                                    textures = HOLDER.get();
                            }
                        }
                        return textures;
                    };
                }
            });

    public static SkinTextures textures(GameProfile profile) {
        return textureSuppliers.getUnchecked(profile).get();
    }

}
