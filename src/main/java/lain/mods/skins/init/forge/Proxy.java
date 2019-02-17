package lain.mods.skins.init.forge;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import lain.mods.skins.api.SkinProviderAPI;
import lain.mods.skins.api.interfaces.ISkin;
import lain.mods.skins.impl.ConfigOptions;
import lain.mods.skins.impl.LegacyConversion;
import lain.mods.skins.impl.PlayerProfile;
import lain.mods.skins.impl.forge.CustomSkinTexture;
import lain.mods.skins.providers.CrafatarCachedCapeProvider;
import lain.mods.skins.providers.CrafatarCachedSkinProvider;
import lain.mods.skins.providers.CustomServerCachedCapeProvider;
import lain.mods.skins.providers.CustomServerCachedSkinProvider;
import lain.mods.skins.providers.MojangCachedCapeProvider;
import lain.mods.skins.providers.MojangCachedSkinProvider;
import lain.mods.skins.providers.UserManagedCapeProvider;
import lain.mods.skins.providers.UserManagedSkinProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.gameevent.TickEvent;

enum Proxy
{

    INSTANCE;

    Map<ByteBuffer, CustomSkinTexture> textures = new WeakHashMap<>();

    ResourceLocation generateRandomLocation()
    {
        return new ResourceLocation("offlineskins", String.format("textures/generated/%s", UUID.randomUUID().toString()));
    }

    ResourceLocation getLocationCape(GameProfile profile)
    {
        ISkin skin = SkinProviderAPI.CAPE.getSkin(PlayerProfile.wrapGameProfile(profile));
        if (skin != null && skin.isDataReady())
            return getOrCreateTexture(skin.getData(), skin).getLocation();
        return null;
    }

    ResourceLocation getLocationSkin(GameProfile profile)
    {
        ISkin skin = SkinProviderAPI.SKIN.getSkin(PlayerProfile.wrapGameProfile(profile));
        if (skin != null && skin.isDataReady())
            return getOrCreateTexture(skin.getData(), skin).getLocation();
        return null;
    }

    CustomSkinTexture getOrCreateTexture(ByteBuffer data, ISkin skin)
    {
        if (!textures.containsKey(data))
        {
            CustomSkinTexture texture = new CustomSkinTexture(generateRandomLocation(), data);
            Minecraft.getInstance().getTextureManager().loadTexture(texture.getLocation(), texture);
            textures.put(data, texture);

            if (skin != null)
            {
                skin.setRemovalListener(s -> {
                    if (data == s.getData())
                    {
                        Minecraft.getInstance().addScheduledTask(() -> {
                            Minecraft.getInstance().getTextureManager().deleteTexture(texture.getLocation());
                            textures.remove(data);
                        });
                    }
                });
            }
        }
        return textures.get(data);
    }

    String getSkinType(GameProfile profile)
    {
        ResourceLocation location = getLocationSkin(profile);
        if (location != null)
        {
            ISkin skin = SkinProviderAPI.SKIN.getSkin(PlayerProfile.wrapGameProfile(profile));
            if (skin != null && skin.isDataReady())
                return skin.getSkinType();
        }
        return null;
    }

    void handleClientTickEvent(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            World world = Minecraft.getInstance().world;
            if (world != null)
            {
                for (EntityPlayer player : world.playerEntities)
                {
                    SkinProviderAPI.SKIN.getSkin(PlayerProfile.wrapGameProfile(player.getGameProfile()));
                    SkinProviderAPI.CAPE.getSkin(PlayerProfile.wrapGameProfile(player.getGameProfile()));
                }
            }
        }
    }

    void init()
    {
        Logger logger = LogManager.getLogger(ForgeOfflineSkins.class);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path pathToConfig = Paths.get(".", "config", "offlineskins.json");
        pathToConfig.toFile().getParentFile().mkdirs();
        if (!pathToConfig.toFile().exists())
        {
            try (Writer w = Files.newBufferedWriter(pathToConfig, StandardCharsets.UTF_8))
            {
                gson.toJson(new ConfigOptions().defaultOptions(), w);
            }
            catch (Throwable t)
            {
                logger.error("[OfflineSkins] Failed to write default config file.", t);
            }
        }
        ConfigOptions config = null;
        try
        {
            config = gson.fromJson(Files.lines(pathToConfig, StandardCharsets.UTF_8).collect(Collectors.joining(System.getProperty("line.separator"))), ConfigOptions.class);
        }
        catch (Throwable t)
        {
            logger.error("[OfflineSkins] Failed to read config file.", t);
            config = new ConfigOptions();
        }
        config.validate();

        SkinProviderAPI.SKIN.clearProviders();
        SkinProviderAPI.SKIN.registerProvider(new UserManagedSkinProvider(Paths.get(".", "cachedImages")).withFilter(LegacyConversion.createFilter()));
        if (config.useCustomServer)
            SkinProviderAPI.SKIN.registerProvider(new CustomServerCachedSkinProvider(Paths.get(".", "cachedImages", "custom"), config.hostCustomServer).withFilter(LegacyConversion.createFilter()));
        if (config.useMojang)
            SkinProviderAPI.SKIN.registerProvider(new MojangCachedSkinProvider(Paths.get(".", "cachedImages", "mojang")).withFilter(LegacyConversion.createFilter()));
        if (config.useCrafatar)
            SkinProviderAPI.SKIN.registerProvider(new CrafatarCachedSkinProvider(Paths.get(".", "cachedImages", "crafatar")).withFilter(LegacyConversion.createFilter()));

        SkinProviderAPI.CAPE.clearProviders();
        SkinProviderAPI.CAPE.registerProvider(new UserManagedCapeProvider(Paths.get(".", "cachedImages")));
        if (config.useCustomServer)
            SkinProviderAPI.CAPE.registerProvider(new CustomServerCachedCapeProvider(Paths.get(".", "cachedImages", "custom"), config.hostCustomServer));
        if (config.useMojang)
            SkinProviderAPI.CAPE.registerProvider(new MojangCachedCapeProvider(Paths.get(".", "cachedImages", "mojang")));
        if (config.useCrafatar)
            SkinProviderAPI.CAPE.registerProvider(new CrafatarCachedCapeProvider(Paths.get(".", "cachedImages", "crafatar")));

        MinecraftForge.EVENT_BUS.addListener(this::handleClientTickEvent);
    }

}
