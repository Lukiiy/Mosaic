package io.papermc.paper.mixin.launch;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

public final class MosaicMixinBootstrap {
    private MosaicMixinBootstrap() {

    }

    public static void init(@NonNull MixinClassLoader loader) {
        System.setProperty("mixin.bootstrapService", MosaicMixinBootstrapService.class.getName());
        System.setProperty("mixin.service", MosaicMixinService.class.getName());
        MosaicMixinService.loader = loader;

        MixinBootstrap.init();
        MixinExtrasBootstrap.init();

        MixinScanner.Result found = MixinScanner.scan(Path.of("plugins"));

        loader.setMixinSources(found.jars().toArray(new URL[0]));
        found.configs().forEach(Mixins::addConfiguration);
        System.out.println("[Mosaic] plugin mixin configss " + found.configs());

        try {
            Method invok = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);

            invok.setAccessible(true);
            invok.invoke(null, MixinEnvironment.Phase.INIT);
            invok.invoke(null, MixinEnvironment.Phase.DEFAULT);
        } catch (@NonNull ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        IMixinTransformer transform = MosaicMixinService.getTransformer();
        if (transform == null) throw new IllegalStateException("Mixin did not have a transformer");

        loader.setTransformer(transform);
    }
}
