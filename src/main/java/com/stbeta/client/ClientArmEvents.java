package com.stbeta.client;
import com.stbeta.item.LaserGunItem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "st_beta", value = Dist.CLIENT)
public class ClientArmEvents {
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer p)) return;
        boolean main = p.getMainHandItem().getItem() instanceof LaserGunItem;
        boolean off = p.getOffhandItem().getItem() instanceof LaserGunItem;
        if (!main && !off) return;
        PlayerModel<AbstractClientPlayer> m = event.getRenderer().getModel();
        boolean rightMain = p.getMainArm() == HumanoidArm.RIGHT;
        float pitch = (float) Math.toRadians(p.getXRot()) - ((float) Math.PI / 2.0F);
        if ((main && rightMain) || (off && !rightMain)) { m.rightArm.xRot = pitch; m.rightArm.yRot = 0.0F; m.rightArm.zRot = 0.0F; }
        if ((main && !rightMain) || (off && rightMain)) { m.leftArm.xRot = pitch; m.leftArm.yRot = 0.0F; m.leftArm.zRot = 0.0F; }
    }
}
