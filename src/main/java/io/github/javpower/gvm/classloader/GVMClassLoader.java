package io.github.javpower.gvm.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GVM类加载器抽象基类
 * 实现双亲委派模型和类加载的基本流程
 */
public abstract class GVMClassLoader {
    
    // 父类加载器
    private final GVMClassLoader parent;
    
    // 已加载的类缓存
    private final ConcurrentHashMap<String, GVMClass> loadedClasses;
    
    // 类加载器名称
    private final String name;
    
    /**
     * 构造类加载器
     * 
     * @param name 类加载器名称
     * @param parent 父类加载器
     */
    protected GVMClassLoader(String name, GVMClassLoader parent) {
        this.name = name;
        this.parent = parent;
        this.loadedClasses = new ConcurrentHashMap<>();
    }
    
    /**
     * 加载类 - 实现双亲委派模型
     * 
     * @param className 类名
     * @return 加载的类
     */
    public GVMClass loadClass(String className) throws ClassNotFoundException {
        return loadClass(className, false);
    }
    
    /**
     * 加载类
     * 
     * @param className 类名
     * @param resolve 是否解析类
     * @return 加载的类
     */
    public synchronized GVMClass loadClass(String className, boolean resolve) throws ClassNotFoundException {
        // 1. 检查类是否已经加载
        GVMClass gvmClass = findLoadedClass(className);
        if (gvmClass != null) {
            if (resolve) {
                resolveClass(gvmClass);
            }
            return gvmClass;
        }
        
        // 2. 双亲委派 - 先让父类加载器尝试加载
        if (parent != null) {
            try {
                gvmClass = parent.loadClass(className, false);
                if (resolve) {
                    resolveClass(gvmClass);
                }
                return gvmClass;
            } catch (ClassNotFoundException e) {
                // 父类加载器无法加载，继续使用当前加载器
            }
        }
        
        // 3. 使用当前类加载器查找并加载类
        try {
            gvmClass = findClass(className);
            if (gvmClass != null) {
                // 缓存加载的类
                loadedClasses.put(className, gvmClass);
                
                if (resolve) {
                    resolveClass(gvmClass);
                }
                
                return gvmClass;
            }
        } catch (Exception e) {
            throw new ClassNotFoundException("Could not load class: " + className, e);
        }
        
        throw new ClassNotFoundException("Class not found: " + className);
    }
    
    /**
     * 查找类 - 由子类实现具体的查找逻辑
     * 
     * @param className 类名
     * @return 找到的类，如果没找到返回null
     */
    protected abstract GVMClass findClass(String className) throws Exception;
    
    /**
     * 获取类的字节码数据 - 由子类实现
     * 
     * @param className 类名
     * @return 类的字节码数据
     */
    protected abstract byte[] getClassData(String className) throws IOException;
    
    /**
     * 从已加载的类中查找
     * 
     * @param className 类名
     * @return 已加载的类，如果没找到返回null
     */
    protected GVMClass findLoadedClass(String className) {
        return loadedClasses.get(className);
    }
    
    /**
     * 定义类 - 将字节码转换为类对象
     * 
     * @param className 类名
     * @param classData 类的字节码数据
     * @return 定义的类
     */
    protected GVMClass defineClass(String className, byte[] classData) {
        try {
            // 解析class文件
            ClassFileParser parser = new ClassFileParser();
            GVMClass gvmClass = parser.parse(classData, this);
            
            // 验证类
            if (!verifyClass(gvmClass)) {
                throw new RuntimeException("Class verification failed: " + className);
            }
            
            return gvmClass;
        } catch (Exception e) {
            throw new RuntimeException("Failed to define class: " + className, e);
        }
    }
    
