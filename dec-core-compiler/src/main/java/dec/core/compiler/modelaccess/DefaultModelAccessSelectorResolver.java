package dec.core.compiler.modelaccess;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.SystemKey;
import dec.core.context.model.ViewKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 只在当前 System 已声明的目标 View 内执行确定性 selector 解析。
 */
final class DefaultModelAccessSelectorResolver
        implements ModelAccessSelectorResolver {

    /**
     * 先精确匹配 target-main，未命中时再按 property 层级逐段精确遍历。
     */
    @Override
    public ModelAccessResolution resolve(
            SystemKey owner,
            SharedModelPath sourcePath,
            ViewKey targetView,
            SystemViewSelector selector,
            SymbolTable symbols) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(sourcePath, "sourcePath");
        Objects.requireNonNull(targetView, "targetView");
        Objects.requireNonNull(selector, "selector");
        Objects.requireNonNull(symbols, "symbols");

        Optional<RawDefinition> ownerDefinition = symbols.find(owner);
        if (!ownerDefinition.isPresent()
                || ownerDefinition.get().kind() != RawDefinitionKind.SYSTEM) {
            return failed(ModelAccessDiagnostics.ownerInvalid(
                    sourceRef(ownerDefinition)));
        }
        if (!declaresView(ownerDefinition.get(), targetView)) {
            return failed(ModelAccessDiagnostics.viewNotDeclared(
                    targetView,
                    ownerDefinition.get().sourceRef()));
        }

        Optional<RawDefinition> targetDefinition = symbols.find(targetView);
        if (!targetDefinition.isPresent()
                || targetDefinition.get().kind() != RawDefinitionKind.VIEW) {
            return failed(ModelAccessDiagnostics.selectorNotFound(
                    targetView,
                    ownerDefinition.get().sourceRef()));
        }

        RawDefinition view = targetDefinition.get();
        String targetMain = view.attributes().get("target-main");
        if (selector.value().equals(targetMain)) {
            return ModelAccessResolution.resolved(
                    TargetPropertyPath.targetMain(selector.value()));
        }
        return resolvePropertyPath(view, targetView, selector);
    }

    /** 在目标 View 的 property-info 树内逐段精确解析。 */
    private static ModelAccessResolution resolvePropertyPath(
            RawDefinition view,
            ViewKey targetView,
            SystemViewSelector selector) {
        List<RawNodeBody> candidates = rootProperties(view.body());
        List<String> segments = selector.segments();
        for (int index = 0; index < segments.size(); index++) {
            List<RawNodeBody> matches = namedProperties(
                    candidates,
                    segments.get(index));
            if (matches.isEmpty()) {
                return failed(ModelAccessDiagnostics.selectorNotFound(
                        targetView,
                        view.sourceRef()));
            }
            if (matches.size() > 1) {
                return failed(ModelAccessDiagnostics.selectorAmbiguous(
                        targetView,
                        matches.get(1).sourceRef()));
            }
            RawNodeBody matched = matches.get(0);
            if (index < segments.size() - 1) {
                candidates = directProperties(matched);
                if (candidates.isEmpty()) {
                    return failed(ModelAccessDiagnostics.selectorNonComposite(
                            targetView,
                            matched.sourceRef()));
                }
            }
        }
        return ModelAccessResolution.resolved(
                TargetPropertyPath.propertyPath(selector.value()));
    }

    /** 判断当前 System 是否显式声明目标 View。 */
    private static boolean declaresView(
            RawDefinition system,
            ViewKey targetView) {
        for (RawNodeBody section : system.body().children()) {
            if (!"view-info".equals(section.name())) {
                continue;
            }
            for (RawNodeBody declaration : section.children()) {
                if (!"view-ref".equals(declaration.name())) {
                    continue;
                }
                String name = declaration.attributes().get("name");
                try {
                    if (name != null && targetView.equals(new ViewKey(name))) {
                        return true;
                    }
                } catch (IllegalArgumentException failure) {
                    // 非法 lexical 由精确未声明结果统一 fail-closed，不执行修复。
                }
            }
        }
        return false;
    }

    /** 返回 View 根 property 候选。 */
    private static List<RawNodeBody> rootProperties(RawNodeBody view) {
        for (RawNodeBody section : view.children()) {
            if ("property-info".equals(section.name())) {
                return directProperties(section);
            }
        }
        return Collections.emptyList();
    }

    /** 返回当前节点的直接 property 子节点。 */
    private static List<RawNodeBody> directProperties(RawNodeBody parent) {
        List<RawNodeBody> properties = new ArrayList<RawNodeBody>();
        for (RawNodeBody child : parent.children()) {
            if ("property".equals(child.name())) {
                properties.add(child);
            }
        }
        return properties;
    }

    /** 返回 name 区分大小写完全相同的 property 候选。 */
    private static List<RawNodeBody> namedProperties(
            List<RawNodeBody> candidates,
            String name) {
        List<RawNodeBody> matches = new ArrayList<RawNodeBody>();
        for (RawNodeBody candidate : candidates) {
            if (name.equals(candidate.attributes().get("name"))) {
                matches.add(candidate);
            }
        }
        return matches;
    }

    /** 创建单项失败解析结果。 */
    private static ModelAccessResolution failed(Diagnostic diagnostic) {
        return ModelAccessResolution.failed(
                Collections.singletonList(diagnostic));
    }

    /** 返回可选定义的稳定来源位置。 */
    private static dec.core.context.model.SourceRef sourceRef(
            Optional<RawDefinition> definition) {
        return definition.isPresent() ? definition.get().sourceRef() : null;
    }
}
