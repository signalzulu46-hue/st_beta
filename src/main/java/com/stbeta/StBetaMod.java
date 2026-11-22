package com.stbeta;
import com.stbeta.init.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
@Mod("st_beta")
public class StBetaMod {
    public StBetaMod() {
        IEventBus b = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(b); ModCreativeTabs.register(b); ModSounds.register(b);
        MinecraftForge.EVENT_BUS.register(this);
    }
}
