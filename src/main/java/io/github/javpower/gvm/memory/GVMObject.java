package io.github.javpower.gvm.memory;

import io.github.javpower.gvm.classloader.GVMClass;

import java.util.HashMap;
import java.util.Map;

/**
 * GVM对象实例，代表堆中的一个对象
 * 包含对象的类信息、实例字段和引用计数等信息
 */
public class GVMObject {
    
    // 对象状态枚举
    public enum ObjectState {
        ALIVE,      // 存活状态
        MARKED,     // 标记状态（GC标记阶段）
        DEAD        // 死亡状态（待回收）
    }
    
    private final long objectId;              // 对象唯一ID
    private final GVMClass objectClass;       // 对象所属的类
    private final Map<String, Object> instanceFields; // 实例字段
    private ObjectState state;                // 对象状态
    private int referenceCount;              // 引用计数
    private long lastAccessTime;             // 最后访问时间
    private boolean isArray;                 // 是否为数组对象
    private Object[] arrayData;              // 数组数据（如果是数组）
    private int arrayLength;                 // 数组长度（如果是数组）
    
    // 普通对象构造函数
    public GVMObject(long objectId, GVMClass objectClass) {
        this.objectId = objectId;
        this.objectClass = objectClass;
        this.instanceFields = new HashMap<>();
        this.state = ObjectState.ALIVE;
        this.referenceCount = 0;
        this.lastAccessTime = System.currentTimeMillis();
        this.isArray = false;
        initializeInstanceFields();
    }
    
    // 数组对象构造函数
    public GVMObject(long objectId, GVMClass objectClass, int arrayLength) {
        this.objectId = objectId;
        this.objectClass = objectClass;
        this.instanceFields = new HashMap<>();
        this.state = ObjectState.ALIVE;
        this.referenceCount = 0;
        this.lastAccessTime = System.currentTimeMillis();
        this.isArray = true;
        this.arrayLength = arrayLength;
        this.arrayData = new Object[arrayLength];
    }
    
    /**
     * 初始化实例字段
     */
    private void initializeInstanceFields() {
        if (objectClass != null && objectClass.getFields() != null) {
            for (Map.Entry<String, GVMClass.FieldInfo> entry : objectClass.getFields().entrySet()) {
                String fieldName = entry.getKey();
                GVMClass.FieldInfo fieldInfo = entry.getValue();
                
                // 只初始化实例字段，跳过静态字段
                if (!fieldInfo.isStatic()) {
                    Object defaultValue = getDefaultValue(fieldInfo.getDescriptor());
                    instanceFields.put(fieldName, defaultValue);
                }
            }
        }
    }
    
    /**
     * 根据字段描述符获取默认值
     */
    private Object getDefaultValue(String descriptor) {
        switch (descriptor) {
            case "I": return 0;           // int
            case "J": return 0L;          // long
            case "F": return 0.0f;        // float
            case "D": return 0.0;         // double
            case "Z": return false;       // boolean
            case "B": return (byte) 0;    // byte
            case "C": return '\0';        // char
            case "S": return (short) 0;   // short
            default: return null;         // 引用类型
        }
    }
    
    // Getter和Setter方法
    public long getObjectId() {
        return objectId;
    }
    
    public GVMClass getObjectClass() {
        return objectClass;
    }
    
    public ObjectState getState() {
        return state;
    }
    
    public void setState(ObjectState state) {
        this.state = state;
    }
    
    public int getReferenceCount() {
        return referenceCount;
    }
    
    public void incrementReferenceCount() {
        this.referenceCount++;
        updateLastAccessTime();
    }
    
    public void decrementReferenceCount() {
        this.referenceCount = Math.max(0, this.referenceCount - 1);
    }
    
    public long getLastAccessTime() {
        return lastAccessTime;
    }
    
    public void updateLastAccessTime() {
        this.lastAccessTime = System.currentTimeMillis();
    }
    
    public boolean isArray() {
        return isArray;
    }
    
    public int getArrayLength() {
        return arrayLength;
    }
    
    public Object[] getArrayData() {
        return arrayData;
    }
    
    /**
     * 获取实例字段值
     */
    public Object getInstanceField(String fieldName) {
        updateLastAccessTime();
        return instanceFields.get(fieldName);
    }
    
    /**
     * 设置实例字段值
     */
    public void setInstanceField(String fieldName, Object value) {
        updateLastAccessTime();
        instanceFields.put(fieldName, value);
    }
    
    /**
     * 获取数组元素
     */
    public Object getArrayElement(int index) {
        if (!isArray || index < 0 || index >= arrayLength) {
            throw new ArrayIndexOutOfBoundsException("Index: " + index + ", Length: " + arrayLength);
        }
        updateLastAccessTime();
        return arrayData[index];
    }
    
    /**
     * 设置数组元素
     */
    public void setArrayElement(int index, Object value) {
        if (!isArray || index < 0 || index >= arrayLength) {
            throw new ArrayIndexOutOfBoundsException("Index: " + index + ", Length: " + arrayLength);
        }
        updateLastAccessTime();
        arrayData[index] = value;
    }
    
    /**
     * 获取对象大小估算（字节）
     */
    public long getEstimatedSize() {
        long size = 64; // 对象头部大小估算
        
        // 实例字段大小
        for (Object value : instanceFields.values()) {
            if (value != null) {
                if (value instanceof Integer || value instanceof Float) {
                    size += 4;
                } else if (value instanceof Long || value instanceof Double) {
                    size += 8;
                } else if (value instanceof Boolean || value instanceof Byte) {
                    size += 1;
                } else if (value instanceof Character || value instanceof Short) {
                    size += 2;
                } else {
                    size += 8; // 引用大小
                }
            }
        }
        
        // 数组数据大小
        if (isArray) {
            size += arrayLength * 8; // 假设每个数组元素8字节
        }
        
        return size;
    }
    
    @Override
    public String toString() {
        return String.format("GVMObject{id=%d, class=%s, state=%s, refCount=%d, isArray=%s}", 
                objectId, 
                objectClass != null ? objectClass.getClassName() : "null", 
                state, 
                referenceCount, 
                isArray);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GVMObject gvmObject = (GVMObject) obj;
        return objectId == gvmObject.objectId;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(objectId);
    }
} 