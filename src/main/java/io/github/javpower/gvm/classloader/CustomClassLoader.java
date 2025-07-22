package io.github.javpower.gvm.classloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义类加载器 - 支持用户自定义的类加载逻辑
 * 可以用于热部署、加密类文件、网络类加载等场景
 */
public class CustomClassLoader extends GVMClassLoader {
    
    // 自定义类数据提供者
    private final Map<String, ClassDataProvider> classDataProviders;
    
    // 是否破坏双亲委派模型
    private final boolean breakParentDelegation;
    
    /**
     * 类数据提供者接口
     */
    public interface ClassDataProvider {
        /**
         * 获取类的字节码数据
         * 
         * @param className 类名
         * @return 类的字节码数据，如果没有找到返回null
         */
        byte[] getClassData(String className) throws IOException;
        
        /**
         * 检查是否可以提供指定类的数据
         * 
         * @param className 类名
         * @return 如果可以提供返回true
         */
        boolean canProvide(String className);
    }
    
    /**
     * 文件系统类数据提供者
     */
    public static class FileSystemProvider implements ClassDataProvider {
        private final String basePath;
        
        public FileSystemProvider(String basePath) {
            this.basePath = basePath;
        }
        
        @Override
        public byte[] getClassData(String className) throws IOException {
            String classFile = basePath + "/" + className.replace('.', '/') + ".class";
            try (InputStream is = getClass().getResourceAsStream(classFile)) {
                if (is == null) {
                    return null;
                }
                return readStream(is);
            }
        }
        
        @Override
        public boolean canProvide(String className) {
            String classFile = basePath + "/" + className.replace('.', '/') + ".class";
            return getClass().getResource(classFile) != null;
        }
        
        private byte[] readStream(InputStream is) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }
    
    /**
     * 网络类数据提供者
     */
    public static class NetworkProvider implements ClassDataProvider {
        private final String baseUrl;
        
        public NetworkProvider(String baseUrl) {
            this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        }
        
        @Override
        public byte[] getClassData(String className) throws IOException {
            String classUrl = baseUrl + className.replace('.', '/') + ".class";
            
            try {
                URL url = new URL(classUrl);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(10000);
                
                try (InputStream is = connection.getInputStream()) {
                    return readStream(is);
                }
            } catch (Exception e) {
                return null;
            }
        }
        
        @Override
        public boolean canProvide(String className) {
            try {
                return getClassData(className) != null;
            } catch (IOException e) {
                return false;
            }
        }
        
        private byte[] readStream(InputStream is) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }
    
    /**
     * 内存类数据提供者 - 用于动态生成的类
     */
    public static class MemoryProvider implements ClassDataProvider {
        private final Map<String, byte[]> classDataMap;
        
        public MemoryProvider() {
            this.classDataMap = new HashMap<>();
        }
        
        /**
         * 添加类数据
         */
        public void addClass(String className, byte[] classData) {
            classDataMap.put(className, classData.clone());
        }
        
        /**
         * 移除类数据
         */
        public void removeClass(String className) {
            classDataMap.remove(className);
        }
        
        @Override
        public byte[] getClassData(String className) throws IOException {
            byte[] data = classDataMap.get(className);
            return data != null ? data.clone() : null;
        }
        
        @Override
        public boolean canProvide(String className) {
            return classDataMap.containsKey(className);
        }
        
        public int getClassCount() {
            return classDataMap.size();
        }
    }
    
    /**
     * 构造自定义类加载器
     * 
     * @param name 类加载器名称
     * @param parent 父类加载器
     * @param breakParentDelegation 是否破坏双亲委派模型
     */
    public CustomClassLoader(String name, GVMClassLoader parent, boolean breakParentDelegation) {
        super(name, parent);
        this.classDataProviders = new HashMap<>();
        this.breakParentDelegation = breakParentDelegation;
    }
    
    /**
     * 构造自定义类加载器 - 默认遵循双亲委派模型
     */
    public CustomClassLoader(String name, GVMClassLoader parent) {
        this(name, parent, false);
    }
    
