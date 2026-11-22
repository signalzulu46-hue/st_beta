package com.stbeta.init;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ModItems.MOD_ID);
    public static final RegistryObject<SoundEvent> LAZER_EKLEME = reg("lazer_ekleme");
    public static final RegistryObject<SoundEvent> LAZER_DOLDURMA = reg("lazer_doldurma");
    public static final RegistryObject<SoundEvent> LAZER_ATES = reg("lazer_ates");
    private static RegistryObject<SoundEvent> reg(String n) { return SOUND_EVENTS.register(n, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ModItems.MOD_ID, n))); }
    public static void register(IEventBus b) { SOUND_EVENTS.register(b); }
}
