package dec.core.compiler.pass;

/**
 * Compiler Pipeline 中一个可独立验证的固定阶段。
 */
public interface CompilerPass {
    /**
     * 返回 DESIGN-R38 冻结的稳定 Pass 名称。
     */
    String name();

    /**
     * 在当前 Session 的局部上下文中执行本阶段。
     *
     * @param context 当前 Session 专属上下文
     * @return 不以 null 或预期异常表达业务失败的不可变结果
     */
    PassResult execute(PassContext context);
}
