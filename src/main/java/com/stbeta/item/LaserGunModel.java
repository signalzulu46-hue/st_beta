package com.stbeta.item;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LaserGunModel extends GeoModel<LaserGunItem> {

    @Override
    public ResourceLocation getModelResource(LaserGunItem animatable) {
        return new ResourceLocation("st_beta", "geo/item/astro_k9_lazer.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LaserGunItem animatable) {
        return new ResourceLocation("st_beta", "textures/item/silah.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LaserGunItem animatable) {
        return new ResourceLocation("st_beta", "animations/item/astro_k9_lazer.animation.json");
    }
}
