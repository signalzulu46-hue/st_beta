package com.stbeta.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
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
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CROSSBOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide || !(livingEntity instanceof Player player)) {
            return;
        }

        if (level.getGameTime() % FIRE_INTERVAL_TICKS != 0) {
            return;
        }

        boolean hasMain = player.getMainHandItem().getItem() instanceof LaserGunItem;
        boolean hasOff = player.getOffhandItem().getItem() instanceof LaserGunItem;

        if (hasMain && hasOff) {
            fireBeam(level, player, 0.35);
            fireBeam(level, player, -0.35);
        } else if (hasOff) {
            fireBeam(level, player, -0.35);
        } else {
            fireBeam(level, player, 0.35);
        }
    }

    private void fireBeam(Level level, Player player, double sideOffset) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 rightVec = lookVec.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 startPos = eyePos.add(rightVec.scale(sideOffset)).add(0, -0.2, 0);
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
            double distance = startPos.distanceTo(hitPos);
            int steps = Math.min((int) (distance * 4), 120);
            for (int i = 1; i <= steps; i++) {
                double t = (double) i / steps;
                Vec3 p = startPos.lerp(hitPos, t);
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

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 2, state -> {
            Player player = Minecraft.getInstance().player;
            if (player != null && player.isUsingItem() &&
                    player.getUseItem().getItem() instanceof LaserGunItem) {
                return state.setAndContinue(SHOOT_ANIM);
            }
            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
