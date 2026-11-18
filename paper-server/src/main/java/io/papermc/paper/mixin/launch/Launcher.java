package io.papermc.paper.mixin.launch;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;

public final class Launcher {
    private Launcher() {

    }

    public static void main(@NonNull String @NonNull [] args) throws Throwable {
        MixinClassLoader loader = new MixinClassLoader(getClasspath(), ClassLoader.getPlatformClassLoader());

        Thread.currentThread().setContextClassLoader(loader);
        MosaicMixinBootstrap.init(loader);

        Class<?> main = Class.forName("org.bukkit.craftbukkit.Main", true, loader);

        try {
            main.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (@NonNull InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private static @NonNull URL @NonNull [] getClasspath() throws Exception {
        ClassLoader loader = Launcher.class.getClassLoader();
        if (loader instanceof URLClassLoader u) return u.getURLs();

        List<URL> urls = new ArrayList<>();
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) urls.add(new File(entry).toURI().toURL());

        return urls.toArray(new URL[0]);
    }
}
