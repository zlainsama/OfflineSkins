package lain.mods.skins.impl.forge;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import lain.mods.skins.init.forge.Hooks;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class SkinUtils {

    private static final Function<GameProfile, ResourceLocation> SKIN = (profile) -> Hooks.getSkinLocation(profile, null);
    private static final Function<GameProfile, ResourceLocation> CAPE = (profile) -> Hooks.getCapeLocation(profile, null);
    private static final Function<GameProfile, PlayerSkin.Model> MODEL = (profile) -> PlayerSkin.Model.byName(Hooks.getModelName(profile, null));

    private static final LoadingCache<GameProfile, Supplier<PlayerSkin>> skinSuppliers = CacheBuilder
            .newBuilder()
            .expireAfterAccess(15, TimeUnit.SECONDS)
            .build(new CacheLoader<GameProfile, Supplier<PlayerSkin>>() {
                @Override
                public Supplier<PlayerSkin> load(GameProfile profile) throws Exception {
                    AtomicReference<PlayerSkin> HOLDER = new AtomicReference<>();
                    return () -> {
                        PlayerSkin skins = HOLDER.get();
                        ResourceLocation skinTexture = SKIN.apply(profile);
                        ResourceLocation capeTexture = CAPE.apply(profile);
                        PlayerSkin.Model model = MODEL.apply(profile);
                        if (skins == null) {
                            if (skinTexture != null) {
                                if (!HOLDER.compareAndSet(null, skins = new PlayerSkin(skinTexture, null, capeTexture, null, model, false)))
                                    skins = HOLDER.get();
                            }
                        } else if (skinTexture != null) {
                            if (skins.texture() != skinTexture || skins.capeTexture() != capeTexture || skins.model() != model) {
                                if (!HOLDER.compareAndSet(skins, skins = new PlayerSkin(skinTexture, null, capeTexture, null, model, false)))
                                    skins = HOLDER.get();
                            }
                        }
                        return skins;
                    };
                }
            });

    public static PlayerSkin skins(GameProfile profile) {
        return skinSuppliers.getUnchecked(profile).get();
    }

}
