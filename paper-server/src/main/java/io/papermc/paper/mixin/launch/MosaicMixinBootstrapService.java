package io.papermc.paper.mixin.launch;

import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.service.IMixinServiceBootstrap;

public final class MosaicMixinBootstrapService implements IMixinServiceBootstrap {
    @Override
    public @NonNull String getName() {
        return "Mosaic";
    }

    @Override
    public @NonNull String getServiceClassName() {
        return MosaicMixinService.class.getName();
    }

    @Override
    public void bootstrap() {

    }
}
