package cn.jieyuc.file.application.port.storage;

import cn.jieyuc.file.domain.StorageProviderCode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** 按提供方代号索引唯一对象存储 Adapter。 */
public final class StorageProviderRegistry {
    private final Map<StorageProviderCode, ObjectStoragePort> ports;

    /** 构建不可变注册表，并拒绝重复提供方。 */
    public StorageProviderRegistry(List<ObjectStoragePort> ports) {
        EnumMap<StorageProviderCode, ObjectStoragePort> indexed = new EnumMap<>(StorageProviderCode.class);
        for (ObjectStoragePort port : ports) {
            if (indexed.putIfAbsent(port.providerCode(), port) != null) {
                throw new IllegalArgumentException("对象存储提供方重复: " + port.providerCode());
            }
        }
        this.ports = Map.copyOf(indexed);
    }

    /** 获取指定提供方 Adapter，缺失配置时快速失败。 */
    public ObjectStoragePort require(StorageProviderCode providerCode) {
        ObjectStoragePort port = ports.get(providerCode);
        if (port == null) {
            throw new IllegalStateException("对象存储提供方未配置: " + providerCode);
        }
        return port;
    }
}
