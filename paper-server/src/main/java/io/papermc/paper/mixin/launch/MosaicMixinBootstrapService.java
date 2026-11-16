package io.papermc.paper.mixin.launch;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public final class MosaicMixinBootstrapService implements IMixinServiceBootstrap {
    @Override
    public String getName() {
        return "Mosaic";
    }

    @Override
    public String getServiceClassName() {
        return MosaicMixinService.class.getName();
    }

    @Override
    public void bootstrap() {}
}
