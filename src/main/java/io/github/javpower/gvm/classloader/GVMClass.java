package io.github.javpower.gvm.classloader;

import java.util.HashMap;
import java.util.Map;

/**
 * GVM类信息 - 存储加载后的类的元数据
 */
public class GVMClass {
    
    // 类的基本信息
    private final String className;
    private final String superClassName;
    private final String[] interfaceNames;
    
    // 访问标志
    private final int accessFlags;
    
    // 常量池
    private final Object[] constantPool;
    
    // 字段信息
    private final Map<String, FieldInfo> fields;
    
    // 方法信息
    private final Map<String, MethodInfo> methods;
    
    // 类加载器
    private final GVMClassLoader classLoader;
    
    // 类状态
    private ClassState state;
    
    // 静态变量存储
    private final Map<String, Object> staticFields;
    
    /**
     * 类状态枚举
     */
    public enum ClassState {
        LOADED,      // 已加载
        LINKED,      // 已链接
        INITIALIZED  // 已初始化
    }
    
    /**
     * 字段信息
     */
    public static class FieldInfo {
        private final String name;
        private final String descriptor;
        private final int accessFlags;
        private final Object defaultValue;
        
        public FieldInfo(String name, String descriptor, int accessFlags, Object defaultValue) {
            this.name = name;
            this.descriptor = descriptor;
            this.accessFlags = accessFlags;
            this.defaultValue = defaultValue;
        }
        
        public String getName() { return name; }
        public String getDescriptor() { return descriptor; }
        public int getAccessFlags() { return accessFlags; }
        public Object getDefaultValue() { return defaultValue; }
        
        public boolean isStatic() { return (accessFlags & 0x0008) != 0; }
        public boolean isPublic() { return (accessFlags & 0x0001) != 0; }
        public boolean isPrivate() { return (accessFlags & 0x0002) != 0; }
        public boolean isProtected() { return (accessFlags & 0x0004) != 0; }
        public boolean isFinal() { return (accessFlags & 0x0010) != 0; }
        
        @Override
        public String toString() {
            return String.format("Field[name=%s, descriptor=%s, flags=0x%04x]", name, descriptor, accessFlags);
        }
    }
    
    /**
     * 方法信息
     */
    public static class MethodInfo {
        private final String name;
        private final String descriptor;
        private final int accessFlags;
        private final byte[] bytecode;
        private final int maxStack;
        private final int maxLocals;
        
        public MethodInfo(String name, String descriptor, int accessFlags, byte[] bytecode, int maxStack, int maxLocals) {
            this.name = name;
            this.descriptor = descriptor;
            this.accessFlags = accessFlags;
            this.bytecode = bytecode;
            this.maxStack = maxStack;
            this.maxLocals = maxLocals;
        }
        
        public String getName() { return name; }
        public String getDescriptor() { return descriptor; }
        public int getAccessFlags() { return accessFlags; }
        public byte[] getBytecode() { return bytecode; }
        public int getMaxStack() { return maxStack; }
        public int getMaxLocals() { return maxLocals; }
        
        public boolean isStatic() { return (accessFlags & 0x0008) != 0; }
        public boolean isPublic() { return (accessFlags & 0x0001) != 0; }
        public boolean isPrivate() { return (accessFlags & 0x0002) != 0; }
        public boolean isProtected() { return (accessFlags & 0x0004) != 0; }
        public boolean isAbstract() { return (accessFlags & 0x0400) != 0; }
        public boolean isNative() { return (accessFlags & 0x0100) != 0; }
        
        @Override
        public String toString() {
            return String.format("Method[name=%s, descriptor=%s, flags=0x%04x, maxStack=%d, maxLocals=%d]", 
                name, descriptor, accessFlags, maxStack, maxLocals);
        }
    }
    
    /**
     * 构造GVM类
     */
    public GVMClass(String className, String superClassName, String[] interfaceNames, 
                   int accessFlags, Object[] constantPool, GVMClassLoader classLoader) {
        this.className = className;
        this.superClassName = superClassName;
        this.interfaceNames = interfaceNames != null ? interfaceNames.clone() : new String[0];
        this.accessFlags = accessFlags;
        this.constantPool = constantPool != null ? constantPool.clone() : new Object[0];
        this.classLoader = classLoader;
        this.fields = new HashMap<>();
        this.methods = new HashMap<>();
        this.staticFields = new HashMap<>();
        this.state = ClassState.LOADED;
    }
    