    /**
     * 验证类
     * 
     * @param gvmClass 要验证的类
     * @return 验证是否通过
     */
    protected boolean verifyClass(GVMClass gvmClass) {
        // 简化的类验证逻辑
        try {
            // 检查类名是否有效
            if (gvmClass.getClassName() == null || gvmClass.getClassName().isEmpty()) {
                return false;
            }
            
            // 检查父类是否存在（除了Object类）
            String superClass = gvmClass.getSuperClassName();
            if (superClass != null && !superClass.equals("java/lang/Object")) {
                // 这里可以添加更多的父类验证逻辑
            }
            
            // 检查方法的字节码是否有效
            for (GVMClass.MethodInfo method : gvmClass.getMethods().values()) {
                if (method.getBytecode() != null && method.getBytecode().length == 0 && !method.isAbstract() && !method.isNative()) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Class verification error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 解析类 - 将符号引用转换为直接引用
     * 
     * @param gvmClass 要解析的类
     */
    protected void resolveClass(GVMClass gvmClass) {
        if (gvmClass.getState() == GVMClass.ClassState.LINKED || 
            gvmClass.getState() == GVMClass.ClassState.INITIALIZED) {
            return; // 已经解析过了
        }
        
        try {
            // 简化的解析过程
            // 在真实JVM中，这里会解析常量池中的符号引用
            
            gvmClass.setState(GVMClass.ClassState.LINKED);
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve class: " + gvmClass.getClassName(), e);
        }
    }
    
    /**
     * 初始化类 - 执行类的<clinit>方法
     * 
     * @param gvmClass 要初始化的类
     */
    public void initializeClass(GVMClass gvmClass) {
        if (gvmClass.getState() == GVMClass.ClassState.INITIALIZED) {
            return; // 已经初始化过了
        }
        
        // 确保类已经链接
        if (gvmClass.getState() != GVMClass.ClassState.LINKED) {
            resolveClass(gvmClass);
        }
        
        try {
            // 先初始化父类
            String superClassName = gvmClass.getSuperClassName();
            if (superClassName != null && !superClassName.equals("java/lang/Object")) {
                try {
                    GVMClass superClass = loadClass(superClassName);
                    initializeClass(superClass);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Cannot initialize super class: " + superClassName, e);
                }
            }
            
            // 执行类初始化方法 <clinit>
            GVMClass.MethodInfo clinit = gvmClass.getMethod("<clinit>", "()V");
            if (clinit != null) {
                // 这里应该执行<clinit>方法的字节码
                // 为了简化，我们只是标记为已初始化
                System.out.println("Executing <clinit> for class: " + gvmClass.getClassName());
            }
            
            gvmClass.setState(GVMClass.ClassState.INITIALIZED);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize class: " + gvmClass.getClassName(), e);
        }
    }
    
    /**
     * 获取父类加载器
     */
    public GVMClassLoader getParent() {
        return parent;
    }
    
    /**
     * 获取类加载器名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取已加载的类数量
     */
    public int getLoadedClassCount() {
        return loadedClasses.size();
    }
    
    /**
     * 获取已加载的所有类名
     */
    public String[] getLoadedClassNames() {
        return loadedClasses.keySet().toArray(new String[0]);
    }
    
    /**
     * 清除类加载器缓存
     */
    public void clearCache() {
        loadedClasses.clear();
    }
    
    @Override
    public String toString() {
        return String.format("%s[name=%s, parent=%s, loadedClasses=%d]", 
            getClass().getSimpleName(), name, 
            parent != null ? parent.getName() : "null", 
            loadedClasses.size());
    }
    
    /**
     * 获取类加载器层次结构信息
     */
    public String getHierarchy() {
        StringBuilder sb = new StringBuilder();
        GVMClassLoader current = this;
        int level = 0;
        
        while (current != null) {
            for (int i = 0; i < level; i++) {
                sb.append("  ");
            }
            sb.append("- ").append(current.getName()).append(" (").append(current.getLoadedClassCount()).append(" classes)\n");
            current = current.getParent();
            level++;
        }
        
        return sb.toString();
    }
} 