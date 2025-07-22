# GVM - Generic Virtual Machine

一个用Java实现的简化版JVM，用于学习和理解JVM的工作原理。通过详细的日志输出，让JVM的内部工作机制"看得见"！

## 项目概述

GVM（Generic Virtual Machine）是一个教育性质的JVM实现，旨在帮助开发者深入理解Java虚拟机的内部工作机制。通过实际的代码实现和详细的日志输出，您可以学习到：

- JVM的基本架构和组件
- 类加载机制和双亲委派模型
- 字节码逐条执行过程
- 内存管理和垃圾回收
- 方法调用和栈帧管理
- 异常处理机制

## 核心特性

### 详细的日志系统
GVM 提供了完整的分级日志系统，让您能够清楚地看到：

- **JVM初始化的每个步骤**
- **类加载的详细过程**
- **字节码指令的逐条执行**
- **堆、栈、方法区的内容变化**
- **内存分配和垃圾回收过程**
- **方法调用链和栈帧管理**

### 日志级别和分类
- **基本级别**: ERROR, WARN, INFO, DEBUG, TRACE
- **专门类别**: 
  - `[CLASS]` - 类加载相关（蓝色）
  - `[MEMORY]` - 内存管理相关（紫色）
  - `[BYTECODE]` - 字节码执行（亮黄色）
  - `[METHOD]` - 方法调用（亮青色）
  - `[EXCEPTION]` - 异常处理（亮红色）
  - `[STACK]` - 栈操作（亮蓝色）
  - `[GC]` - 垃圾回收（亮紫色）

## 🏗️ 核心架构

### 1. 类加载器系统
- **Bootstrap ClassLoader**: 引导类加载器
- **Extension ClassLoader**: 扩展类加载器  
- **Application ClassLoader**: 应用程序类加载器
- **Custom ClassLoader**: 自定义类加载器
- **双亲委派模型**: 实现类加载的安全性和唯一性

### 2. 运行时数据区
- **方法区**: 存储类元数据、常量池、静态变量
- **堆内存**: 存储对象实例，分为年轻代和老年代
- **JVM栈**: 每个线程独有，存储栈帧
- **PC寄存器**: 程序计数器，记录当前执行的字节码指令
- **本地方法栈**: 支持native方法调用

### 3. 执行引擎
- **字节码解释器**: 逐条解释执行字节码指令
- **方法调用器**: 处理各种方法调用类型
- **异常处理器**: 异常的创建、传播和处理
- **垃圾收集器**: 自动内存管理

### 4. 内存管理
- **对象分配**: 在堆中分配对象内存
- **引用计数**: 跟踪对象引用
- **分代收集**: 年轻代和老年代的分别管理
- **标记清除**: 简化的垃圾回收算法

##  快速开始

### 环境要求
- JDK 1.8 或更高版本
- Maven 3.6 或更高版本

### 构建项目
```bash
# 克隆项目
git clone https://github.com/javpower/gvm
cd gvm

# 编译项目
mvn compile

# 运行测试
mvn test
```

### 运行示例

#### 1. 基本模式（INFO级别日志）
```bash
# 编译示例程序
javac examples/SimpleArithmetic.java

# 运行GVM
java -cp target/classes:examples io.github.javpower.gvm.GVM  examples SimpleArithmetic
```

#### 2. 调试模式（TRACE级别日志）
```bash
# 运行调试模式，查看详细的JVM工作过程
java -cp target/classes:examples io.github.javpower.gvm.GVM -debug -cp examples SimpleArithmetic
```

## 📊 日志输出示例

### JVM初始化过程
```
[GVM] ======================== GVM 初始化开始 =========================
[GVM] 10:10:59 [INFO] 正在初始化 GVM (Generic Virtual Machine) v1.0.0
[GVM] 10:10:59 [DEBUG] 调试模式: 开启
[GVM] 10:10:59 [DEBUG] 执行模式: INTERPRET
[GVM] 10:10:59 [DEBUG] 栈大小: 1000
[GVM] --- 步骤1: 初始化类加载器体系 ---
[GVM] [CLASS] 正在初始化类加载器层次结构...
[GVM] [CLASS] 创建系统类加载器: ApplicationClassLoader
[GVM] [CLASS] 类加载器体系初始化完成
```

### 类加载详情（方法区内容）
```
[GVM] [CLASS] 正在加载主类: SimpleArithmetic
=== 类信息（方法区内容）===
类名: SimpleArithmetic
父类: java/lang/Object
接口: (无)
访问标志: 0x21
状态: LOADED
常量池大小: 19

--- 字段信息（存储在方法区）---
  字段: staticCounter (I) static
  字段: instanceValue (I) private

--- 方法信息（存储在方法区）---
  方法: main ([Ljava/lang/String;)V static
    字节码长度: 45 bytes
  方法: add (II)I static
    字节码长度: 8 bytes
```

