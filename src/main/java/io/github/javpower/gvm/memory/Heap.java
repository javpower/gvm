package io.github.javpower.gvm.memory;

import io.github.javpower.gvm.classloader.GVMClass;
import io.github.javpower.gvm.utils.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * GVM堆内存管理器
 * 负责对象的分配、管理和垃圾回收
 */
public class Heap {
    
    // 堆配置
    public static class HeapConfig {
        private long initialSize = 16 * 1024 * 1024;    // 初始堆大小：16MB
        private long maxSize = 256 * 1024 * 1024;       // 最大堆大小：256MB
        private double gcTriggerRatio = 0.8;            // GC触发比例：80%
        private int youngGenRatio = 30;                 // 年轻代比例：30%
        private int oldGenRatio = 70;                   // 老年代比例：70%
        
        // Getter和Setter方法
        public long getInitialSize() { return initialSize; }
        public void setInitialSize(long initialSize) { this.initialSize = initialSize; }
        
        public long getMaxSize() { return maxSize; }
        public void setMaxSize(long maxSize) { this.maxSize = maxSize; }
        
        public double getGcTriggerRatio() { return gcTriggerRatio; }
        public void setGcTriggerRatio(double gcTriggerRatio) { this.gcTriggerRatio = gcTriggerRatio; }
        
        public int getYoungGenRatio() { return youngGenRatio; }
        public void setYoungGenRatio(int youngGenRatio) { this.youngGenRatio = youngGenRatio; }
        
        public int getOldGenRatio() { return oldGenRatio; }
        public void setOldGenRatio(int oldGenRatio) { this.oldGenRatio = oldGenRatio; }
    }
    
    private final HeapConfig config;
    private final AtomicLong nextObjectId;              // 下一个对象ID
    private final ConcurrentHashMap<Long, GVMObject> objects; // 所有对象
    private final ConcurrentHashMap<Long, GVMObject> youngGeneration; // 年轻代
    private final ConcurrentHashMap<Long, GVMObject> oldGeneration;   // 老年代
    
    private volatile long currentSize;                  // 当前堆大小
    private volatile long allocatedObjects;             // 已分配对象数量
    private volatile long gcCount;                      // GC次数
    private volatile long lastGcTime;                   // 最后一次GC时间
    
    public Heap() {
        this(new HeapConfig());
    }
    
    public Heap(HeapConfig config) {
        this.config = config;
        this.nextObjectId = new AtomicLong(1);
        this.objects = new ConcurrentHashMap<>();
        this.youngGeneration = new ConcurrentHashMap<>();
        this.oldGeneration = new ConcurrentHashMap<>();
        this.currentSize = 0;
        this.allocatedObjects = 0;
        this.gcCount = 0;
        this.lastGcTime = System.currentTimeMillis();
    }
    
    /**
     * 分配新对象
     */
    public synchronized GVMObject allocateObject(GVMClass gvmClass) {
        // 检查是否需要触发GC
        if (shouldTriggerGC()) {
            runGarbageCollection();
        }
        
        // 检查堆大小限制
        if (currentSize >= config.getMaxSize()) {
            throw new OutOfMemoryError("GVM heap space");
        }
        
        long objectId = nextObjectId.getAndIncrement();
        GVMObject object = new GVMObject(objectId, gvmClass);
        
        // 新对象默认放入年轻代
        objects.put(objectId, object);
        youngGeneration.put(objectId, object);
        
        currentSize += object.getEstimatedSize();
        allocatedObjects++;
        
        Logger.memory("分配对象: ID=%d, 类=%s, 大小=%d bytes, 总对象数=%d", 
                     objectId, gvmClass.getClassName(), object.getEstimatedSize(), objects.size());
        Logger.debug("堆内存状态: 已用=%d bytes, 对象=%d个 (年轻代=%d, 老年代=%d)", 
                    currentSize, objects.size(), youngGeneration.size(), oldGeneration.size());
        
        return object;
    }
    
    /**
     * 分配数组对象
     */
    public synchronized GVMObject allocateArray(GVMClass arrayClass, int length) {
        // 检查是否需要触发GC
        if (shouldTriggerGC()) {
            runGarbageCollection();
        }
        
        // 检查堆大小限制
        if (currentSize >= config.getMaxSize()) {
            throw new OutOfMemoryError("GVM heap space");
        }
        
        long objectId = nextObjectId.getAndIncrement();
        GVMObject arrayObject = new GVMObject(objectId, arrayClass, length);
        
        // 新对象默认放入年轻代
        objects.put(objectId, arrayObject);
        youngGeneration.put(objectId, arrayObject);
        
        currentSize += arrayObject.getEstimatedSize();
        allocatedObjects++;
        
        return arrayObject;
    }
    
    /**
     * 获取对象
     */
    public GVMObject getObject(long objectId) {
        return objects.get(objectId);
    }
    
