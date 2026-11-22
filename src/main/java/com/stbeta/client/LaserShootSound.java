package com.stbeta.client;

import com.stbeta.init.ModSounds;
import com.stbeta.item.LaserGunItem;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class LaserShootSound extends AbstractTickableSoundInstance {
    private final Player player;

    public LaserShootSound(Player player) {
        super(ModSounds.LAZER_ATES.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public void tick() {
        if (this.player == null || !this.player.isAlive() || this.player.isRemoved()) {
            this.stop();
            return;
        }
        boolean shooting = this.player.isUsingItem()
                && this.player.getUseItem().getItem() instanceof LaserGunItem;
        if (!shooting) {
            this.stop();
            return;
        }
        this.x = this.player.getX();
        this.y = this.player.getY();
        this.z = this.player.getZ();
    }
}
