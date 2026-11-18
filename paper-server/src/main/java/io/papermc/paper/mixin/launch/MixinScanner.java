package io.papermc.paper.mixin.launch;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public final class MixinScanner {
    private static final MosaicMixinLogger LOGGER = new MosaicMixinLogger();
    private static final String CONF_KEY = "mixin-config";

    private MixinScanner() {

    }

    static @NonNull Result scan(@NonNull Path pluginsDir) {
        List<URL> jars = new ArrayList<>();
        List<String> configs = new ArrayList<>();

        if (!Files.isDirectory(pluginsDir)) return new Result(jars, configs);

        try (Stream<Path> files = Files.list(pluginsDir)) {
            for (Path jar : files.filter(p -> p.toString().endsWith(".jar")).toList()) {
                try {
                    List<String> found = read(jar);
                    if (found.isEmpty()) continue;

                    jars.add(jar.toUri().toURL());
                    configs.addAll(found);
                } catch (@NonNull Exception e) {
                    LOGGER.warn("[Mosaic] skipping mixins from " + jar.getFileName() + ": " + e);
                }
            }
        } catch (@NonNull IOException e) {
            LOGGER.warn("[Mosaic] could not list " + pluginsDir + ": " + e);
        }

        return new Result(jars, configs);
    }

    private static @NonNull List<String> read(@NonNull Path jar) throws IOException {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            JarEntry yml = jarFile.getJarEntry("paper-plugin.yml");
            if (yml == null) return List.of();

            Object root;
            try (InputStream in = jarFile.getInputStream(yml)) {
                root = new Yaml(new SafeConstructor(new LoaderOptions())).load(in);
            }

            if (!(root instanceof Map<?, ?> map) || map.get(CONF_KEY) == null) return List.of();

            Object value = map.get(CONF_KEY);
            Collection<?> raw = value instanceof Collection<?> list ? list : List.of(value);

            return raw.stream().map(o -> String.valueOf(o).trim()).filter(s -> !s.isEmpty()).distinct()
                .filter(config -> {
                    if (jarFile.getJarEntry(config) != null) return true;

                    LOGGER.warn("[Mosaic] " + jar.getFileName() + ": mixin config '" + config + "' not found in jar, ignoring");
                    return false;
                }).toList();
        }
    }

    record Result(@NonNull List<URL> jars, @NonNull List<String> configs) {

    }
}
