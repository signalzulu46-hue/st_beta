package com.stbeta.client;
import com.stbeta.item.LaserGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "st_beta", value = Dist.CLIENT)
public class ClientArmEvents {
    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Pre<AbstractClientPlayer, ?> event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer p)) return;
        if (!(event.getRenderer().getModel() instanceof HumanoidModel<?> m)) return;
        boolean main = p.getMainHandItem().getItem() instanceof LaserGunItem;
        boolean off = p.getOffhandItem().getItem() instanceof LaserGunItem;
        if (!main && !off) return;
        boolean rightMain = p.getMainArm() == HumanoidArm.RIGHT;
        float pitch = (float) Math.toRadians(p.getXRot()) - ((float) Math.PI / 2.0F);
        if ((main && rightMain) || (off && !rightMain)) {
            m.rightArmPose = HumanoidModel.ArmPose.ITEM;
            m.rightArm.xRot = pitch; m.rightArm.yRot = 0.0F; m.rightArm.zRot = 0.0F;
        }
        if ((main && !rightMain) || (off && rightMain)) {
            m.leftArmPose = HumanoidModel.ArmPose.ITEM;
            m.leftArm.xRot = pitch; m.leftArm.yRot = 0.0F; m.leftArm.zRot = 0.0F;
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getItemStack().getItem() instanceof LaserGunItem) {
            event.getPoseStack().translate(0.0F, -0.05F, 0.0F);
        }
    }
}
