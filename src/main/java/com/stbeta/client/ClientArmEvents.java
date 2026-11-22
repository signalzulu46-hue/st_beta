package com.stbeta.client;
import com.stbeta.item.LaserGunItem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "st_beta", value = Dist.CLIENT)
public class ClientArmEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayer(RenderLivingEvent.Pre<AbstractClientPlayer, ?> event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer p)) return;
        if (!(event.getRenderer().getModel() instanceof PlayerModel<?> m)) return;
        boolean main = p.getMainHandItem().getItem() instanceof LaserGunItem;
        boolean off = p.getOffhandItem().getItem() instanceof LaserGunItem;
        if (!main && !off) return;
        boolean rightMain = p.getMainArm() == HumanoidArm.RIGHT;
        float zombiePitch = (float) Math.toRadians(p.getXRot()) - 1.5708F;
        if ((main && rightMain) || (off && !rightMain)) {
            m.rightArm.xRot = zombiePitch; m.rightArm.yRot = 0.0F; m.rightArm.zRot = 0.0F;
            m.rightSleeve.copyFrom(m.rightArm);
        }
        if ((main && !rightMain) || (off && rightMain)) {
            m.leftArm.xRot = zombiePitch; m.leftArm.yRot = 0.0F; m.leftArm.zRot = 0.0F;
            m.leftSleeve.copyFrom(m.leftArm);
        }
    }
}
