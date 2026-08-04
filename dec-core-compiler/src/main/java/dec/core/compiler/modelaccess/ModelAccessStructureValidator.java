package dec.core.compiler.modelaccess;

import dec.core.compiler.raw.RawDefinition;
import dec.core.context.model.Diagnostic;
import java.util.Collections;
import java.util.List;

/**
 * TASK-P1-T10 / I002 ModelAccess 根结构验证 Architecture seam。
 *
 * <p>该对象只负责冻结“结构门禁先于 owner、selector、resolver 与发布”的职责边界；
 * Development 阶段再接入完整 root、attribute、scalar 与 child 合同。</p>
 */
final class ModelAccessStructureValidator {

    /**
     * 返回当前定义的结构 Diagnostic。
     *
     * <p>Architecture Skeleton 暂不改变现有业务行为，保持 malformed Oracle 受控 RED。</p>
     */
    List<Diagnostic> validate(RawDefinition definition) {
        if (definition == null) {
            throw new NullPointerException("definition");
        }
        return Collections.emptyList();
    }
}