    /**
     * 检查是否应该触发GC
     */
    private boolean shouldTriggerGC() {
        return (double) currentSize / config.getMaxSize() >= config.getGcTriggerRatio();
    }
    
    /**
     * 运行垃圾回收
     */
    public synchronized void runGarbageCollection() {
        long startTime = System.currentTimeMillis();
        long beforeSize = currentSize;
        int beforeObjectCount = objects.size();
        
        Logger.gc("开始垃圾回收...");
        Logger.gc("GC前: 内存=%d bytes, 对象=%d个, 年轻代=%d个, 老年代=%d个", 
                 beforeSize, beforeObjectCount, youngGeneration.size(), oldGeneration.size());
        
        // 标记阶段：标记所有可达对象
        Logger.gc("阶段1: 标记可达对象");
        markReachableObjects();
        
        // 清除阶段：回收未标记的对象
        Logger.gc("阶段2: 清除不可达对象");
        sweepUnreachableObjects();
        
        // 整理阶段：将长期存活的对象移到老年代
        Logger.gc("阶段3: 晋升长期存活对象");
        promoteToOldGeneration();
        
        // 重置所有对象状态
        Logger.gc("阶段4: 重置对象状态");
        resetObjectStates();
        
        gcCount++;
        lastGcTime = System.currentTimeMillis();
        
        long afterSize = currentSize;
        int afterObjectCount = objects.size();
        long gcTime = lastGcTime - startTime;
        
        Logger.gc("垃圾回收完成: 耗时%dms", gcTime);
        Logger.gc("GC后: 内存=%d bytes (释放%d), 对象=%d个 (回收%d), 年轻代=%d个, 老年代=%d个", 
                 afterSize, beforeSize - afterSize, afterObjectCount, beforeObjectCount - afterObjectCount,
                 youngGeneration.size(), oldGeneration.size());
    }
    
    /**
     * 标记可达对象（简化版本）
     * 在真实JVM中，这会从GC Roots开始遍历所有可达对象
     */
    private void markReachableObjects() {
        // 简化版本：将引用计数大于0的对象标记为可达
        for (GVMObject object : objects.values()) {
            if (object.getReferenceCount() > 0) {
                object.setState(GVMObject.ObjectState.MARKED);
                markObjectReferences(object);
            }
        }
    }
    
    /**
     * 标记对象引用的其他对象
     */
    private void markObjectReferences(GVMObject object) {
        // 简化版本：遍历对象的实例字段，标记引用的其他对象
        if (object.getObjectClass() != null && object.getObjectClass().getFields() != null) {
            for (Map.Entry<String, GVMClass.FieldInfo> entry : object.getObjectClass().getFields().entrySet()) {
                Object fieldValue = object.getInstanceField(entry.getKey());
                if (fieldValue instanceof GVMObject) {
                    GVMObject referencedObject = (GVMObject) fieldValue;
                    if (referencedObject.getState() != GVMObject.ObjectState.MARKED) {
                        referencedObject.setState(GVMObject.ObjectState.MARKED);
                        markObjectReferences(referencedObject);
                    }
                }
            }
        }
        
        // 如果是数组，标记数组元素
        if (object.isArray() && object.getArrayData() != null) {
            for (Object element : object.getArrayData()) {
                if (element instanceof GVMObject) {
                    GVMObject referencedObject = (GVMObject) element;
                    if (referencedObject.getState() != GVMObject.ObjectState.MARKED) {
                        referencedObject.setState(GVMObject.ObjectState.MARKED);
                        markObjectReferences(referencedObject);
                    }
                }
            }
        }
    }
    
    /**
     * 清除未标记的对象
     */
    private void sweepUnreachableObjects() {
        List<Long> objectsToRemove = new ArrayList<>();
        
        for (Map.Entry<Long, GVMObject> entry : objects.entrySet()) {
            GVMObject object = entry.getValue();
            if (object.getState() != GVMObject.ObjectState.MARKED) {
                objectsToRemove.add(entry.getKey());
                currentSize -= object.getEstimatedSize();
            }
        }
        
        // 移除未标记的对象
        for (Long objectId : objectsToRemove) {
            objects.remove(objectId);
            youngGeneration.remove(objectId);
            oldGeneration.remove(objectId);
        }
    }
    