### 字节码执行过程（栈操作详情）
```
[GVM] [BYTECODE] PC=  0: bipush 10
[GVM] [STACK] 操作数栈: 0/2 个元素
[GVM] 10:10:59 [TRACE] 栈内容: []
[GVM] 10:10:59 [TRACE] 局部变量: [slot0=null, slot1=null]
[GVM] 10:10:59 [TRACE] 下一个PC: 2

[GVM] [BYTECODE] PC=  2: istore_1
[GVM] [STACK] 操作数栈: 1/2 个元素
[GVM] 10:10:59 [TRACE] 栈内容: [10]
[GVM] 10:10:59 [TRACE] 局部变量: [slot0=null, slot1=null]
[GVM] 10:10:59 [TRACE] 下一个PC: 3
```

### 内存管理详情（堆内容）
```
[GVM] [MEMORY] 分配对象: ID=1, 类=SimpleArithmetic, 大小=64 bytes, 总对象数=1
[GVM] 10:10:59 [DEBUG] 堆内存状态: 已用=64 bytes, 对象=1个 (年轻代=1, 老年代=0)

=== 堆内存详情 ===
最大大小: 256 MB (268435456 bytes)
当前使用: 64 bytes (0.00%)
对象总数: 1
年轻代: 1 个对象
老年代: 0 个对象
GC次数: 0

--- 堆中的对象 ---
  对象ID=1, 类=SimpleArithmetic, 状态=ALIVE, 大小=64 bytes, 引用数=1
================
```

### 垃圾回收过程
```
[GVM] [GC] 开始垃圾回收...
[GVM] [GC] GC前: 内存=1024 bytes, 对象=5个, 年轻代=3个, 老年代=2个
[GVM] [GC] 阶段1: 标记可达对象
[GVM] [GC] 阶段2: 清除不可达对象
[GVM] [GC] 阶段3: 晋升长期存活对象
[GVM] [GC] 阶段4: 重置对象状态
[GVM] [GC] 垃圾回收完成: 耗时15ms
[GVM] [GC] GC后: 内存=512 bytes (释放512), 对象=3个 (回收2), 年轻代=1个, 老年代=2个
```

##  示例程序

项目提供了一个完整的示例程序 `SimpleArithmetic.java`，展示了JVM的各种特性：

```java
/**
 * 简单算术运算演示程序
 * 展示了JVM的核心功能：
 * 1. 局部变量操作
 * 2. 算术运算
 * 3. 静态方法调用
 * 4. 递归调用
 * 5. 栈帧管理
 * 6. 对象创建和字段访问
 */
public class SimpleArithmetic {
    
    // 静态字段（存储在方法区）
    public static int staticCounter = 0;
    
    // 实例字段（存储在堆中的对象里）
    private int instanceValue = 42;
    
    public static void main(String[] args) {
        System.out.println("=== SimpleArithmetic 演示程序 ===");
        
        // 1. 基本算术运算（展示局部变量和操作数栈）
        int a = 10;
        int b = 5;
        int sum = a + b;        // 加法
        int diff = a - b;       // 减法
        int product = a * b;    // 乘法
        
        // 2. 静态方法调用（展示方法调用和栈帧）
        int result1 = add(a, b);
        int result2 = multiply(a, b);
        
        // 3. 复杂计算（展示更多局部变量）
        int complexResult = calculate(sum, product);
        
        // 4. 递归调用（展示栈深度变化）
        int factorial5 = factorial(5);
        
        // 5. 静态字段访问（展示方法区访问）
        staticCounter++;
        
        // 6. 创建对象和访问实例字段（展示堆内存分配）
        SimpleArithmetic obj = new SimpleArithmetic();
        int instanceVal = obj.getInstanceValue();
        
        System.out.println("计算完成，最终结果: " + 
            (sum + diff + product + result1 + result2 + complexResult + factorial5 + staticCounter + instanceVal));
    }
    
    public static int add(int x, int y) {
        return x + y;
    }
    
    public static int multiply(int x, int y) {
        return x * y;
    }
    
    public static int calculate(int param1, int param2) {
        int local1 = param1 + 10;
        int local2 = param2 - 5;
        int local3 = local1 * 2;
        int local4 = local2 + local3;
        int local5 = local4 / 2;
        return local5;
    }
    
    public static int factorial(int n) {
        if (n <= 1) {
            return 1;
        }
        return n * factorial(n - 1);
    }
    
    public int getInstanceValue() {
        return this.instanceValue;
    }
}
```

