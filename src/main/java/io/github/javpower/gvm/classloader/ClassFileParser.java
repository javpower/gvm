package io.github.javpower.gvm.classloader;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * 简化的Class文件解析器
 * 解析.class文件的字节码，提取类的元数据信息
 */
public class ClassFileParser {
    
    // Class文件魔数
    private static final int MAGIC = 0xCAFEBABE;
    
    // 常量池标签
    private static final int CONSTANT_Utf8 = 1;
    private static final int CONSTANT_Integer = 3;
    private static final int CONSTANT_Float = 4;
    private static final int CONSTANT_Long = 5;
    private static final int CONSTANT_Double = 6;
    private static final int CONSTANT_Class = 7;
    private static final int CONSTANT_String = 8;
    private static final int CONSTANT_Fieldref = 9;
    private static final int CONSTANT_Methodref = 10;
    private static final int CONSTANT_InterfaceMethodref = 11;
    private static final int CONSTANT_NameAndType = 12;
    
    /**
     * 解析Class文件
     * 
     * @param classData 类文件的字节码数据
     * @param classLoader 类加载器
     * @return 解析后的GVMClass对象
     */
    public GVMClass parse(byte[] classData, GVMClassLoader classLoader) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(classData));
        
        try {
            // 1. 读取魔数
            int magic = dis.readInt();
            if (magic != MAGIC) {
                throw new IOException("Invalid class file magic number: 0x" + Integer.toHexString(magic));
            }
            
            // 2. 读取版本号
            int minorVersion = dis.readUnsignedShort();
            int majorVersion = dis.readUnsignedShort();
            
            // 3. 读取常量池
            Object[] constantPool = parseConstantPool(dis);
            
            // 4. 读取访问标志
            int accessFlags = dis.readUnsignedShort();
            
            // 5. 读取类索引
            int thisClass = dis.readUnsignedShort();
            String className = getClassName(constantPool, thisClass);
            
            // 6. 读取父类索引
            int superClass = dis.readUnsignedShort();
            String superClassName = superClass == 0 ? null : getClassName(constantPool, superClass);
            
            // 7. 读取接口
            String[] interfaces = parseInterfaces(dis, constantPool);
            
            // 8. 创建GVMClass对象
            GVMClass gvmClass = new GVMClass(className, superClassName, interfaces, 
                                           accessFlags, constantPool, classLoader);
            
            // 9. 解析字段
            parseFields(dis, constantPool, gvmClass);
            
            // 10. 解析方法
            parseMethods(dis, constantPool, gvmClass);
            
            // 11. 解析属性
            parseAttributes(dis, constantPool);
            
            return gvmClass;
            
        } finally {
            dis.close();
        }
    }
    
    /**
     * 解析常量池
     */
    private Object[] parseConstantPool(DataInputStream dis) throws IOException {
        int constantPoolCount = dis.readUnsignedShort();
        Object[] constantPool = new Object[constantPoolCount];
        
        for (int i = 1; i < constantPoolCount; i++) {
            int tag = dis.readUnsignedByte();
            
            switch (tag) {
                case CONSTANT_Utf8:
                    constantPool[i] = dis.readUTF();
                    break;
                    
                case CONSTANT_Integer:
                    constantPool[i] = dis.readInt();
                    break;
                    
                case CONSTANT_Float:
                    constantPool[i] = dis.readFloat();
                    break;
                    
                case CONSTANT_Long:
                    constantPool[i] = dis.readLong();
                    i++; // Long占用两个常量池位置
                    break;
                    
                case CONSTANT_Double:
                    constantPool[i] = dis.readDouble();
                    i++; // Double占用两个常量池位置
                    break;
                    
                case CONSTANT_Class:
                    constantPool[i] = new ClassInfo(dis.readUnsignedShort());
                    break;
                    
                case CONSTANT_String:
                    constantPool[i] = new StringInfo(dis.readUnsignedShort());
                    break;
                    
                case CONSTANT_Fieldref:
                    constantPool[i] = new FieldrefInfo(dis.readUnsignedShort(), dis.readUnsignedShort());
                    break;
                    
                case CONSTANT_Methodref:
                    constantPool[i] = new MethodrefInfo(dis.readUnsignedShort(), dis.readUnsignedShort());
                    break;
                    
                case CONSTANT_InterfaceMethodref:
                    constantPool[i] = new InterfaceMethodrefInfo(dis.readUnsignedShort(), dis.readUnsignedShort());
                    break;
                    
                case CONSTANT_NameAndType:
                    constantPool[i] = new NameAndTypeInfo(dis.readUnsignedShort(), dis.readUnsignedShort());
                    break;
                    
                default:
                    throw new IOException("Unknown constant pool tag: " + tag);
            }
        }
        
        return constantPool;
    }
    
    /**
     * 解析接口
     */
    private String[] parseInterfaces(DataInputStream dis, Object[] constantPool) throws IOException {
        int interfaceCount = dis.readUnsignedShort();
        String[] interfaces = new String[interfaceCount];
        
        for (int i = 0; i < interfaceCount; i++) {
            int interfaceIndex = dis.readUnsignedShort();
            interfaces[i] = getClassName(constantPool, interfaceIndex);
        }
        
        return interfaces;
    }
    
    /**
     * 解析字段
     */
    private void parseFields(DataInputStream dis, Object[] constantPool, GVMClass gvmClass) throws IOException {
        int fieldCount = dis.readUnsignedShort();
        
        for (int i = 0; i < fieldCount; i++) {
            int accessFlags = dis.readUnsignedShort();
            int nameIndex = dis.readUnsignedShort();
            int descriptorIndex = dis.readUnsignedShort();
            
            String name = (String) constantPool[nameIndex];
            String descriptor = (String) constantPool[descriptorIndex];
            
            // 解析字段属性
            int attributeCount = dis.readUnsignedShort();
            Object defaultValue = null;
            
            for (int j = 0; j < attributeCount; j++) {
                int attributeNameIndex = dis.readUnsignedShort();
                int attributeLength = dis.readInt();
                String attributeName = (String) constantPool[attributeNameIndex];
                
                if ("ConstantValue".equals(attributeName)) {
                    int constantValueIndex = dis.readUnsignedShort();
                    defaultValue = constantPool[constantValueIndex];
                } else {
                    // 跳过其他属性
                    dis.skipBytes(attributeLength);
                }
            }
            
            GVMClass.FieldInfo fieldInfo = new GVMClass.FieldInfo(name, descriptor, accessFlags, defaultValue);
            gvmClass.addField(fieldInfo);
        }
    }
    
    /**
     * 解析方法
     */
    private void parseMethods(DataInputStream dis, Object[] constantPool, GVMClass gvmClass) throws IOException {
        int methodCount = dis.readUnsignedShort();
        
        for (int i = 0; i < methodCount; i++) {
            int accessFlags = dis.readUnsignedShort();
            int nameIndex = dis.readUnsignedShort();
            int descriptorIndex = dis.readUnsignedShort();
            
            String name = (String) constantPool[nameIndex];
            String descriptor = (String) constantPool[descriptorIndex];
            
            // 解析方法属性
            int attributeCount = dis.readUnsignedShort();
            byte[] bytecode = null;
            int maxStack = 0;
            int maxLocals = 0;
            
            for (int j = 0; j < attributeCount; j++) {
                int attributeNameIndex = dis.readUnsignedShort();
                int attributeLength = dis.readInt();
                String attributeName = (String) constantPool[attributeNameIndex];
                
                if ("Code".equals(attributeName)) {
                    maxStack = dis.readUnsignedShort();
                    maxLocals = dis.readUnsignedShort();
                    int codeLength = dis.readInt();
                    
                    bytecode = new byte[codeLength];
                    dis.readFully(bytecode);
                    
                    // 跳过异常表
                    int exceptionTableLength = dis.readUnsignedShort();
                    dis.skipBytes(exceptionTableLength * 8);
                    
                    // 跳过代码属性
                    int codeAttributeCount = dis.readUnsignedShort();
                    for (int k = 0; k < codeAttributeCount; k++) {
                        dis.readUnsignedShort(); // attribute_name_index
                        int codeAttributeLength = dis.readInt();
                        dis.skipBytes(codeAttributeLength);
                    }
                } else {
                    // 跳过其他属性
                    dis.skipBytes(attributeLength);
                }
            }
            
            GVMClass.MethodInfo methodInfo = new GVMClass.MethodInfo(name, descriptor, accessFlags, 
                                                                    bytecode, maxStack, maxLocals);
            gvmClass.addMethod(methodInfo);
        }
    }
    
    /**
     * 解析属性
     */
    private void parseAttributes(DataInputStream dis, Object[] constantPool) throws IOException {
        int attributeCount = dis.readUnsignedShort();
        
        for (int i = 0; i < attributeCount; i++) {
            int attributeNameIndex = dis.readUnsignedShort();
            int attributeLength = dis.readInt();
            
            // 跳过所有类级别的属性
            dis.skipBytes(attributeLength);
        }
    }
    
    /**
     * 获取类名
     */
    private String getClassName(Object[] constantPool, int classIndex) {
        ClassInfo classInfo = (ClassInfo) constantPool[classIndex];
        return (String) constantPool[classInfo.nameIndex];
    }
    
    // 常量池信息类
    private static class ClassInfo {
        final int nameIndex;
        ClassInfo(int nameIndex) { this.nameIndex = nameIndex; }
    }
    
    private static class StringInfo {
        final int stringIndex;
        StringInfo(int stringIndex) { this.stringIndex = stringIndex; }
    }
    
    private static class FieldrefInfo {
        final int classIndex;
        final int nameAndTypeIndex;
        FieldrefInfo(int classIndex, int nameAndTypeIndex) {
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }
    
    private static class MethodrefInfo {
        final int classIndex;
        final int nameAndTypeIndex;
        MethodrefInfo(int classIndex, int nameAndTypeIndex) {
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }
    
    private static class InterfaceMethodrefInfo {
        final int classIndex;
        final int nameAndTypeIndex;
        InterfaceMethodrefInfo(int classIndex, int nameAndTypeIndex) {
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }
    
    private static class NameAndTypeInfo {
        final int nameIndex;
        final int descriptorIndex;
        NameAndTypeInfo(int nameIndex, int descriptorIndex) {
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
        }
    }
} 