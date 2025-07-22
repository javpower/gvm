/**
 * 简单算术运算演示程序
 * 这个程序展示了JVM的核心功能：
 * 1. 局部变量操作
 * 2. 算术运算
 * 3. 静态方法调用
 * 4. 递归调用
 * 5. 栈帧管理
 * 6. 操作数栈操作
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
        staticCounter = staticCounter + 10;
        
        // 6. 创建对象和访问实例字段（展示堆内存分配）
        SimpleArithmetic obj = new SimpleArithmetic();
        int instanceVal = obj.getInstanceValue();
        
        // 7. 最终计算
        int finalResult = sum + diff + product + result1 + result2 + complexResult + factorial5 + staticCounter + instanceVal;
        
        System.out.println("计算完成，最终结果: " + finalResult);
    }
    
    /**
     * 加法运算
     */
    public static int add(int x, int y) {
        int temp = x + y;  // 局部变量
        return temp;
    }
    
    /**
     * 乘法运算
     */
    public static int multiply(int x, int y) {
        int temp1 = x;
        int temp2 = y;
        int result = temp1 * temp2;
        return result;
    }
    
    /**
     * 复杂计算（展示更多局部变量操作）
     */
    public static int calculate(int param1, int param2) {
        int local1 = param1 + 10;
        int local2 = param2 - 5;
        int local3 = local1 * 2;
        int local4 = local2 + local3;
        int local5 = local4 / 2;
        return local5;
    }
    
    /**
     * 递归计算阶乘（展示递归调用和栈深度）
     */
    public static int factorial(int n) {
        if (n <= 1) {
            return 1;
        }
        return n * factorial(n - 1);
    }
    
    /**
     * 构造函数
     */
    public SimpleArithmetic() {
        this.instanceValue = 42;
    }
    
    /**
     * 获取实例字段值
     */
    public int getInstanceValue() {
        return this.instanceValue;
    }
    
    /**
     * 设置实例字段值
     */
    public void setInstanceValue(int value) {
        this.instanceValue = value;
    }
} 