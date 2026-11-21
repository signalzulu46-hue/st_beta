package com.stbeta;

import com.stbeta.init.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import software.bernie.geckolib.GeckoLib;

@Mod(StBetaMod.MOD_ID)
public class StBetaMod {

    public static final String MOD_ID = "st_beta";

    public StBetaMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        GeckoLib.initialize();
        ModItems.register(modEventBus);
    }
}
