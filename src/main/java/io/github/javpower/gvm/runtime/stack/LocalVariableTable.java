package io.github.javpower.gvm.runtime.stack;

/**
 * 局部变量表 - 存储方法参数和局部变量
 * 每个Slot可以存储32位的数据类型，long和double占用两个Slot
 */
public class LocalVariableTable {
    
    /**
     * 局部变量槽位
     */
    public static class Slot {
        private Object value;  // 存储值
        private String type;   // 类型信息
        
        public Slot() {
            this.value = null;
            this.type = "empty";
        }
        
        public void setInt(int value) {
            this.value = value;
            this.type = "int";
        }
        
        public void setFloat(float value) {
            this.value = value;
            this.type = "float";
        }
        
        public void setReference(Object value) {
            this.value = value;
            this.type = "reference";
        }
        
        public void setLong(long value) {
            this.value = value;
            this.type = "long";
        }
        
        public void setDouble(double value) {
            this.value = value;
            this.type = "double";
        }
        
        public Object getValue() {
            return value;
        }
        
        public String getType() {
            return type;
        }
        
        public int getInt() {
            if (!"int".equals(type)) {
                throw new RuntimeException("Type mismatch: expected int, got " + type);
            }
            return (Integer) value;
        }
        
        public float getFloat() {
            if (!"float".equals(type)) {
                throw new RuntimeException("Type mismatch: expected float, got " + type);
            }
            return (Float) value;
        }
        
        public Object getReference() {
            if (!"reference".equals(type)) {
                throw new RuntimeException("Type mismatch: expected reference, got " + type);
            }
            return value;
        }
        
        public long getLong() {
            if (!"long".equals(type)) {
                throw new RuntimeException("Type mismatch: expected long, got " + type);
            }
            return (Long) value;
        }
        
        public double getDouble() {
            if (!"double".equals(type)) {
                throw new RuntimeException("Type mismatch: expected double, got " + type);
            }
            return (Double) value;
        }
        
        @Override
        public String toString() {
            return String.format("Slot[type=%s, value=%s]", type, value);
        }
    }
    
    private final Slot[] slots;
    private final int maxLocals;
    
    public LocalVariableTable(int maxLocals) {
        this.maxLocals = maxLocals;
        this.slots = new Slot[maxLocals];
        for (int i = 0; i < maxLocals; i++) {
            slots[i] = new Slot();
        }
    }
    
    /**
     * 设置int类型局部变量
     */
    public void setInt(int index, int value) {
        checkIndex(index);
        slots[index].setInt(value);
    }
    
    /**
     * 获取int类型局部变量
     */
    public int getInt(int index) {
        checkIndex(index);
        return slots[index].getInt();
    }
    
    /**
     * 设置float类型局部变量
     */
    public void setFloat(int index, float value) {
        checkIndex(index);
        slots[index].setFloat(value);
    }
    
    /**
     * 获取float类型局部变量
     */
    public float getFloat(int index) {
        checkIndex(index);
        return slots[index].getFloat();
    }
    
    /**
     * 设置引用类型局部变量
     */
    public void setReference(int index, Object value) {
        checkIndex(index);
        slots[index].setReference(value);
    }
    
    /**
     * 获取引用类型局部变量
     */
    public Object getReference(int index) {
        checkIndex(index);
        return slots[index].getReference();
    }
    
    /**
     * 设置long类型局部变量 (占用两个slot)
     */
    public void setLong(int index, long value) {
        checkIndex(index);
        checkIndex(index + 1);
        slots[index].setLong(value);
        slots[index + 1].setLong(value); // long占用两个slot
    }
    
    /**
     * 获取long类型局部变量
     */
    public long getLong(int index) {
        checkIndex(index);
        checkIndex(index + 1);
        return slots[index].getLong();
    }
    
    /**
     * 设置double类型局部变量 (占用两个slot)
     */
    public void setDouble(int index, double value) {
        checkIndex(index);
        checkIndex(index + 1);
        slots[index].setDouble(value);
        slots[index + 1].setDouble(value); // double占用两个slot
    }
    
    /**
     * 获取double类型局部变量
     */
    public double getDouble(int index) {
        checkIndex(index);
        checkIndex(index + 1);
        return slots[index].getDouble();
    }
    
    /**
     * 获取指定索引的Slot
     */
    public Slot getSlot(int index) {
        checkIndex(index);
        return slots[index];
    }
    
    /**
     * 检查索引是否有效
     */
    private void checkIndex(int index) {
        if (index < 0 || index >= maxLocals) {
            throw new IndexOutOfBoundsException(
                String.format("Local variable index %d out of bounds [0, %d)", index, maxLocals));
        }
    }
    
    /**
     * 获取最大局部变量数量
     */
    public int getMaxLocals() {
        return maxLocals;
    }
    
    /**
     * 清空所有局部变量
     */
    public void clear() {
        for (int i = 0; i < maxLocals; i++) {
            slots[i] = new Slot();
        }
    }
    
    /**
     * 调试输出
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LocalVariableTable[maxLocals=").append(maxLocals).append("]:\n");
        for (int i = 0; i < maxLocals; i++) {
            sb.append(String.format("  [%d] %s\n", i, slots[i]));
        }
        return sb.toString();
    }
} 