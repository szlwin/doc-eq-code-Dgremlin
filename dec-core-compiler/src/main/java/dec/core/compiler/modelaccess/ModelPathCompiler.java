package dec.core.compiler.modelaccess;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.ModelPath;
import dec.core.context.model.ViewKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** DEV-03 ModelPath 编译器：把共享 source path 解析为有限、规范、已验证的 ModelPath。 */
public final class ModelPathCompiler {

    /**
     * 按冻结 TestDesign seam 编译路径。AccessMode 显式进入编译上下文，禁止运行期再猜 READ/WRITE。
     */
    public ModelPathCompilationResult compile(
            SharedModelPath sourcePath,
            AccessMode accessMode,
            RawDefinition sourceView) {
        Objects.requireNonNull(accessMode, "accessMode");
        return compileAgainstView(sourceView, sourcePath);
    }

    /** 按 source View 的真实 property shape 编译；通配符仅在编译期有限展开。 */
    private ModelPathCompilationResult compileAgainstView(
            RawDefinition sourceView,
            SharedModelPath sourcePath) {
        Objects.requireNonNull(sourceView, "sourceView");
        Objects.requireNonNull(sourcePath, "sourcePath");
        if (sourceView.kind() != RawDefinitionKind.VIEW || !sourceView.name().isPresent()) {
            return failed(ModelAccessDiagnostics.sourceViewNotFound(
                    new ViewKey("<invalid-view>"), sourceView.sourceRef()));
        }
        List<RawNodeBody> roots = propertyRoots(sourceView.body());
        if ("*".equals(sourcePath.value())) {
            List<ModelPath> expanded = new ArrayList<ModelPath>();
            collectPaths(roots, new ArrayList<String>(), expanded);
            if (expanded.isEmpty()) {
                return failed(ModelAccessDiagnostics.selectorNotFound(
                        new ViewKey(sourceView.name().get()), sourceView.sourceRef()));
            }
            Collections.sort(expanded);
            return ModelPathCompilationResult.compiled(expanded);
        }

        String[] segments = sourcePath.value().split("\\.", -1);
        List<String> resolved = new ArrayList<String>(segments.length);
        List<RawNodeBody> level = roots;
        for (int index = 0; index < segments.length; index++) {
            RawNodeBody match = uniqueProperty(level, segments[index]);
            if (match == null) {
                return failed(ModelAccessDiagnostics.selectorNotFound(
                        new ViewKey(sourceView.name().get()), sourceView.sourceRef()));
            }
            if (isAmbiguous(level, segments[index])) {
                return failed(ModelAccessDiagnostics.selectorAmbiguous(
                        new ViewKey(sourceView.name().get()), match.sourceRef()));
            }
            resolved.add(segments[index]);
            if (index + 1 < segments.length) {
                level = directProperties(match);
                if (level.isEmpty()) {
                    return failed(ModelAccessDiagnostics.selectorNonComposite(
                            new ViewKey(sourceView.name().get()), match.sourceRef()));
                }
            }
        }
        return ModelPathCompilationResult.compiled(
                Collections.singletonList(ModelPath.ofSegments(resolved)));
    }

    /** 找到 view/property-info 下的顶层 property。 */
    private static List<RawNodeBody> propertyRoots(RawNodeBody body) {
        for (RawNodeBody child : body.children()) {
            if ("property-info".equals(child.name())) {
                return directProperties(child);
            }
        }
        return directProperties(body);
    }

    /** 只返回当前层直接 property，禁止跨层模糊搜索。 */
    private static List<RawNodeBody> directProperties(RawNodeBody body) {
        List<RawNodeBody> result = new ArrayList<RawNodeBody>();
        for (RawNodeBody child : body.children()) {
            if ("property".equals(child.name())) {
                result.add(child);
            }
        }
        return result;
    }

    /** 精确、大小写敏感地解析当前层唯一 property。 */
    private static RawNodeBody uniqueProperty(List<RawNodeBody> level, String name) {
        RawNodeBody found = null;
        for (RawNodeBody candidate : level) {
            if (name.equals(candidate.attributes().get("name"))) {
                if (found != null) {
                    return found;
                }
                found = candidate;
            }
        }
        return found;
    }

    /** 判断当前层是否存在多个同名候选。 */
    private static boolean isAmbiguous(List<RawNodeBody> level, String name) {
        int count = 0;
        for (RawNodeBody candidate : level) {
            if (name.equals(candidate.attributes().get("name")) && ++count > 1) {
                return true;
            }
        }
        return false;
    }

    /** 深度优先有限展开全部可命名 property 路径；结果随后统一排序。 */
    private static void collectPaths(
            List<RawNodeBody> level,
            List<String> prefix,
            List<ModelPath> result) {
        for (RawNodeBody property : level) {
            String name = property.attributes().get("name");
            if (name == null || name.isEmpty()) {
                continue;
            }
            List<String> path = new ArrayList<String>(prefix);
            path.add(name);
            result.add(ModelPath.ofSegments(path));
            collectPaths(directProperties(property), path, result);
        }
    }

    private static ModelPathCompilationResult failed(Diagnostic diagnostic) {
        return ModelPathCompilationResult.failed(Collections.singletonList(diagnostic));
    }
}
