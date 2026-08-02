package dec.core.compiler.api;

/**
 * CompilationSession 的稳定状态机状态。
 */
public enum CompilationSessionState {
    CREATED,
    SOURCES_DISCOVERED,
    PARSED,
    RAW_BUILT,
    STRUCTURALLY_VALIDATED,
    SYMBOLS_REGISTERED,
    REFERENCES_RESOLVED,
    GRAPH_PREPARED,
    SEMANTICALLY_VALIDATED,
    PUBLISHED,
    FAILED
}
