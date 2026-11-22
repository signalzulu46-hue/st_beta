package com.stbeta.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class ClientArmEvents {
    public static final HumanoidModel.ArmPose ZOMBIE_GUN_POSE = HumanoidModel.ArmPose.create(
            "ST_BETA_ZOMBIE_GUN",
            false,
            (model, entity, arm) -> {
                float pitch = (float) Math.toRadians(entity.getXRot()) - 1.5708F;
                if (arm == HumanoidArm.RIGHT) {
                    model.rightArm.xRot = pitch;
                    model.rightArm.yRot = 0.0F;
                    model.rightArm.zRot = 0.0F;
                } else {
                    model.leftArm.xRot = pitch;
                    model.leftArm.yRot = 0.0F;
                    model.leftArm.zRot = 0.0F;
                }
            }
    );
}
