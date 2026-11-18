package io.papermc.paper.mixin.launch;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class MosaicPlugins { // registry, ig
    private static final Map<String, List<String>> MOSAIC = new ConcurrentHashMap<>(); // plugins that have mixin
    private static final Set<String> BLOCKED = ConcurrentHashMap.newKeySet();

    private MosaicPlugins() {

    }

    static void register(final String plugin, final List<String> configs) {
        MOSAIC.put(plugin, configs);
    }

    static void block(final String plugin, final String reason) {
        BLOCKED.add(plugin);

        MosaicMixinService.LOGGER.warn("[Mosaic] not loading plugin '" + plugin + "': " + reason);
    }

    public static boolean is(final String plugin) {
        return MOSAIC.containsKey(plugin);
    }

    public static boolean isBlocked(final String plugin) {
        return BLOCKED.contains(plugin);
    }

    public static List<String> configsOf(final String plugin) {
        return MOSAIC.getOrDefault(plugin, List.of());
    }

    static String ownerOf(final String config) {
        return MOSAIC.entrySet().stream().filter(e -> e.getValue().contains(config)).map(Map.Entry::getKey).findFirst().orElse("Mosaic");
    }
}
