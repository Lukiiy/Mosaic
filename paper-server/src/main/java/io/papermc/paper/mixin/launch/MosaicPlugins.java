package io.papermc.paper.mixin.launch;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;

public final class MosaicPlugins { // registry, ig
    private static final Map<String, List<String>> MOSAIC = new ConcurrentHashMap<>(); // plugins that have mixin
    private static final Set<String> BLOCKED = ConcurrentHashMap.newKeySet();

    private MosaicPlugins() {

    }

    static void register(final @NonNull String plugin, final @NonNull List<String> configs) {
        MOSAIC.put(plugin, configs);
    }

    static void block(final @NonNull String plugin, final @NonNull String reason) {
        BLOCKED.add(plugin);

        MosaicMixinService.LOGGER.warn("[Mosaic] not loading plugin '" + plugin + "': " + reason);
    }

    public static boolean is(final @NonNull String plugin) {
        return MOSAIC.containsKey(plugin);
    }

    public static boolean isBlocked(final @NonNull String plugin) {
        return BLOCKED.contains(plugin);
    }

    public static @NonNull List<String> configsOf(final @NonNull String plugin) {
        return MOSAIC.getOrDefault(plugin, List.of());
    }

    static @NonNull String ownerOf(final @NonNull String config) {
        return MOSAIC.entrySet().stream().filter(e -> e.getValue().contains(config)).map(Map.Entry::getKey).findFirst().orElse("Mosaic");
    }
}
