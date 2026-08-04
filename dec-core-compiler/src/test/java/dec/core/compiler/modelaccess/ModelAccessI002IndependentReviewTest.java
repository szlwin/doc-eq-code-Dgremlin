package dec.core.compiler.modelaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.compiler.symbol.SymbolBuildResult;
import dec.core.compiler.symbol.SymbolBuildStatus;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.compiler.symbol.SymbolTableBuilder;
import dec.core.context.model.Diagnostic;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T10 / I002 独立 Review：补齐未被首轮 RED 显式覆盖的结构与状态边界。
 */
class ModelAccessI002IndependentReviewTest {

    /** blank model-ref 必须在 resolver 前以结构错误阻断。 */
    @Test
    void rejectsBlankModelRefBeforeResolver() {
        assertRejected(replaceModelAccess(definition -> {
            Map<String, String> attributes = attributes("model-ref", "   ");
            RawNodeBody body = copyBody(
                    definition.body(),
                    attributes,
                    definition.body().children());
            return copyDefinition(definition, attributes, body);
        }));
    }

    /** root 出现冻结 grammar 之外的额外属性必须阻断。 */
    @Test
    void rejectsExtraRootAttributeBeforeResolver() {
        assertRejected(replaceModelAccess(definition -> {
            Map<String, String> attributes = new LinkedHashMap<String, String>(
                    definition.attributes());
            attributes.put("extra", "x");
            RawNodeBody body = copyBody(
                    definition.body(),
                    attributes,
                    definition.body().children());
            return copyDefinition(definition, attributes, body);
        }));
    }

    /** read/write child 不是 ref 时必须阻断。 */
    @Test
    void rejectsIllegalAccessChildBeforeResolver() {
        assertRejected(replaceModelAccess(definition -> {
            RawNodeBody root = definition.body();
            List<RawNodeBody> accesses = new ArrayList<RawNodeBody>(
                    root.children());
            RawNodeBody access = accesses.get(0);
            RawNodeBody illegal = new RawNodeBody(
                    "illegal",
                    Collections.<String, String>emptyMap(),
                    Optional.<String>empty(),
                    Collections.<RawNodeBody>emptyList(),
                    access.sourceRef());
            accesses.set(0, copyBody(
                    access,
                    access.attributes(),
                    Collections.singletonList(illegal)));
            RawNodeBody changedRoot = copyBody(
                    root,
                    root.attributes(),
                    accesses);
            return copyDefinition(
                    definition,
                    definition.attributes(),
                    changedRoot);
        }));
    }

    /** 结构验证器与 trie 不得引入跨 compilation 的静态可变状态。 */
    @Test
    void keepsReworkHelpersFreeOfStaticMutableState() {
        Class<?>[] types = {
            ModelAccessStructureValidator.class,
            WritePathOverlapIndex.class
        };
        for (Class<?> type : types) {
            for (Field field : type.getDeclaredFields()) {
                if (field.isSynthetic() || field.getName().startsWith("$jacoco")) {
                    continue;
                }
                if (Modifier.isStatic(field.getModifiers())) {
                    assertTrue(Modifier.isFinal(field.getModifiers()),
                            type.getSimpleName() + "." + field.getName());
                }
            }
        }
    }

    /** 断言结构失败不调用 resolver，也不发布 Binding 或 Deferred。 */
    private static void assertRejected(RawDefinitionSet definitions) {
        AtomicInteger calls = new AtomicInteger();
        ModelAccessSelectorResolver resolver = (owner, sourcePath, targetView,
                selector, symbols) -> {
            calls.incrementAndGet();
            return ModelAccessResolution.resolved(
                    TargetPropertyPath.propertyPath(selector.value()));
        };
        ModelAccessCompilationResult result = new ModelAccessCompiler(resolver)
                .compile(definitions, symbols(definitions));

        assertEquals(ModelAccessCompilationStatus.FAILED, result.status(),
                result.diagnostics().toString());
        assertFalse(result.compilation().isPresent());
        assertEquals(0, calls.get());
        assertTrue(hasMessage(result.diagnostics(),
                "modelaccess.structure.invalid"), result.diagnostics().toString());
    }

    /** 替换标准合法 fixture 中的唯一 ModelAccess。 */
    private static RawDefinitionSet replaceModelAccess(Change change) {
        RawDefinitionSet original = ModelAccessTestFixture.targetMainPriority();
        List<RawDefinition> definitions = new ArrayList<RawDefinition>(
                original.definitions());
        for (int index = 0; index < definitions.size(); index++) {
            RawDefinition definition = definitions.get(index);
            if (definition.kind() == RawDefinitionKind.MODEL_ACCESS) {
                definitions.set(index, change.apply(definition));
            }
        }
        return new RawDefinitionSet(definitions);
    }

    /** 复制 RawDefinition，只替换根属性和 body。 */
    private static RawDefinition copyDefinition(
            RawDefinition definition,
            Map<String, String> attributes,
            RawNodeBody body) {
        return new RawDefinition(
                definition.kind(),
                definition.sourceOrdinal(),
                definition.sourceRef(),
                definition.ownerToken(),
                definition.name(),
                attributes,
                definition.references(),
                body,
                definition.format(),
                definition.schemaVersion());
    }

    /** 复制 Raw body，只替换属性和 children。 */
    private static RawNodeBody copyBody(
            RawNodeBody body,
            Map<String, String> attributes,
            List<RawNodeBody> children) {
        return new RawNodeBody(
                body.name(),
                attributes,
                body.scalar(),
                children,
                body.sourceRef());
    }

    /** 通过 T07 构造与当前 Raw 快照绑定的 SymbolTable。 */
    private static SymbolTable symbols(RawDefinitionSet definitions) {
        SymbolBuildResult result = new SymbolTableBuilder().build(definitions);
        assertEquals(SymbolBuildStatus.BUILT, result.status(),
                result.diagnostics().toString());
        return result.symbolTable().get();
    }

    /** 查询稳定 Diagnostic messageKey。 */
    private static boolean hasMessage(
            List<Diagnostic> diagnostics,
            String messageKey) {
        return diagnostics.stream().anyMatch(diagnostic ->
                messageKey.equals(diagnostic.messageKey()));
    }

    /** 构造保持插入顺序的属性集合。 */
    private static Map<String, String> attributes(String... values) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (int index = 0; index < values.length; index += 2) {
            result.put(values[index], values[index + 1]);
        }
        return result;
    }

    /** RawDefinition 变更函数。 */
    private interface Change {
        RawDefinition apply(RawDefinition definition);
    }
}
