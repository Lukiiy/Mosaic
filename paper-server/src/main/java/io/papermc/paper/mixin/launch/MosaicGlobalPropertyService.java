package io.papermc.paper.mixin.launch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

public final class MosaicGlobalPropertyService implements IGlobalPropertyService {
    private static final Map<String, Object> PROPERTIES = new ConcurrentHashMap<>();

    private record StringKey(String key) implements IPropertyKey {}

    public MosaicGlobalPropertyService() {}

    @Override
    public IPropertyKey resolveKey(final String name) {
        return new StringKey(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(final IPropertyKey key) {
        return (T) PROPERTIES.get(((StringKey) key).key());
    }

    @Override
    public void setProperty(final IPropertyKey key, final Object value) {
        PROPERTIES.put(((StringKey) key).key(), value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(final IPropertyKey key, final T defaultValue) {
        return (T) PROPERTIES.getOrDefault(((StringKey) key).key(), defaultValue);
    }

    @Override
    public String getPropertyString(final IPropertyKey key, final String defaultValue) {
        Object obj = PROPERTIES.get(((StringKey) key).key());

        return obj != null ? obj.toString() : defaultValue;
    }
}
