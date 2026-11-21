package com.stbeta.item;

import software.bernie.geckolib.renderer.GeoItemRenderer;

public class LaserGunRenderer extends GeoItemRenderer<LaserGunItem> {

    public LaserGunRenderer() {
        super(new LaserGunModel());
    }
}
