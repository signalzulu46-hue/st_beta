package com.stbeta.item;

import com.stbeta.client.LaserIdleSound;
import com.stbeta.client.LaserShootSound;
import com.stbeta.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
    private static final float EXPLOSION_POWER = 1.8F;
    private static final float DIRECT_DAMAGE = 6.0F;
    private static final int FIRE_INTERVAL_TICKS = 2;
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation SHOOT_ANIM = RawAnimation.begin().thenLoop("shoot");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private LaserGunRenderer renderer;

    @OnlyIn(Dist.CLIENT)
    private static LaserIdleSound activeIdleSound;
    @OnlyIn(Dist.CLIENT)
    private static LaserShootSound activeShootSound;

    public LaserGunItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (LaserGunItem.this.renderer == null) LaserGunItem.this.renderer = new LaserGunRenderer();
                return LaserGunItem.this.renderer;
            }
        });
    }

    @Override
    public int getUseDuration(ItemStack stack) { return 72000; }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.CROSSBOW; }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide && entity instanceof Player player) handleClientSounds(player);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClientSounds(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != player) return;
        boolean holding = player.getMainHandItem().getItem() instanceof LaserGunItem
                || player.getOffhandItem().getItem() instanceof LaserGunItem;
        boolean shooting = player.isUsingItem() && player.getUseItem().getItem() instanceof LaserGunItem;

        if (shooting) {
            if (activeShootSound == null || activeShootSound.isStopped() || !mc.getSoundManager().isActive(activeShootSound)) {
                activeShootSound = new LaserShootSound(player);
                mc.getSoundManager().play(activeShootSound);
            }
        } else if (activeShootSound != null && !activeShootSound.isStopped()) {
            activeShootSound.stop();
            activeShootSound = null;
        }

        if (holding && !shooting) {
            if (activeIdleSound == null || activeIdleSound.isStopped() || !mc.getSoundManager().isActive(activeIdleSound)) {
                activeIdleSound = new LaserIdleSound(player);
                mc.getSoundManager().play(activeIdleSound);
            }
        } else if (activeIdleSound != null && !activeIdleSound.isStopped()) {
            activeIdleSound.stop();
            activeIdleSound = null;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.LAZER_DOLDURMA.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.startUsingItem(hand);
        if (level.isClientSide) handleClientSounds(player);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide) {
            if (livingEntity instanceof Player player) handleClientSounds(player);
            return;
        }
        if (!(livingEntity instanceof Player player)) return;
        if (level.getGameTime() % FIRE_INTERVAL_TICKS != 0) return;

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

        BlockHitResult blockHit = level.clip(new ClipContext(
                eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        Vec3 hitPos = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation() : endPos;
        double maxHitDistSqr = eyePos.distanceToSqr(hitPos);

        AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(MAX_DISTANCE)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level, player, eyePos, hitPos, searchBox,
                e -> !e.isSpectator() && e.isPickable() && e != player, 0.35F);

        boolean didHit = blockHit.getType() != HitResult.Type.MISS;
        if (entityHit != null && eyePos.distanceToSqr(entityHit.getLocation()) (this, "controller", 2, state -> {
            Player player = Minecraft.getInstance().player;
            if (player != null && player.isUsingItem() &&
                    player.getUseItem().getItem() instanceof LaserGunItem) {
                return state.setAndContinue(SHOOT_ANIM);
            }
            return state.setAndContinue(IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