##  学习价值

通过GVM的详细日志，您可以：

1. **"看见"JVM的工作过程**：从初始化到程序执行的每个步骤都清晰可见
2. **理解字节码执行**：逐条指令的执行过程和栈状态变化
3. **掌握类加载机制**：双亲委派模型的实际运行过程
4. **学习内存管理**：对象分配、垃圾回收的详细过程
5. **理解方法调用**：栈帧创建、参数传递、返回值处理
6. **掌握异常处理**：异常的创建、传播、处理流程

### 内存区域详细展示

- **堆内存**：显示对象分配、引用计数、分代管理
- **JVM栈**：显示栈帧内容、局部变量表、操作数栈
- **方法区**：显示类信息、字段、方法、常量池

## 🛠️ 支持的字节码指令

### 常量指令
- `iconst_<i>`, `bipush`, `sipush`, `ldc`

### 加载/存储指令
- `iload_<n>`, `istore_<n>`, `aload_<n>`, `astore_<n>`

### 算术指令
- `iadd`, `isub`, `imul`, `idiv`

### 方法调用指令
- `invokestatic`, `invokevirtual`, `invokespecial`, `invokeinterface`

### 返回指令
- `return`, `ireturn`, `areturn`

##  命令行选项

```bash
java -cp target/classes:examples io.github.javpower.gvm.GVM [options] <main-class> [args...]

Options:
  -debug                   启用调试模式（TRACE级别日志）
  -cp, -classpath <paths>  类搜索路径
  -mode <mode>             执行模式 (INTERPRET, JIT, MIXED)
  -stack <size>            栈大小 (默认: 1000)
```

### 使用示例
```bash
# 基本运行
java -cp target/classes:examples io.github.javpower.gvm.GVM SimpleArithmetic

# 调试模式，查看详细日志
java -cp target/classes:examples io.github.javpower.gvm.GVM -debug SimpleArithmetic

# 指定栈大小
java -cp target/classes:examples io.github.javpower.gvm.GVM -debug -stack 500 SimpleArithmetic
```

## 🎯 项目架构

```
gvm/
├── src/main/java/io/github/javpower/gvm/
│   ├── classloader/          # 类加载器
│   │   ├── GVMClass.java            # 类信息
│   │   ├── GVMClassLoader.java      # 类加载器基类
│   │   ├── ApplicationClassLoader.java  # 应用程序类加载器
│   │   ├── CustomClassLoader.java   # 自定义类加载器
│   │   └── ClassFileParser.java     # Class文件解析器
│   ├── runtime/              # 运行时数据区
│   │   └── stack/
│   │       ├── LocalVariableTable.java  # 局部变量表
│   │       ├── OperandStack.java         # 操作数栈
│   │       ├── StackFrame.java           # 栈帧
│   │       └── JVMStack.java             # JVM栈
│   ├── bytecode/            # 字节码处理
│   │   ├── Instruction.java          # 指令基类
│   │   ├── InstructionSet.java       # 指令集管理
│   │   └── instructions/             # 具体指令实现
│   ├── execution/           # 执行引擎
│   │   ├── ExecutionEngine.java      # 执行引擎
│   │   ├── MethodInvoker.java        # 方法调用器
│   │   └── ExceptionHandler.java     # 异常处理器
│   ├── memory/              # 内存管理
│   │   ├── Heap.java                 # 堆内存
│   │   └── GVMObject.java            # 对象表示
│   ├── utils/               # 工具类
│   │   └── Logger.java               # 日志系统
│   └── GVM.java            # 主入口
├── examples/               # 示例程序
│   └── SimpleArithmetic.java
└── test/                  # 测试用例
    └── java/io/github/javpower/gvm/
        └── GVMTest.java
```

## 🎯 扩展功能

- [x] 基础字节码指令集
- [x] 类加载器体系
- [x] 运行时数据区
- [x] 详细日志系统
- [x] 内存管理和垃圾回收
- [x] 方法调用机制
- [x] 异常处理系统
- [x] 堆、栈、方法区内容展示
- [ ] JIT编译器
- [ ] 多线程支持
- [ ] 同步机制
- [ ] 反射机制

## 🤝 贡献指南

欢迎贡献代码、报告问题或提出改进建议：

1. Fork 项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件。

---

**注意**: 这是一个教育项目，不适合生产环境使用。它的目的是帮助理解JVM的工作原理，通过"看得见"的方式学习JVM内部机制！ 