package io.papermc.paper.mixin.launch;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

public final class MixinClassLoader extends URLClassLoader { // todo
    private static final String[] BRIDGED = {
        "org.spongepowered.asm.",
        "org.objectweb.asm.",
        "io.papermc.paper.mixin.launch.",
        "com.llamalad7.mixinextras."
    };

    private final ClassLoader bridge = MixinClassLoader.class.getClassLoader();
    private volatile IMixinTransformer transformer;
    private volatile URLClassLoader mixinSources;

    public MixinClassLoader(final @NonNull URL @NonNull [] urls, final @NonNull ClassLoader parent) {
        super(urls, parent);
    }

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public void setTransformer(final @NonNull IMixinTransformer transformer) {
        this.transformer = transformer;
    }

    boolean isLoaded(@NonNull String name) {
        return findLoadedClass(name.replace('/', '.')) != null;
    }

    byte @NonNull [] getClassBytes(@NonNull String name) throws IOException {
        name = name.replace('/', '.');

        try (InputStream in = this.getResourceAsStream(name.replace('.', '/') + ".class")) {
            return in == null ? null : in.readAllBytes();
        }
    }

    void setMixinSources(@NonNull URL @NonNull [] urls) {
         this.mixinSources = new URLClassLoader(urls, null);
    }

    @Override
    public @Nullable InputStream getResourceAsStream(@NonNull String name) {
        InputStream in = super.getResourceAsStream(name);
        URLClassLoader snap = this.mixinSources;

        return in != null || snap == null ? in : snap.getResourceAsStream(name);
    }

    private static boolean canTransform(@NonNull String name) {
        return !name.startsWith("org.apache.logging.log4j");
    }

    private static boolean isBridged(@NonNull String name) {
        return Arrays.stream(BRIDGED).anyMatch(name::startsWith);
    }

    @Override
    protected @NonNull Class<?> loadClass(@NonNull String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = findLoadedClass(name);

            if (clazz == null) {
                if (isBridged(name)) return this.bridge.loadClass(name);

                try {
                    clazz = getParent().loadClass(name); // JDK classes
                } catch (@NonNull ClassNotFoundException _) {
                    clazz = this.defineFromResource(name);
                }
            }

            if (resolve) this.resolveClass(clazz);

            return clazz;
        }
    }

    private @NonNull Class<?> defineFromResource(@NonNull String name) throws ClassNotFoundException {
        String path = name.replace('.', '/') + ".class";
        URL res = findResource(path);
        if (res == null) throw new ClassNotFoundException(name);

        try {
            byte[] bytes;

            try (InputStream in = res.openStream()) {
                bytes = in.readAllBytes();
            }

            IMixinTransformer snapshot = this.transformer;

            if (snapshot != null && canTransform(name)) {
                try {
                    bytes = snapshot.transformClassBytes(name, name, bytes);
                } catch (@NonNull Throwable e) {
                    throw new RuntimeException("Mixin transformation of " + name + " failed", e);
                }

                Class<?> existing = findLoadedClass(name); // class maaaay have been already defined while it was initializing
                if (existing != null) return existing;
            }

            int dot = name.lastIndexOf('.');
            if (dot > 0 && getDefinedPackage(name.substring(0, dot)) == null) {
                try {
                    definePackage(name.substring(0, dot), null, null, null, null, null, null, null);
                } catch (@NonNull IllegalArgumentException _) {

                }
            }

            CodeSource source = new CodeSource(this.getBase(res, path), (CodeSigner[]) null); // <- i dont understand thiss

            return defineClass(name, bytes, 0, bytes.length, new ProtectionDomain(source, null, this, null));
        } catch (@NonNull IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    private @NonNull URL getBase(@NonNull URL res, @NonNull String path) throws MalformedURLException {
        String resString = res.toString();
        int bang = resString.indexOf("!/");
        String base = bang > 0 ? resString.substring(resString.startsWith("jar:") ? 4 : 0, bang) : resString.substring(0, resString.length() - path.length());

        return URI.create(base).toURL();
    }
}
