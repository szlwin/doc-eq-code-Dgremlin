package dec.core.compiler.modelaccess;

/**
 * TASK-P1-T10 / I002 WRITE 路径重叠索引 Architecture seam。
 *
 * <p>本阶段只冻结按路径段工作的局部索引入口和资源计数接口，真实祖先、后代、
 * 重复与通配冲突语义在 Development 阶段实现。</p>
 */
final class WritePathOverlapIndex {
    private int operationCount;

    /**
     * 接收一个 WRITE 路径并返回是否与已登记路径重叠。
     *
     * <p>Architecture Skeleton 只记录常数次结构操作，暂不判断业务重叠，
     * 以保持 Review Oracle 的受控 RED。</p>
     */
    boolean add(SharedModelPath path) {
        if (path == null) {
            throw new NullPointerException("path");
        }
        operationCount++;
        return false;
    }

    /** 返回本次局部 compilation 已执行的结构查询次数。 */
    int operationCount() {
        return operationCount;
    }
}