    /**
     * 添加字段
     */
    public void addField(FieldInfo field) {
        fields.put(field.getName(), field);
        
        // 如果是静态字段，初始化默认值
        if (field.isStatic()) {
            staticFields.put(field.getName(), field.getDefaultValue());
        }
    }
    
    /**
     * 添加方法
     */
    public void addMethod(MethodInfo method) {
        String key = method.getName() + method.getDescriptor();
        methods.put(key, method);
    }
    
    /**
     * 获取字段
     */
    public FieldInfo getField(String name) {
        return fields.get(name);
    }
    
    /**
     * 获取方法
     */
    public MethodInfo getMethod(String name, String descriptor) {
        return methods.get(name + descriptor);
    }
    
    /**
     * 获取静态字段值
     */
    public Object getStaticFieldValue(String name) {
        return staticFields.get(name);
    }
    
    /**
     * 设置静态字段值
     */
    public void setStaticFieldValue(String name, Object value) {
        if (fields.containsKey(name) && fields.get(name).isStatic()) {
            staticFields.put(name, value);
        } else {
            throw new RuntimeException("Static field not found: " + name);
        }
    }
    
    // Getters
    public String getClassName() { return className; }
    public String getSuperClassName() { return superClassName; }
    public String[] getInterfaceNames() { return interfaceNames.clone(); }
    public int getAccessFlags() { return accessFlags; }
    public Object[] getConstantPool() { return constantPool.clone(); }
    public GVMClassLoader getClassLoader() { return classLoader; }
    public ClassState getState() { return state; }
    
    /**
     * 设置类状态
     */
    public void setState(ClassState state) {
        this.state = state;
    }
    
    /**
     * 获取所有字段
     */
    public Map<String, FieldInfo> getFields() {
        return new HashMap<>(fields);
    }
    
    /**
     * 获取所有方法
     */
    public Map<String, MethodInfo> getMethods() {
        return new HashMap<>(methods);
    }
    
    /**
     * 检查访问标志
     */
    public boolean isPublic() { return (accessFlags & 0x0001) != 0; }
    public boolean isInterface() { return (accessFlags & 0x0200) != 0; }
    public boolean isAbstract() { return (accessFlags & 0x0400) != 0; }
    public boolean isFinal() { return (accessFlags & 0x0010) != 0; }
    
    /**
     * 检查是否是指定类的子类
     */
    public boolean isSubclassOf(String className) {
        if (this.className.equals(className)) {
            return true;
        }
        
        String current = this.superClassName;
        while (current != null && !current.equals("java/lang/Object")) {
            if (current.equals(className)) {
                return true;
            }
            // 这里应该递归查找父类，简化处理
            break;
        }
        
        return false;
    }
    
    /**
     * 检查是否实现了指定接口
     */
    public boolean implementsInterface(String interfaceName) {
        for (String iface : interfaceNames) {
            if (iface.equals(interfaceName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取类的简单名称
     */
    public String getSimpleName() {
        int lastSlash = className.lastIndexOf('/');
        return lastSlash >= 0 ? className.substring(lastSlash + 1) : className;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GVMClass[name=").append(className);
        sb.append(", super=").append(superClassName);
        sb.append(", state=").append(state);
        sb.append(", fields=").append(fields.size());
        sb.append(", methods=").append(methods.size());
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * 获取详细信息
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 类信息（方法区内容）===\n");
        sb.append("类名: ").append(className).append("\n");
        sb.append("父类: ").append(superClassName).append("\n");
        sb.append("接口: ");
        if (interfaceNames.length == 0) {
            sb.append("(无)");
        } else {
            sb.append(String.join(", ", interfaceNames));
        }
        sb.append("\n");
        sb.append("访问标志: 0x").append(Integer.toHexString(accessFlags)).append("\n");
        sb.append("状态: ").append(state).append("\n");
        sb.append("常量池大小: ").append(constantPool.length).append("\n");
        
        sb.append("\n--- 字段信息（存储在方法区）---\n");
        if (fields.isEmpty()) {
            sb.append("(无字段)\n");
        } else {
            for (FieldInfo field : fields.values()) {
                sb.append("  字段: ").append(field).append("\n");
            }
        }
        
        sb.append("\n--- 方法信息（存储在方法区）---\n");
        if (methods.isEmpty()) {
            sb.append("(无方法)\n");
        } else {
            for (MethodInfo method : methods.values()) {
                sb.append("  方法: ").append(method).append("\n");
                if (method.getBytecode() != null) {
                    sb.append("    字节码长度: ").append(method.getBytecode().length).append(" bytes\n");
                }
            }
        }
        
        sb.append("================================\n");
        return sb.toString();
    }
} 