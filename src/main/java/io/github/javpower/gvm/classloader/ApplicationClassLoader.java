package io.github.javpower.gvm.classloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 应用程序类加载器 - 负责加载classpath中的应用程序类
 */
public class ApplicationClassLoader extends GVMClassLoader {
    
    // 类路径列表
    private final List<String> classPaths;
    
    /**
     * 构造应用程序类加载器
     * 
     * @param parent 父类加载器
     * @param classPaths 类路径列表
     */
    public ApplicationClassLoader(GVMClassLoader parent, List<String> classPaths) {
        super("Application ClassLoader", parent);
        this.classPaths = new ArrayList<>(classPaths != null ? classPaths : new ArrayList<>());
        
        // 如果没有指定classpath，使用当前目录
        if (this.classPaths.isEmpty()) {
            this.classPaths.add(".");
        }
    }
    
    /**
     * 构造应用程序类加载器 - 使用系统classpath
     */
    public ApplicationClassLoader(GVMClassLoader parent) {
        this(parent, parseSystemClassPath());
    }
    
    /**
     * 解析系统classpath
     */
    private static List<String> parseSystemClassPath() {
        List<String> paths = new ArrayList<>();
        
        // 获取java.class.path系统属性
        String classPath = System.getProperty("java.class.path");
        if (classPath != null && !classPath.isEmpty()) {
            String[] pathArray = classPath.split(File.pathSeparator);
            for (String path : pathArray) {
                if (!path.trim().isEmpty()) {
                    paths.add(path.trim());
                }
            }
        }
        
        // 如果没有classpath，添加当前目录
        if (paths.isEmpty()) {
            paths.add(".");
        }
        
        return paths;
    }
    
    @Override
    protected GVMClass findClass(String className) throws Exception {
        // 尝试从类路径中加载类
        byte[] classData = getClassData(className);
        if (classData != null) {
            return defineClass(className, classData);
        }
        return null;
    }
    
    @Override
    protected byte[] getClassData(String className) throws IOException {
        // 将类名转换为文件路径
        String classFile = className.replace('.', '/') + ".class";
        
        // 在每个类路径中查找类文件
        for (String classPath : classPaths) {
            File file = new File(classPath, classFile);
            
            if (file.exists() && file.isFile()) {
                return readClassFile(file);
            }
            
            // 如果classPath是jar文件，这里应该从jar中读取
            // 为了简化，暂时只支持目录形式的classpath
        }
        
        return null;
    }
    
    /**
     * 读取类文件
     */
    private byte[] readClassFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            
            return baos.toByteArray();
        }
    }
    
    /**
     * 添加类路径
     */
    public void addClassPath(String classPath) {
        if (classPath != null && !classPath.trim().isEmpty()) {
            classPaths.add(classPath.trim());
        }
    }
    
    /**
     * 移除类路径
     */
    public void removeClassPath(String classPath) {
        classPaths.remove(classPath);
    }
    
    /**
     * 获取所有类路径
     */
    public List<String> getClassPaths() {
        return new ArrayList<>(classPaths);
    }
    
    /**
     * 检查类路径是否存在
     */
    public boolean hasClassPath(String classPath) {
        return classPaths.contains(classPath);
    }
    
    /**
     * 清空类路径
     */
    public void clearClassPaths() {
        classPaths.clear();
    }
    
    /**
     * 获取类路径数量
     */
    public int getClassPathCount() {
        return classPaths.size();
    }
    
    /**
     * 检查指定的类是否存在于类路径中
     */
    public boolean classExists(String className) {
        try {
            return getClassData(className) != null;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 获取指定类路径中的所有类文件
     */
    public List<String> listClasses(String classPath) {
        List<String> classes = new ArrayList<>();
        File dir = new File(classPath);
        
        if (dir.exists() && dir.isDirectory()) {
            listClassesRecursive(dir, "", classes);
        }
        
        return classes;
    }
    
    /**
     * 递归列出目录中的所有类文件
     */
    private void listClassesRecursive(File dir, String packageName, List<String> classes) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                String subPackage = packageName.isEmpty() ? file.getName() : packageName + "." + file.getName();
                listClassesRecursive(file, subPackage, classes);
            } else if (file.getName().endsWith(".class")) {
                String className = file.getName().substring(0, file.getName().length() - 6);
                String fullClassName = packageName.isEmpty() ? className : packageName + "." + className;
                classes.add(fullClassName);
            }
        }
    }
    
    /**
     * 获取详细信息
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Application ClassLoader ===\n");
        sb.append("Name: ").append(getName()).append("\n");
        sb.append("Parent: ").append(getParent() != null ? getParent().getName() : "null").append("\n");
        sb.append("Loaded Classes: ").append(getLoadedClassCount()).append("\n");
        sb.append("Class Paths (").append(classPaths.size()).append("):\n");
        
        for (int i = 0; i < classPaths.size(); i++) {
            String path = classPaths.get(i);
            File file = new File(path);
            String status = file.exists() ? (file.isDirectory() ? "DIR" : "FILE") : "MISSING";
            sb.append(String.format("  [%d] %s (%s)\n", i, path, status));
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
        
        sb.append("==============================\n");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("ApplicationClassLoader[paths=%d, classes=%d]", 
            classPaths.size(), getLoadedClassCount());
    }
} 