    /**
     * 将长期存活的对象提升到老年代
     */
    private void promoteToOldGeneration() {
        long currentTime = System.currentTimeMillis();
        long promotionThreshold = 60000; // 1分钟
        
        Iterator<Map.Entry<Long, GVMObject>> iterator = youngGeneration.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, GVMObject> entry = iterator.next();
            GVMObject object = entry.getValue();
            
            // 如果对象存活时间超过阈值，提升到老年代
            if (currentTime - object.getLastAccessTime() > promotionThreshold) {
                iterator.remove();
                oldGeneration.put(entry.getKey(), object);
            }
        }
    }
    
    /**
     * 重置对象状态
     */
    private void resetObjectStates() {
        for (GVMObject object : objects.values()) {
            if (object.getState() == GVMObject.ObjectState.MARKED) {
                object.setState(GVMObject.ObjectState.ALIVE);
            }
        }
    }
    
    /**
     * 增加对象引用
     */
    public void addReference(long objectId) {
        GVMObject object = objects.get(objectId);
        if (object != null) {
            object.incrementReferenceCount();
        }
    }
    
    /**
     * 减少对象引用
     */
    public void removeReference(long objectId) {
        GVMObject object = objects.get(objectId);
        if (object != null) {
            object.decrementReferenceCount();
        }
    }
    
    /**
     * 获取堆统计信息
     */
    public HeapStatistics getStatistics() {
        return new HeapStatistics(
                currentSize,
                config.getMaxSize(),
                allocatedObjects,
                objects.size(),
                youngGeneration.size(),
                oldGeneration.size(),
                gcCount,
                lastGcTime
        );
    }
    
    /**
     * 获取堆的详细信息
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 堆内存详情 ===\n");
        sb.append(String.format("最大大小: %d MB (%d bytes)\n", config.getMaxSize() / (1024 * 1024), config.getMaxSize()));
        sb.append(String.format("当前使用: %d bytes (%.2f%%)\n", currentSize, (double) currentSize / config.getMaxSize() * 100));
        sb.append(String.format("对象总数: %d\n", objects.size()));
        sb.append(String.format("年轻代: %d 个对象\n", youngGeneration.size()));
        sb.append(String.format("老年代: %d 个对象\n", oldGeneration.size()));
        sb.append(String.format("GC次数: %d\n", gcCount));
        
        sb.append("\n--- 堆中的对象 ---\n");
        if (objects.isEmpty()) {
            sb.append("(无对象)\n");
        } else {
            int count = 0;
            for (GVMObject obj : objects.values()) {
                if (count >= 10) { // 最多显示10个对象，避免输出过长
                    sb.append(String.format("... 还有 %d 个对象\n", objects.size() - count));
                    break;
                }
                sb.append(String.format("  对象ID=%d, 类=%s, 状态=%s, 大小=%d bytes, 引用数=%d\n",
                    obj.getObjectId(), obj.getObjectClass().getClassName(), 
                    obj.getState(), obj.getEstimatedSize(), obj.getReferenceCount()));
                if (obj.isArray()) {
                    sb.append(String.format("    数组长度: %d\n", obj.getArrayLength()));
                }
                count++;
            }
        }
        sb.append("================\n");
        return sb.toString();
    }
    
    /**
     * 堆统计信息
     */
    public static class HeapStatistics {
        private final long currentSize;
        private final long maxSize;
        private final long totalAllocated;
        private final int liveObjects;
        private final int youngGenObjects;
        private final int oldGenObjects;
        private final long gcCount;
        private final long lastGcTime;
        
        public HeapStatistics(long currentSize, long maxSize, long totalAllocated, 
                            int liveObjects, int youngGenObjects, int oldGenObjects,
                            long gcCount, long lastGcTime) {
            this.currentSize = currentSize;
            this.maxSize = maxSize;
            this.totalAllocated = totalAllocated;
            this.liveObjects = liveObjects;
            this.youngGenObjects = youngGenObjects;
            this.oldGenObjects = oldGenObjects;
            this.gcCount = gcCount;
            this.lastGcTime = lastGcTime;
        }
        
        // Getter方法
        public long getCurrentSize() { return currentSize; }
        public long getMaxSize() { return maxSize; }
        public long getTotalAllocated() { return totalAllocated; }
        public int getLiveObjects() { return liveObjects; }
        public int getYoungGenObjects() { return youngGenObjects; }
        public int getOldGenObjects() { return oldGenObjects; }
        public long getGcCount() { return gcCount; }
        public long getLastGcTime() { return lastGcTime; }
        
        public double getUsageRatio() {
            return maxSize > 0 ? (double) currentSize / maxSize : 0;
        }
        
        @Override
        public String toString() {
            return String.format("HeapStatistics{size=%d/%d (%.1f%%), objects=%d (young=%d, old=%d), gc=%d}", 
                    currentSize, maxSize, getUsageRatio() * 100, 
                    liveObjects, youngGenObjects, oldGenObjects, gcCount);
        }
    }
    
    // Getter方法
    public HeapConfig getConfig() {
        return config;
    }
    
    public long getCurrentSize() {
        return currentSize;
    }
    
    public long getAllocatedObjects() {
        return allocatedObjects;
    }
    
    public long getGcCount() {
        return gcCount;
    }
} 