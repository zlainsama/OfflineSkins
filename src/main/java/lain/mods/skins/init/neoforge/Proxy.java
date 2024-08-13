package lain.mods.skins.init.neoforge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import lain.mods.skins.api.SkinProviderAPI;
import lain.mods.skins.api.interfaces.ISkin;
import lain.mods.skins.impl.ConfigOptions;
import lain.mods.skins.impl.PlayerProfile;
import lain.mods.skins.impl.neoforge.CustomSkinTexture;
import lain.mods.skins.impl.neoforge.ImageUtils;
import lain.mods.skins.impl.neoforge.SkinUtils;
import lain.mods.skins.providers.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

enum Proxy {

    INSTANCE;

    Map<ByteBuffer, CustomSkinTexture> textures = new WeakHashMap<>();

    ResourceLocation generateRandomLocation() {
        return ResourceLocation.fromNamespaceAndPath("offlineskins", String.format("textures/generated/%s", UUID.randomUUID()));
    }

    ResourceLocation getLocationCape(GameProfile profile) {
        ISkin skin = SkinProviderAPI.CAPE.getSkin(PlayerProfile.wrapGameProfile(profile));
        if (skin != null && skin.isDataReady()) {
            ByteBuffer data = skin.getData();
            if (data != null) // I don't know how this could happen, but it happens, apparently.
                return getOrCreateTexture(data, skin).getLocation();
        }
        return null;
    }

    ResourceLocation getLocationSkin(GameProfile profile) {
        ISkin skin = SkinProviderAPI.SKIN.getSkin(PlayerProfile.wrapGameProfile(profile));
        if (skin != null && skin.isDataReady()) {
            ByteBuffer data = skin.getData();
            if (data != null) // I don't know how this could happen, but it happens, apparently.
                return getOrCreateTexture(data, skin).getLocation();
        }
        return null;
    }

    CustomSkinTexture getOrCreateTexture(ByteBuffer data, ISkin skin) {
        if (!textures.containsKey(data)) {
            CustomSkinTexture texture = new CustomSkinTexture(generateRandomLocation(), data);
            Minecraft.getInstance().getTextureManager().register(texture.getLocation(), texture);
            textures.put(data, texture);

            if (skin != null) {
                skin.setRemovalListener(s -> {
                    if (data == s.getData()) {
                        Minecraft.getInstance().execute(() -> {
                            Minecraft.getInstance().getTextureManager().release(texture.getLocation());
                            textures.remove(data);
                        });
                    }
                });
            }
        }
        return textures.get(data);
    }

    PlayerSkin getSkin(GameProfile profile) {
        return SkinUtils.skins(profile);
    }

    String getSkinType(GameProfile profile) {
        ResourceLocation location = getLocationSkin(profile);
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

    void handleClientTickEvent(ClientTickEvent.Pre event) {
        Level world = Minecraft.getInstance().level;
        if (world != null) {
            for (Player player : world.players()) {
                SkinProviderAPI.SKIN.getSkin(PlayerProfile.wrapGameProfile(player.getGameProfile()));
                SkinProviderAPI.CAPE.getSkin(PlayerProfile.wrapGameProfile(player.getGameProfile()));
            }
        }
    }

    void init() {
        Logger logger = LogManager.getLogger(NeoForgeOfflineSkins.class);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path pathToConfig = Paths.get(".", "config", "offlineskins.json");
        pathToConfig.toFile().getParentFile().mkdirs();
        if (!pathToConfig.toFile().exists()) {
            try (Writer w = Files.newBufferedWriter(pathToConfig, StandardCharsets.UTF_8)) {
                gson.toJson(new ConfigOptions().defaultOptions(), w);
            } catch (Throwable t) {
                logger.error("[OfflineSkins] Failed to write default config file.", t);
            }
        }
        ConfigOptions config = null;
        try {
            config = gson.fromJson(Files.lines(pathToConfig, StandardCharsets.UTF_8).collect(Collectors.joining(System.getProperty("line.separator"))), ConfigOptions.class);
        } catch (Throwable t) {
            logger.error("[OfflineSkins] Failed to read config file.", t);
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

        Hooks.PLAYERHEADS = !config.disablePlayerHeads;

        NeoForge.EVENT_BUS.addListener(this::handleClientTickEvent);
    }

}
