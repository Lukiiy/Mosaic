package io.papermc.paper.mixin.launch;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.IAdviceProvider;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.IFeatureValidator;
import org.spongepowered.asm.service.IMixinAuditTrail;
import org.spongepowered.asm.service.IMixinInternal;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.ITransformer;
import org.spongepowered.asm.service.ITransformerProvider;
import org.spongepowered.asm.util.ReEntranceLock;

public final class MosaicMixinService implements IMixinService, IClassProvider, IClassBytecodeProvider, ITransformerProvider, IClassTracker {
    public static final ILogger LOGGER = new MosaicMixinLogger();

    public static volatile MixinClassLoader loader;
    private static volatile IMixinTransformer transformer;
    private final ReEntranceLock lock = new ReEntranceLock(1);

    static @Nullable IMixinTransformer getTransformer() {
        return transformer;
    }

    private @Nullable byte @Nullable [] bytes(@NonNull String name) throws IOException {
        return loader.getClassBytes(name);
    }

    @Override
    public @NonNull ClassNode getClassNode(final @NonNull String name) throws ClassNotFoundException, IOException {
        return this.getClassNode(name, true);
    }

    @Override
    public @NonNull ClassNode getClassNode(final @NonNull String name, final boolean runTransformers) throws ClassNotFoundException, IOException {
        return this.getClassNode(name, runTransformers, 0);
    }

    @Override
    public @NonNull ClassNode getClassNode(final @NonNull String name, final boolean runTransformers, final int readerFlags) throws ClassNotFoundException, IOException {
        ClassNode node = new ClassNode();

        new ClassReader(this.bytes(name)).accept(node, readerFlags);

        return node;
    }

    @Override
    public @NonNull URL @NonNull [] getClassPath() {
        return new URL[0];
    }

    @Override
    public @NonNull Class<?> findClass(final @NonNull String name) throws ClassNotFoundException {
        return loader.loadClass(name);
    }

    @Override
    public @NonNull Class<?> findClass(final @NonNull String name, final boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, loader);
    }

    @Override
    public @NonNull Class<? extends @NonNull Object> findAgentClass(final @NonNull String name, final boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, MosaicMixinService.class.getClassLoader());
    }

    @Override
    public @NonNull String getName() {
        return "Mosaic";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void prepare() {

    }

    @Override
    public MixinEnvironment.@NonNull Phase getInitialPhase() {
        return MixinEnvironment.Phase.PREINIT;
    }

    @Override
    public void offer(final @NonNull IMixinInternal internal) {
        if (internal instanceof IMixinTransformerFactory factory) transformer = factory.createTransformer();
    }

    @Override
    public void init() {

    }

    @Override
    public void beginPhase() {

    }

    @Override
    public void checkEnv(final @Nullable Object bootSource) {

    }

    @Override
    public @NonNull ReEntranceLock getReEntranceLock() {
        return this.lock;
    }

    @Override
    public @NonNull IClassProvider getClassProvider() {
        return this;
    }

    @Override
    public @NonNull IClassBytecodeProvider getBytecodeProvider() {
        return this;
    }

    @Override
    public @NonNull ITransformerProvider getTransformerProvider() {
        return this;
    }

    @Override
    public @NonNull IClassTracker getClassTracker() {
        return this;
    }

    @Override
    public @Nullable IMixinAuditTrail getAuditTrail() {
        return null;
    }

    @Override
    public @NonNull IFeatureValidator getFeatureValidator() {
        return IFeatureValidator.ALLOW_ALL;
    }

    @Override
    public @NonNull IAdviceProvider getAdviceProvider() {
        return (int _, @NonNull String _) -> "Update Mosaic";
    }

    @Override
    public @NonNull Collection<@NonNull String> getPlatformAgents() {
        return Collections.singletonList("org.spongepowered.asm.launch.platform.MixinPlatformAgentDefault");
    }

    @Override
    public @NonNull IContainerHandle getPrimaryContainer() {
        try {
            return new ContainerHandleURI(MosaicMixinService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (@NonNull Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public @NonNull Collection<@NonNull IContainerHandle> getMixinContainers() {
        return List.of();
    }

    @Override
    public @Nullable InputStream getResourceAsStream(final @NonNull String name) {
        return loader.getResourceAsStream(name);
    }

    @Override
    public void registerInvalidClass(final @Nullable String className) {

    }

    @Override
    public boolean isClassLoaded(final @NonNull String className) {
        return loader.isLoaded(className);
    }

    @Override
    public @NonNull String getClassRestrictions(final @NonNull String className) {
        return "";
    }

    @Override
    public @NonNull Collection<ITransformer> getTransformers() {
        return List.of();
    }

    @Override
    public @NonNull Collection<@NonNull ITransformer> getDelegatedTransformers() {
        return List.of();
    }

    @Override
    public void addTransformerExclusion(final @NonNull String name) {

    }

    @Override
    public @NonNull String getSideName() {
        return "SERVER";
    }

    @Override
    public MixinEnvironment.@NonNull CompatibilityLevel getMinCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_17;
    }

    @Override
    public MixinEnvironment.@NonNull CompatibilityLevel getMaxCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_25;
    }

    @Override
    public @NonNull ILogger getLogger(final @Nullable String name) {
        return LOGGER;
    }
}
