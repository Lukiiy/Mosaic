package io.papermc.paper.mixin.launch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

public final class MosaicGlobalPropertyService implements IGlobalPropertyService {
    private static final Map<String, Object> PROPERTIES = new ConcurrentHashMap<>();

    private record StringKey(@NonNull String key) implements IPropertyKey {

    }

    public MosaicGlobalPropertyService() {

    }

    @Override
    public @NonNull IPropertyKey resolveKey(final @NonNull String name) {
        return new StringKey(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T> T getProperty(final @NonNull IPropertyKey key) {
        return (T) PROPERTIES.get(((StringKey) key).key());
    }

    @Override
    public void setProperty(final @NonNull IPropertyKey key, final @NonNull Object value) {
        PROPERTIES.put(((StringKey) key).key(), value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getProperty(final @NonNull IPropertyKey key, final @Nullable T defaultValue) {
        return (T) PROPERTIES.getOrDefault(((StringKey) key).key(), defaultValue);
    }

    @Override
    public @NonNull String getPropertyString(final @NonNull IPropertyKey key, final @NonNull String defaultValue) {
        Object obj = PROPERTIES.get(((StringKey) key).key());

        return obj != null ? obj.toString() : defaultValue;
    }
}