    /**
     * 重写类加载方法 - 支持破坏双亲委派模型
     */
    @Override
    public synchronized GVMClass loadClass(String className, boolean resolve) throws ClassNotFoundException {
        if (breakParentDelegation) {
            // 破坏双亲委派 - 先尝试自己加载
            GVMClass gvmClass = findLoadedClass(className);
            if (gvmClass != null) {
                if (resolve) {
                    resolveClass(gvmClass);
                }
                return gvmClass;
            }
            
            // 尝试使用自定义提供者加载
            try {
                gvmClass = findClass(className);
                if (gvmClass != null) {
                    if (resolve) {
                        resolveClass(gvmClass);
                    }
                    return gvmClass;
                }
            } catch (Exception e) {
                // 继续尝试父类加载器
            }
            
            // 最后尝试父类加载器
            if (getParent() != null) {
                return getParent().loadClass(className, resolve);
            }
            
            throw new ClassNotFoundException("Class not found: " + className);
        } else {
            // 遵循双亲委派模型
            return super.loadClass(className, resolve);
        }
    }
    
    @Override
    protected GVMClass findClass(String className) throws Exception {
        // 尝试所有注册的类数据提供者
        for (ClassDataProvider provider : classDataProviders.values()) {
            if (provider.canProvide(className)) {
                byte[] classData = provider.getClassData(className);
                if (classData != null) {
                    return defineClass(className, classData);
                }
            }
        }
        return null;
    }
    
    @Override
    protected byte[] getClassData(String className) throws IOException {
        for (ClassDataProvider provider : classDataProviders.values()) {
            if (provider.canProvide(className)) {
                byte[] classData = provider.getClassData(className);
                if (classData != null) {
                    return classData;
                }
            }
        }
        return null;
    }
    
    /**
     * 添加类数据提供者
     */
    public void addClassDataProvider(String name, ClassDataProvider provider) {
        classDataProviders.put(name, provider);
    }
    
    /**
     * 移除类数据提供者
     */
    public void removeClassDataProvider(String name) {
        classDataProviders.remove(name);
    }
    
    /**
     * 获取类数据提供者
     */
    public ClassDataProvider getClassDataProvider(String name) {
        return classDataProviders.get(name);
    }
    
    /**
     * 检查是否有指定的类数据提供者
     */
    public boolean hasClassDataProvider(String name) {
        return classDataProviders.containsKey(name);
    }
    
    /**
     * 获取所有类数据提供者名称
     */
    public String[] getClassDataProviderNames() {
        return classDataProviders.keySet().toArray(new String[0]);
    }
    
    /**
     * 清空所有类数据提供者
     */
    public void clearClassDataProviders() {
        classDataProviders.clear();
    }
    
    /**
     * 检查指定类是否可以被加载
     */
    public boolean canLoadClass(String className) {
        for (ClassDataProvider provider : classDataProviders.values()) {
            if (provider.canProvide(className)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 热部署 - 重新加载指定的类
     */
    public GVMClass hotDeploy(String className) throws ClassNotFoundException {
        // 清除缓存中的类
        clearCache();
        
        // 重新加载类
        return loadClass(className);
    }
    
    /**
     * 是否破坏双亲委派模型
     */
    public boolean isBreakParentDelegation() {
        return breakParentDelegation;
    }
    
    /**
     * 获取详细信息
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Custom ClassLoader ===\n");
        sb.append("Name: ").append(getName()).append("\n");
        sb.append("Parent: ").append(getParent() != null ? getParent().getName() : "null").append("\n");
        sb.append("Break Parent Delegation: ").append(breakParentDelegation).append("\n");
        sb.append("Loaded Classes: ").append(getLoadedClassCount()).append("\n");
        sb.append("Data Providers (").append(classDataProviders.size()).append("):\n");
        
        for (Map.Entry<String, ClassDataProvider> entry : classDataProviders.entrySet()) {
            sb.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue().getClass().getSimpleName()).append("\n");
        }
        
        sb.append("\nLoaded Classes:\n");
        String[] loadedClasses = getLoadedClassNames();
        if (loadedClasses.length == 0) {
            sb.append("  (none)\n");
        } else {
            for (String className : loadedClasses) {
                sb.append("  - ").append(className).append("\n");
            }
        }
        
        sb.append("==========================\n");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("CustomClassLoader[name=%s, providers=%d, classes=%d, breakDelegation=%s]", 
            getName(), classDataProviders.size(), getLoadedClassCount(), breakParentDelegation);
    }
} 