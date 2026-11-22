package com.stbeta;

import com.stbeta.init.ModCreativeTabs;
import com.stbeta.init.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("st_beta")
public class StBetaMod {
    public StBetaMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }
}
