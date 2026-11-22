package com.stbeta.item;
import com.stbeta.client.*;
import com.stbeta.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.*;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;

public class LaserGunItem extends Item implements GeoItem {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation SHOOT = RawAnimation.begin().thenLoop("shoot");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private LaserGunRenderer renderer;
    @OnlyIn(Dist.CLIENT) private static LaserIdleSound idleSnd;
    @OnlyIn(Dist.CLIENT) private static LaserShootSound shootSnd;

    public LaserGunItem(Properties p) { super(p); SingletonGeoAnimatable.registerSyncedAnimatable(this); }
    @Override public void initializeClient(Consumer<IClientItemExtensions> c) {
        c.accept(new IClientItemExtensions() {
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new LaserGunRenderer();
                return renderer;
            }
        });
    }
    @Override public int getUseDuration(ItemStack s) { return 72000; }
    @Override public UseAnim getUseAnimation(ItemStack s) { return UseAnim.CROSSBOW; }
    @Override public void inventoryTick(ItemStack s, Level l, Entity e, int slot, boolean sel) {
        if (l.isClientSide && e instanceof Player p) syncSounds(p);
    }
    @OnlyIn(Dist.CLIENT) private static void syncSounds(Player p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != p) return;
        boolean h = p.getMainHandItem().getItem() instanceof LaserGunItem || p.getOffhandItem().getItem() instanceof LaserGunItem;
        boolean s = p.isUsingItem() && p.getUseItem().getItem() instanceof LaserGunItem;
        if (s) { if (shootSnd == null || shootSnd.isStopped()) { shootSnd = new LaserShootSound(p); mc.getSoundManager().play(shootSnd); } }
        else if (shootSnd != null) { shootSnd.stopSound(); shootSnd = null; }
        if (h && !s) { if (idleSnd == null || idleSnd.isStopped()) { idleSnd = new LaserIdleSound(p); mc.getSoundManager().play(idleSnd); } }
        else if (idleSnd != null) { idleSnd.stopSound(); idleSnd = null; }
    }
    @Override public InteractionResultHolder<ItemStack> use(Level l, Player p, InteractionHand h) {
        if (!l.isClientSide) l.playSound(null, p.getX(), p.getY(), p.getZ(), ModSounds.LAZER_DOLDURMA.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        p.startUsingItem(h);
        if (l.isClientSide) syncSounds(p);
        return InteractionResultHolder.consume(p.getItemInHand(h));
    }
    @Override public void onUseTick(Level l, LivingEntity e, ItemStack s, int rem) {
        if (l.isClientSide) { if (e instanceof Player p) syncSounds(p); return; }
        if (!(e instanceof Player p) || l.getGameTime() % 2 != 0) return;
        boolean m = p.getMainHandItem().getItem() instanceof LaserGunItem, o = p.getOffhandItem().getItem() instanceof LaserGunItem;
        if (m && o) { fire(l, p, 0.35); fire(l, p, -0.35); } else fire(l, p, o ? -0.35 : 0.35);
    }
    private void fire(Level l, Player p, double off) {
        Vec3 eye = p.getEyePosition(1.0F), look = p.getLookAngle(), right = look.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 start = eye.add(right.scale(off)).add(0, -0.2, 0), end = eye.add(look.scale(35.0));
        BlockHitResult bh = l.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, p));
        Vec3 hit = bh.getType() != HitResult.Type.MISS ? bh.getLocation() : end;
        EntityHitResult eh = ProjectileUtil.getEntityHitResult(l, p, eye, hit, p.getBoundingBox().expandTowards(look.scale(35.0)).inflate(1.0), x -> !x.isSpectator() && x.isPickable() && x != p, 0.35F);
        boolean ok = bh.getType() != HitResult.Type.MISS;
        if (eh != null && eye.distanceToSqr(eh.getLocation()) < eye.distanceToSqr(hit)) {
            hit = eh.getLocation(); ok = true; Entity t = eh.getEntity();
            t.invulnerableTime = 0; t.setSecondsOnFire(8); t.hurt(l.damageSources().playerAttack(p), 6.0F);
        }
        if (l instanceof ServerLevel sl) {
            int st = Math.min((int)(start.distanceTo(hit) * 4), 120);
            for (int i = 1; i <= st; i++) { Vec3 pt = start.lerp(hit, (double)i/st); sl.sendParticles(ParticleTypes.FLAME, pt.x, pt.y, pt.z, 1, 0, 0, 0, 0); }
        }
        if (ok) {
            BlockPos fp = BlockPos.containing(hit).above();
            if (l.isEmptyBlock(fp) && BaseFireBlock.canBePlacedAt(l, fp, p.getDirection())) l.setBlockAndUpdate(fp, BaseFireBlock.getState(l, fp));
            l.explode(p, null, null, hit.x, hit.y, hit.z, 1.8F, true, Level.ExplosionInteraction.MOB);
        }
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar c) {
        c.add(new AnimationController<>(this, "controller", 2, s -> {
            Player p = Minecraft.getInstance().player;
            return s.setAndContinue(p != null && p.isUsingItem() && p.getUseItem().getItem() instanceof LaserGunItem ? SHOOT : IDLE);
        }));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
