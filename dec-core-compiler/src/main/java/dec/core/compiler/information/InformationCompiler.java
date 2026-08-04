package dec.core.compiler.information;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DeferredKind;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.InformationKey;
import dec.core.context.model.NormalizedBody;
import dec.core.context.model.RequiredStage;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * 将 System-owned Information expression 编译为强类型 P3 Deferred 的无状态协调器。
 */
public final class InformationCompiler {
    private final InformationExpressionParser parser;
    private final InformationReferenceResolver resolver;

    /** 创建使用生产预算 parser 和精确 TypedKey resolver 的编译器。 */
    public InformationCompiler() {
        this(new DefaultInformationExpressionParser(),
                new DefaultInformationReferenceResolver());
    }

    /** 创建使用显式 seam 的编译器，供独立测试替换 parser/resolver。 */
    public InformationCompiler(
            InformationExpressionParser parser,
            InformationReferenceResolver resolver) {
        this.parser = Objects.requireNonNull(parser, "parser");
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    /**
     * 编译完整 RawDefinitionSet；任一 ERROR 都不发布部分 AST、依赖或 Deferred。
     */
    public InformationCompilationResult compile(
            RawDefinitionSet definitions,
            SymbolTable symbols) {
        if (definitions == null || symbols == null) {
            return InformationCompilationResult.failed(Collections.singletonList(
                    InformationDiagnostics.create(
                            DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                            "information.input.required",
                            null,
                            new SourceRef("<unknown-information-source>", 0, 0, "/"),
                            "请提供完整 RawDefinitionSet 与 SymbolTable")));
        }

        /*
         * 快照门禁必须位于所有语义工作之前，避免旧 SymbolTable 提供当前批次不存在的依赖。
         */
        if (!symbols.isBuiltFrom(definitions)) {
            return InformationCompilationResult.failed(Collections.singletonList(
                    InformationDiagnostics.snapshotMismatch(definitions)));
        }

        Set<Diagnostic> diagnostics = new LinkedHashSet<Diagnostic>();
        InformationCommonValidator.validateSystem(definitions, diagnostics);
        List<ResolvedInformationExpression> expressions =
                new ArrayList<ResolvedInformationExpression>();
        Map<DeferredKey, DeferredDefinition> deferred =
                new TreeMap<DeferredKey, DeferredDefinition>();

        for (RawDefinition definition
                : definitions.definitions(RawDefinitionKind.INFORMATION)) {
            compileInformation(
                    definition,
                    symbols,
                    diagnostics,
                    expressions,
                    deferred);
        }
        InformationCommonValidator.validateModelAccess(definitions, diagnostics);

        if (!diagnostics.isEmpty()) {
            return InformationCompilationResult.failed(
                    InformationDiagnostics.sorted(diagnostics));
        }
        return InformationCompilationResult.compiled(new InformationCompilation(
                expressions,
                new ImmutableDeferredRegistry(deferred)));
    }

    /**
     * 编译单个 Information；普通无 expression 定义在 T09 保持非 Deferred。
     */
    private void compileInformation(
            RawDefinition definition,
            SymbolTable symbols,
            Set<Diagnostic> diagnostics,
            List<ResolvedInformationExpression> expressions,
            Map<DeferredKey, DeferredDefinition> deferred) {
        if (!definition.ownerToken().isPresent()
                || !definition.name().isPresent()) {
            diagnostics.add(InformationDiagnostics.owner(definition, null));
            return;
        }
        String ownerToken = definition.ownerToken().get();
        String name = definition.name().get();
        InformationKey ownerKey;
        try {
            ownerKey = new InformationKey(new SystemKey(ownerToken), name);
        } catch (IllegalArgumentException failure) {
            diagnostics.add(InformationDiagnostics.owner(definition, null));
            return;
        }

        Optional<RawDefinition> registered = symbols.find(ownerKey);
        if (!registered.isPresent() || !registered.get().equals(definition)) {
            diagnostics.add(InformationDiagnostics.owner(definition, ownerKey));
            return;
        }

        String expression = definition.attributes().get("expression");
        boolean common = InformationIdentity.isCommon(ownerKey.owner());
        if (common) {
            InformationCommonValidator.validInformation(
                    definition,
                    ownerKey,
                    diagnostics);
        }
        if (expression == null || expression.trim().isEmpty()) {
            if (common) {
                diagnostics.add(InformationDiagnostics.commonMember(
                        definition,
                        ownerKey));
            }
            return;
        }

        InformationExpressionParseResult parsed =
                parser.parse(expression, definition.sourceRef());
        diagnostics.addAll(parsed.diagnostics());
        if (!parsed.ast().isPresent()) {
            return;
        }

        InformationReferenceResolutionResult resolved = resolver.resolve(
                ownerKey,
                parsed.ast().get(),
                symbols,
                definition.sourceRef());
        diagnostics.addAll(resolved.diagnostics());
        if (!resolved.diagnostics().isEmpty()) {
            return;
        }

        ResolvedInformationExpression resolvedExpression =
                new ResolvedInformationExpression(
                        ownerKey,
                        parsed.ast().get(),
                        resolved.references(),
                        definition.sourceRef());
        expressions.add(resolvedExpression);
        DeferredKey deferredKey = new DeferredKey(
                ownerKey,
                DeferredKind.INFORMATION,
                0);
        DeferredDefinition deferredDefinition = new DeferredDefinition(
                deferredKey,
                RequiredStage.P3,
                "information-expression-evaluation",
                definition.sourceRef(),
                new NormalizedBody(
                        "information-expression-ast/v1",
                        parsed.ast().get().canonical()),
                new ArrayList<dec.core.context.model.DefinitionKey>(
                        resolved.references()));
        deferred.put(deferredKey, deferredDefinition);
    }
}
