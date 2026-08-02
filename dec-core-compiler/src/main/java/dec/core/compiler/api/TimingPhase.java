package dec.core.compiler.api;

/**
 * CompilationObserver 可观察的稳定计时阶段。
 */
public enum TimingPhase {
    DISCOVERY,
    PARSE,
    PASS,
    DIGEST
}
