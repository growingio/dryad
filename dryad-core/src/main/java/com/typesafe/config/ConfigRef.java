package com.typesafe.config;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Component:
 * Description:
 * Date: 2016/10/28
 *
 * @author Andy Ai
 */
public class ConfigRef implements Config {
    private AtomicReference<Config> underlying;

    public ConfigRef(AtomicReference<Config> underlying) {
        this.underlying = underlying;
    }

    @Override
    public ConfigObject root() {
        return underlying.get().root();
    }

    @Override
    public ConfigOrigin origin() {
        return underlying.get().origin();
    }

    @Override
    public Config withFallback(ConfigMergeable other) {
        return underlying.get().withFallback(other);
    }

    @Override
    public Config resolve() {
        return underlying.get().resolve();
    }

    @Override
    public Config resolve(ConfigResolveOptions options) {
        return underlying.get().resolve(options);
    }

    @Override
    public boolean isResolved() {
        return underlying.get().isResolved();
    }

    @Override
    public Config resolveWith(Config source) {
        return underlying.get().resolveWith(source);
    }

    @Override
    public Config resolveWith(Config source, ConfigResolveOptions options) {
        return underlying.get().resolveWith(source, options);
    }

    @Override
    public void checkValid(Config reference, String... restrictToPaths) {
        underlying.get().checkValid(reference, restrictToPaths);
    }

    @Override
    public boolean hasPath(String path) {
        return underlying.get().hasPath(path);
    }

    @Override
    public boolean hasPathOrNull(String path) {
        return underlying.get().hasPathOrNull(path);
    }

    @Override
    public boolean isEmpty() {
        return underlying.get().isEmpty();
    }

    @Override
    public Set<Map.Entry<String, ConfigValue>> entrySet() {
        return underlying.get().entrySet();
    }

    @Override
    public boolean getIsNull(String path) {
        return underlying.get().getIsNull(path);
    }

    @Override
    public boolean getBoolean(String path) {
        return underlying.get().getBoolean(path);
    }

    @Override
    public Number getNumber(String path) {
        return underlying.get().getNumber(path);
    }

    @Override
    public int getInt(String path) {
        return underlying.get().getInt(path);
    }

    @Override
    public long getLong(String path) {
        return underlying.get().getLong(path);
    }

    @Override
    public double getDouble(String path) {
        return underlying.get().getDouble(path);
    }

    @Override
    public String getString(String path) {
        return underlying.get().getString(path);
    }

    @Override
    public <T extends Enum<T>> T getEnum(Class<T> enumClass, String path) {
        return underlying.get().getEnum(enumClass, path);
    }

    @Override
    public ConfigObject getObject(String path) {
        return underlying.get().getObject(path);
    }

    @Override
    public Config getConfig(String path) {
        return underlying.get().getConfig(path);
    }

    @Override
    public Object getAnyRef(String path) {
        return underlying.get().getAnyRef(path);
    }

    @Override
    public ConfigValue getValue(String path) {
        return underlying.get().getValue(path);
    }

    @Override
    public Long getBytes(String path) {
        return underlying.get().getBytes(path);
    }

    @Override
    public ConfigMemorySize getMemorySize(String path) {
        return underlying.get().getMemorySize(path);
    }

    @Override
    @Deprecated
    public Long getMilliseconds(String path) {
        throw new UnsupportedOperationException("Deprecated, replaced by getDuration(String, TimeUnit)");
    }

    @Override
    @Deprecated
    public Long getNanoseconds(String path) {
        throw new UnsupportedOperationException("Deprecated, replaced by getDuration(String, TimeUnit)");
    }

    @Override
    public long getDuration(String path, TimeUnit unit) {
        return underlying.get().getDuration(path, unit);
    }

    @Override
    public Duration getDuration(String path) {
        return underlying.get().getDuration(path);
    }

    @Override
    public ConfigList getList(String path) {
        return underlying.get().getList(path);
    }

    @Override
    public List<Boolean> getBooleanList(String path) {
        return underlying.get().getBooleanList(path);
    }

    @Override
    public List<Number> getNumberList(String path) {
        return underlying.get().getNumberList(path);
    }

    @Override
    public List<Integer> getIntList(String path) {
        return underlying.get().getIntList(path);
    }

    @Override
    public List<Long> getLongList(String path) {
        return underlying.get().getLongList(path);
    }

    @Override
    public List<Double> getDoubleList(String path) {
        return underlying.get().getDoubleList(path);
    }

    @Override
    public List<String> getStringList(String path) {
        return underlying.get().getStringList(path);
    }

    @Override
    public <T extends Enum<T>> List<T> getEnumList(Class<T> enumClass, String path) {
        return underlying.get().getEnumList(enumClass, path);
    }

    @Override
    public List<? extends ConfigObject> getObjectList(String path) {
        return underlying.get().getObjectList(path);
    }

    @Override
    public List<? extends Config> getConfigList(String path) {
        return underlying.get().getConfigList(path);
    }

    @Override
    public List<? extends Object> getAnyRefList(String path) {
        return underlying.get().getAnyRefList(path);
    }

    @Override
    public List<Long> getBytesList(String path) {
        return underlying.get().getBytesList(path);
    }

    @Override
    public List<ConfigMemorySize> getMemorySizeList(String path) {
        return underlying.get().getMemorySizeList(path);
    }

    @Override
    @Deprecated
    public List<Long> getMillisecondsList(String path) {
        throw new UnsupportedOperationException("Deprecated, replaced by getDurationList(String, TimeUnit)");
    }

    @Override
    @Deprecated
    public List<Long> getNanosecondsList(String path) {
        throw new UnsupportedOperationException("Deprecated, replaced by getDurationList(String, TimeUnit)");
    }

    @Override
    public List<Long> getDurationList(String path, TimeUnit unit) {
        return underlying.get().getDurationList(path, unit);
    }

    @Override
    public List<Duration> getDurationList(String path) {
        return underlying.get().getDurationList(path);
    }

    @Override
    public Config withOnlyPath(String path) {
        return underlying.get().withOnlyPath(path);
    }

    @Override
    public Config withoutPath(String path) {
        return underlying.get().withoutPath(path);
    }

    @Override
    public Config atPath(String path) {
        return underlying.get().atPath(path);
    }

    @Override
    public Config atKey(String key) {
        return underlying.get().atKey(key);
    }

    @Override
    public Config withValue(String path, ConfigValue value) {
        return underlying.get().withValue(path, value);
    }

    @Override
    public Period getPeriod(String path) {
        return underlying.get().getPeriod(path);
    }

    @Override
    public TemporalAmount getTemporal(String path) {
        return underlying.get().getTemporal(path);
    }
}
