package lain.mods.skins.init.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("offlineskins")
public class ForgeOfflineSkins {

    public ForgeOfflineSkins(FMLJavaModLoadingContext context) {
        context.getModEventBus().addListener(this::setupClient);
    }

    private void setupClient(FMLClientSetupEvent event) {
        Proxy.INSTANCE.init();
    }

}
