package com.stbeta.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class LaserGunItem extends Item implements GeoItem {

    private static final double MAX_DISTANCE = 35.0;
    private static final float EXPLOSION_POWER = 2.0F;
    private static final int FIRE_INTERVAL_TICKS = 6;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation SHOOT_ANIM = RawAnimation.begin().thenLoop("shoot");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private LaserGunRenderer renderer;

    public LaserGunItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (LaserGunItem.this.renderer == null) {
                    LaserGunItem.this.renderer = new LaserGunRenderer();
                }
                return LaserGunItem.this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 2, state -> {
            Player player = Minecraft.getInstance().player;
            if (player != null && player.isShiftKeyDown() &&
                    (player.getMainHandItem().getItem() == this || player.getOffhandItem().getItem() == this)) {
                return state.setAndContinue(SHOOT_ANIM);
            }
            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }

        if (player.getMainHandItem() != stack || !player.isShiftKeyDown()) {
            return;
        }

        if (level.getGameTime() % FIRE_INTERVAL_TICKS != 0) {
            return;
        }

        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(MAX_DISTANCE));

        BlockHitResult hitResult = level.clip(new ClipContext(
                eyePos,
                endPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        Vec3 hitPos = hitResult.getType() != HitResult.Type.MISS
                ? hitResult.getLocation()
                : endPos;

        if (level instanceof ServerLevel serverLevel) {
            double distance = eyePos.distanceTo(hitPos);
            int steps = Math.min((int) (distance * 4), 120);
            for (int i = 1; i <= steps; i++) {
                double t = (double) i / steps;
                Vec3 p = eyePos.lerp(hitPos, t);
                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,
                        p.x, p.y, p.z,
                        1, 0.0, 0.0, 0.0, 0.0
                );
            }
        }

        if (hitResult.getType() != HitResult.Type.MISS) {
            level.explode(
                    player,
                    hitPos.x,
                    hitPos.y,
                    hitPos.z,
                    EXPLOSION_POWER,
                    Level.ExplosionInteraction.MOB
            );
        }
    }
}
