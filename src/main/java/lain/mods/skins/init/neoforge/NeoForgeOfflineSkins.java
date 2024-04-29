package lain.mods.skins.init.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod("offlineskins")
public class NeoForgeOfflineSkins {

    public NeoForgeOfflineSkins(IEventBus bus) {
        bus.addListener(this::setupClient);
    }

    private void setupClient(FMLClientSetupEvent event) {
        Proxy.INSTANCE.init();
    }

}
