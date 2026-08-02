package dec.core.compiler.api;

/**
 * 由调用方注入的编译计时与状态观察边界。
 */
public interface CompilationObserver {
    /**
     * 接收单次阶段计时；实现不得改变编译语义事实。
     */
    void onTiming(CompilationTiming timing);

    /**
     * 接收实际发生的 Session 状态转换。
     */
    void onStateTransition(SessionStateTransition transition);
}
