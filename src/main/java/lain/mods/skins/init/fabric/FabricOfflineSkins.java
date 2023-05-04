package lain.mods.skins.init.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import lain.mods.skins.api.SkinProviderAPI;
import lain.mods.skins.api.interfaces.ISkin;
import lain.mods.skins.impl.ConfigOptions;
import lain.mods.skins.impl.PlayerProfile;
import lain.mods.skins.impl.fabric.CustomSkinTexture;
import lain.mods.skins.impl.fabric.ImageUtils;
import lain.mods.skins.providers.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class FabricOfflineSkins implements ClientModInitializer {

    private static final Map<ByteBuffer, CustomSkinTexture> textures = new WeakHashMap<>();

    private static Identifier generateRandomLocation() {
        return new Identifier("offlineskins", String.format("textures/generated/%s", UUID.randomUUID()));
    }

    public static Identifier getLocationCape(GameProfile profile, Identifier result) {
        ISkin skin = SkinProviderAPI.CAPE.getSkin(PlayerProfile.wrapGameProfile(profile));
        if (skin != null && skin.isDataReady()) {
            ByteBuffer data = skin.getData();
            if (data != null) // I don't know how this could happen, but it happens, apparently.
                return getOrCreateTexture(data, skin).getLocation();
        }
        return null;
    }

    public static Identifier getLocationSkin(GameProfile profile, Identifier result) {
        ISkin skin = SkinProviderAPI.SKIN.getSkin(PlayerProfile.wrapGameProfile(profile));
        if (skin != null && skin.isDataReady()) {
            ByteBuffer data = skin.getData();
            if (data != null) // I don't know how this could happen, but it happens, apparently.
                return getOrCreateTexture(data, skin).getLocation();
        }
        return null;
    }

    private static CustomSkinTexture getOrCreateTexture(ByteBuffer data, ISkin skin) {
        if (!textures.containsKey(data)) {
            CustomSkinTexture texture = new CustomSkinTexture(generateRandomLocation(), data);
            MinecraftClient.getInstance().getTextureManager().registerTexture(texture.getLocation(), texture);
            textures.put(data, texture);

            if (skin != null) {
                skin.setRemovalListener(s -> {
                    if (data == s.getData()) {
                        MinecraftClient.getInstance().execute(() -> {
                            MinecraftClient.getInstance().getTextureManager().destroyTexture(texture.getLocation());
                            textures.remove(data);
                        });
                    }
                });
            }
        }
        return textures.get(data);
    }

    public static String getSkinType(GameProfile profile, String result) {
        Identifier location = getLocationSkin(profile, null);
        if (location != null) {
            ISkin skin = SkinProviderAPI.SKIN.getSkin(PlayerProfile.wrapGameProfile(profile));
            if (skin != null && skin.isDataReady()) {
                ByteBuffer data = skin.getData();
                if (data != null) // I don't know how this could happen, but it happens, apparently.
                    return skin.getSkinType();
            }
        }
        return null;
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.world != null) {
                for (PlayerEntity player : mc.world.getPlayers()) {
                    SkinProviderAPI.SKIN.getSkin(PlayerProfile.wrapGameProfile(player.getGameProfile()));
                    SkinProviderAPI.CAPE.getSkin(PlayerProfile.wrapGameProfile(player.getGameProfile()));
                }
            }
        });

        reloadConfig();
    }

    public void reloadConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path pathToConfig = Paths.get(".", "config", "offlineskins.json");
        pathToConfig.toFile().getParentFile().mkdirs();
        if (!pathToConfig.toFile().exists()) {
            try (Writer w = Files.newBufferedWriter(pathToConfig, StandardCharsets.UTF_8)) {
                gson.toJson(new ConfigOptions().defaultOptions(), w);
            } catch (Throwable t) {
                t.printStackTrace();
                System.err.println("[OfflineSkins] Failed to write default config file.");
            }
        }
        ConfigOptions config = null;
        try {
            config = gson.fromJson(Files.lines(pathToConfig, StandardCharsets.UTF_8).collect(Collectors.joining(System.getProperty("line.separator"))), ConfigOptions.class);
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("[OfflineSkins] Failed to read config file.");
            config = new ConfigOptions();
        }
        config.validate();

        SkinProviderAPI.SKIN.clearProviders();
        SkinProviderAPI.SKIN.registerProvider(new UserManagedSkinProvider(Paths.get(".", "cachedImages")).withFilter(ImageUtils::legacyFilter));
        if (config.useCustomServer)
            SkinProviderAPI.SKIN.registerProvider(new CustomServerSkinProvider().setHost(config.hostCustomServer).withFilter(ImageUtils::legacyFilter));
        if (config.useCustomServer2)
            SkinProviderAPI.SKIN.registerProvider(new CustomServerSkinProvider2().setHost(config.hostCustomServer2Skin).withFilter(ImageUtils::legacyFilter));
        if (config.useMojang)
            SkinProviderAPI.SKIN.registerProvider(new MojangSkinProvider().withFilter(ImageUtils::legacyFilter));
        if (config.useCrafatar)
            SkinProviderAPI.SKIN.registerProvider(new CrafatarSkinProvider().withFilter(ImageUtils::legacyFilter));

        SkinProviderAPI.CAPE.clearProviders();
        SkinProviderAPI.CAPE.registerProvider(new UserManagedCapeProvider(Paths.get(".", "cachedImages")));
        if (config.useCustomServer)
            SkinProviderAPI.CAPE.registerProvider(new CustomServerCapeProvider().setHost(config.hostCustomServer));
        if (config.useCustomServer2)
            SkinProviderAPI.CAPE.registerProvider(new CustomServerCapeProvider2().setHost(config.hostCustomServer2Cape));
        if (config.useMojang)
            SkinProviderAPI.CAPE.registerProvider(new MojangCapeProvider());
        if (config.useCrafatar)
            SkinProviderAPI.CAPE.registerProvider(new CrafatarCapeProvider());
    }

}
