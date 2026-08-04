package dec.core.compiler.modelaccess;

/**
 * ModelAccess 的结构化读写模式；P1 只记录事实，不执行权限判断。
 */
public enum AccessMode {
    READ,
    WRITE
}
