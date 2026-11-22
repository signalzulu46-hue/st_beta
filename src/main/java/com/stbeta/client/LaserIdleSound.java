package com.stbeta.client;
import com.stbeta.init.ModSounds;
import com.stbeta.item.LaserGunItem;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
public class LaserIdleSound extends AbstractTickableSoundInstance {
    private final Player p;
    public LaserIdleSound(Player p) {
        super(ModSounds.LAZER_EKLEME.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.p = p; this.looping = true; this.volume = 0.85F;
    }
    public void stopSound() { this.stop(); }
    @Override public void tick() {
        if (p == null || !p.isAlive() || p.isRemoved()) { stop(); return; }
        boolean h = p.getMainHandItem().getItem() instanceof LaserGunItem || p.getOffhandItem().getItem() instanceof LaserGunItem;
        boolean s = p.isUsingItem() && p.getUseItem().getItem() instanceof LaserGunItem;
        if (!h || s) { stop(); return; }
        x = p.getX(); y = p.getY(); z = p.getZ();
    }
}
