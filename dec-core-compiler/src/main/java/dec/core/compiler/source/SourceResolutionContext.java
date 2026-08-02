package dec.core.compiler.source;

import java.util.Optional;

/**
 * Provider 解析 Source 时可读取的会话级上下文。
 */
public interface SourceResolutionContext {
    /**
     * 返回本次 CompilationSession 的根 Source 引用。
     */
    SourceReference root();

    /**
     * 返回声明当前引用的父 Source 身份；根引用使用空值。
     */
    Optional<String> parentSourceId();

    /**
     * 返回当前引用相对根 Source 的非负深度。
     */
    int depth();
}
