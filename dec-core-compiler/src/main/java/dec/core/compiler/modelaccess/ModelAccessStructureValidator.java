package dec.core.compiler.modelaccess;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.context.model.Diagnostic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 在任何 owner、selector、resolver 或发布工作之前验证 ModelAccess Raw 结构。
 */
final class ModelAccessStructureValidator {
    private static final String MODEL_REF = "model-ref";
    private static final String PATH = "path";
    private static final String VIEW = "view";
    private static final String PROPERTY = "property";

    /**
     * 返回全部稳定结构 Diagnostic；空列表表示 definition 可进入后续语义阶段。
     */
    List<Diagnostic> validate(RawDefinition definition) {
        if (definition == null) {
            throw new NullPointerException("definition");
        }
        List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
        RawNodeBody body = definition.body();
        if (definition.kind() != RawDefinitionKind.MODEL_ACCESS
                || !"model-access".equals(body.name())
                || body.scalar().isPresent()
                || !hasExactKeys(definition.attributes(), MODEL_REF)
                || !definition.attributes().equals(body.attributes())
                || !hasTypedKeyReferenceLexical(
                        definition.attributes().get(MODEL_REF))
                || !definition.name().isPresent()
                || !definition.name().get().equals(
                        definition.attributes().get(MODEL_REF))) {
            diagnostics.add(ModelAccessDiagnostics.structureInvalid(
                    definition.sourceRef()));
            return Collections.unmodifiableList(diagnostics);
        }

        for (RawNodeBody access : body.children()) {
            validateAccess(access, diagnostics);
        }
        return Collections.unmodifiableList(diagnostics);
    }

    /** 验证 read/write 只携带 path，且其 children 只能是结构完整的 ref。 */
    private static void validateAccess(
            RawNodeBody access,
            List<Diagnostic> diagnostics) {
        if (!("read".equals(access.name()) || "write".equals(access.name()))
                || access.scalar().isPresent()
                || !hasExactKeys(access.attributes(), PATH)
                || !hasExactPathLexical(access.attributes().get(PATH))) {
            diagnostics.add(ModelAccessDiagnostics.structureInvalid(
                    access.sourceRef()));
            return;
        }
        for (RawNodeBody ref : access.children()) {
            validateRef(ref, diagnostics);
        }
    }

    /** 验证 ref 只携带 view/property，且不得包含 scalar 或 child。 */
    private static void validateRef(
            RawNodeBody ref,
            List<Diagnostic> diagnostics) {
        if (!"ref".equals(ref.name())
                || ref.scalar().isPresent()
                || !ref.children().isEmpty()
                || !hasExactKeys(ref.attributes(), VIEW, PROPERTY)
                || !hasTypedKeyReferenceLexical(ref.attributes().get(VIEW))
                || !hasExactPathLexical(ref.attributes().get(PROPERTY))) {
            diagnostics.add(ModelAccessDiagnostics.structureInvalid(
                    ref.sourceRef()));
        }
    }

    /** 判断属性集合是否与冻结的 key 集合完全一致。 */
    private static boolean hasExactKeys(
            Map<String, String> attributes,
            String... keys) {
        if (attributes.size() != keys.length) {
            return false;
        }
        for (String key : keys) {
            if (!attributes.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * TypedKey reference 只要求存在且 trim 后非空。
     *
     * <p>这里不改写 Raw lexical，也不要求输入预先 trim；后续统一由 ViewKey
     * 进行安全规范化，从而保持 T06 Raw 快照与 T07 TypedKey 的职责分离。</p>
     */
    private static boolean hasTypedKeyReferenceLexical(String lexical) {
        return lexical != null && !lexical.trim().isEmpty();
    }

    /** 精确 path/selector 必须存在、非空白且不依赖隐式 trim 修复。 */
    private static boolean hasExactPathLexical(String lexical) {
        return lexical != null
                && !lexical.trim().isEmpty()
                && lexical.equals(lexical.trim());
    }
}
