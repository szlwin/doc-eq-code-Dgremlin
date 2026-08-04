package dec.core.compiler.modelaccess;

import static org.junit.jupiter.api.Assertions.fail;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.compiler.raw.RawReference;
import dec.core.compiler.symbol.SymbolBuildResult;
import dec.core.compiler.symbol.SymbolBuildStatus;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.compiler.symbol.SymbolTableBuilder;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.SourceRef;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * T10 反射式 RED/GREEN 共用夹具。
 *
 * <p>生产边界不存在或未实现时统一转换为断言失败，保证 RED 不是测试编译错误或
 * 未接管的反射 Error。</p>
 */
final class ModelAccessTestFixture {
    private static final String COMPILER =
            "dec.core.compiler.modelaccess.ModelAccessCompiler";
    private static final String RESOLVER =
            "dec.core.compiler.modelaccess.ModelAccessSelectorResolver";

    private ModelAccessTestFixture() {
    }

    /** 使用生产默认构造器执行 T10 编译。 */
    static Object compile(RawDefinitionSet definitions, SymbolTable symbols) {
        try {
            Class<?> type = Class.forName(COMPILER);
            Object compiler = type.getConstructor().newInstance();
            Method method = type.getMethod(
                    "compile",
                    RawDefinitionSet.class,
                    SymbolTable.class);
            return method.invoke(compiler, definitions, symbols);
        } catch (InvocationTargetException exception) {
            fail("T10 编译入口不得抛出未接管异常", root(exception));
            return null;
        } catch (ReflectiveOperationException exception) {
            fail("T10 ModelAccessCompiler 生产边界尚未实现", exception);
            return null;
        }
    }

    /** 注入计数 resolver，验证入口门禁早于 selector 工作。 */
    static CountingCompilation compileWithCountingResolver(
            RawDefinitionSet definitions,
            SymbolTable symbols) {
        AtomicInteger calls = new AtomicInteger();
        try {
            Class<?> resolverType = Class.forName(RESOLVER);
            Object resolver = Proxy.newProxyInstance(
                    resolverType.getClassLoader(),
                    new Class<?>[] {resolverType},
                    (proxy, method, args) -> {
                        calls.incrementAndGet();
                        return null;
                    });
            Class<?> compilerType = Class.forName(COMPILER);
            Constructor<?> constructor = compilerType.getConstructor(resolverType);
            Object compiler = constructor.newInstance(resolver);
            Object result = compilerType.getMethod(
                    "compile",
                    RawDefinitionSet.class,
                    SymbolTable.class).invoke(compiler, definitions, symbols);
            return new CountingCompilation(result, calls.get());
        } catch (InvocationTargetException exception) {
            fail("快照门禁不得调用 resolver 或抛出异常", root(exception));
            return null;
        } catch (ReflectiveOperationException exception) {
            fail("T10 resolver seam 尚未实现", exception);
            return null;
        }
    }

    /** 调用无参数合同方法。 */
    static Object call(Object target, String methodName) {
        try {
            return target.getClass().getMethod(methodName).invoke(target);
        } catch (InvocationTargetException exception) {
            fail("调用 " + methodName + " 时不得抛出异常", root(exception));
            return null;
        } catch (ReflectiveOperationException exception) {
            fail("缺少 T10 合同方法: " + methodName, exception);
            return null;
        }
    }

    /** 返回结果状态名称。 */
    static String status(Object result) {
        return String.valueOf(call(result, "status"));
    }

    /** 返回失败 Diagnostic。 */
    @SuppressWarnings("unchecked")
    static List<Diagnostic> diagnostics(Object result) {
        return (List<Diagnostic>) call(result, "diagnostics");
    }

    /** 返回成功 Compilation。 */
    static Object compilation(Object result) {
        @SuppressWarnings("unchecked")
        Optional<Object> value = (Optional<Object>) call(result, "compilation");
        return value.orElse(null);
    }

    /** 返回 Compilation 的 Binding。 */
    @SuppressWarnings("unchecked")
    static List<Object> bindings(Object compilation) {
        return (List<Object>) call(compilation, "bindings");
    }

    /** 构建合法 T07 SymbolTable。 */
    static SymbolTable symbols(RawDefinitionSet definitions) {
        SymbolBuildResult result = new SymbolTableBuilder().build(definitions);
        if (result.status() != SymbolBuildStatus.BUILT) {
            throw new AssertionError("T10 夹具必须先通过 T07: " + result.diagnostics());
        }
        return result.symbolTable().get();
    }

    /** target-main 与同名 property 同时存在的合法输入。 */
    static RawDefinitionSet targetMainPriority() {
        List<RawNodeBody> targetProperties = Arrays.asList(
                property("user", property("id")),
                property("id"));
        return definitions(
                view(0, "OrderInfo", "order", property("user", property("id"))),
                view(1, "UserInfo", "user", targetProperties),
                system(2, "user", Arrays.asList("UserInfo")),
                modelAccess(3, "user", "OrderInfo",
                        access("read", "user",
                                ref("UserInfo", "user"))));
    }

    /** target-main 未命中后解析嵌套 property path。 */
    static RawDefinitionSet nestedPropertyFallback() {
        return definitions(
                view(0, "OrderInfo", "order",
                        property("payInfo", property("payDetailList", property("id")))),
                system(1, "payment", Arrays.asList("OrderInfo")),
                modelAccess(2, "payment", "OrderInfo",
                        access("read", "payInfo.payDetailList",
                                ref("OrderInfo", "payInfo.payDetailList"))));
    }

    /** 多 ref、直接访问和 P2 Deferred 的合法输入。 */
    static RawDefinitionSet multiBinding() {
        return definitions(
                view(0, "OrderInfo", "order",
                        property("payInfo", property("payDetailList"))),
                view(1, "UserInfo", "user", property("id")),
                system(2, "payment", Arrays.asList("OrderInfo", "UserInfo")),
                modelAccess(3, "payment", "OrderInfo",
                        access("read", "user",
                                ref("UserInfo", "user"),
                                ref("OrderInfo", "payInfo.payDetailList")),
                        access("write", "id")));
    }

    /** target View 未在当前 System 声明。 */
    static RawDefinitionSet undeclaredView() {
        return definitions(
                view(0, "OrderInfo", "order", property("id")),
                view(1, "UserInfo", "user", property("id")),
                system(2, "user", Arrays.asList("OrderInfo")),
                modelAccess(3, "user", "OrderInfo",
                        access("read", "user", ref("UserInfo", "user"))));
    }

    /** source model-ref 指向未知 View。 */
    static RawDefinitionSet unknownSourceView() {
        return definitions(
                view(0, "UserInfo", "user", property("id")),
                system(1, "user", Arrays.asList("UserInfo")),
                modelAccess(2, "user", "MissingModel",
                        access("read", "user", ref("UserInfo", "user"))));
    }

    /** selector 大小写差异不得降级。 */
    static RawDefinitionSet caseMismatch() {
        return definitions(
                view(0, "OrderInfo", "order", property("payInfo")),
                system(1, "payment", Arrays.asList("OrderInfo")),
                modelAccess(2, "payment", "OrderInfo",
                        access("read", "payInfo", ref("OrderInfo", "PayInfo"))));
    }

    /** property path 中间段不是复合属性。 */
    static RawDefinitionSet nonComposite() {
        return definitions(
                view(0, "OrderInfo", "order", property("payInfo")),
                system(1, "payment", Arrays.asList("OrderInfo")),
                modelAccess(2, "payment", "OrderInfo",
                        access("read", "payInfo.id", ref("OrderInfo", "payInfo.id"))));
    }

    /** 同层两个同名 property 产生歧义。 */
    static RawDefinitionSet ambiguousProperty() {
        return definitions(
                view(0, "OrderInfo", "order",
                        Arrays.asList(property("payInfo"), property("payInfo"))),
                system(1, "payment", Arrays.asList("OrderInfo")),
                modelAccess(2, "payment", "OrderInfo",
                        access("read", "payInfo", ref("OrderInfo", "payInfo"))));
    }

    /** 完全重复 ref。 */
    static RawDefinitionSet duplicateReference() {
        RawNodeBody duplicate = ref("UserInfo", "user");
        return definitions(
                view(0, "OrderInfo", "order", property("user")),
                view(1, "UserInfo", "user", property("id")),
                system(2, "user", Arrays.asList("UserInfo")),
                modelAccess(3, "user", "OrderInfo",
                        access("read", "user", duplicate, duplicate)));
    }

    /** WRITE source path 存在祖先/后代重叠。 */
    static RawDefinitionSet overlappingWrite() {
        return definitions(
                view(0, "OrderInfo", "order",
                        property("payInfo", property("payDetailList"))),
                system(1, "payment", Arrays.asList("OrderInfo")),
                modelAccess(2, "payment", "OrderInfo",
                        access("write", "payInfo"),
                        access("write", "payInfo.payDetailList")));
    }

    /** 一项合法、一项非法时不得发布部分结果。 */
    static RawDefinitionSet mixedBatchFailure() {
        return definitions(
                view(0, "OrderInfo", "order", property("id")),
                view(1, "UserInfo", "user", property("id")),
                system(2, "user", Arrays.asList("UserInfo")),
                modelAccess(3, "user", "OrderInfo",
                        access("read", "user", ref("UserInfo", "user"))),
                modelAccess(4, "user", "OrderInfo",
                        access("read", "missing", ref("UserInfo", "missing"))));
    }

    /** 创建相同 key 但 body 改变的输入快照。 */
    static RawDefinitionSet changedSnapshot(RawDefinitionSet original) {
        List<RawDefinition> values = new ArrayList<RawDefinition>(original.definitions());
        RawDefinition current = values.get(values.size() - 1);
        Map<String, String> changedAttributes = new LinkedHashMap<String, String>(
                current.attributes());
        changedAttributes.put("review-change", "true");
        values.set(values.size() - 1, new RawDefinition(
                current.kind(),
                current.sourceOrdinal(),
                current.sourceRef(),
                current.ownerToken(),
                current.name(),
                changedAttributes,
                current.references(),
                current.body(),
                current.format(),
                current.schemaVersion()));
        return new RawDefinitionSet(values);
    }

    /** 构造连续 ordinal 的完整定义集。 */
    private static RawDefinitionSet definitions(RawDefinition... definitions) {
        return new RawDefinitionSet(Arrays.asList(definitions));
    }

    /** 构造全局 View RawDefinition。 */
    private static RawDefinition view(
            long ordinal,
            String name,
            String targetMain,
            RawNodeBody... properties) {
        return view(ordinal, name, targetMain, Arrays.asList(properties));
    }

    /** 构造全局 View RawDefinition。 */
    private static RawDefinition view(
            long ordinal,
            String name,
            String targetMain,
            List<RawNodeBody> properties) {
        Map<String, String> attributes = attrs(
                "name", name,
                "target-main", targetMain);
        RawNodeBody propertyInfo = body(
                "property-info",
                attrs(),
                properties,
                source("view.xml", (int) ordinal + 2));
        return definition(
                RawDefinitionKind.VIEW,
                ordinal,
                null,
                name,
                attributes,
                body("view", attributes,
                        Collections.singletonList(propertyInfo),
                        source("view.xml", (int) ordinal + 1)));
    }

    /** 构造 System，并显式声明本地 View。 */
    private static RawDefinition system(
            long ordinal,
            String name,
            List<String> views) {
        List<RawNodeBody> declarations = new ArrayList<RawNodeBody>();
        for (String view : views) {
            declarations.add(body(
                    "view-ref",
                    attrs("name", view),
                    Collections.<RawNodeBody>emptyList(),
                    source("systems.xml", (int) ordinal + declarations.size() + 2)));
        }
        RawNodeBody viewInfo = body(
                "view-info",
                attrs(),
                declarations,
                source("systems.xml", (int) ordinal + 1));
        Map<String, String> attributes = attrs("name", name);
        return definition(
                RawDefinitionKind.SYSTEM,
                ordinal,
                null,
                name,
                attributes,
                body("system", attributes,
                        Collections.singletonList(viewInfo),
                        source("systems.xml", (int) ordinal + 1)));
    }

    /** 构造 ModelAccess 定义。 */
    private static RawDefinition modelAccess(
            long ordinal,
            String owner,
            String modelRef,
            RawNodeBody... accesses) {
        Map<String, String> attributes = attrs("model-ref", modelRef);
        List<RawReference> references = new ArrayList<RawReference>();
        references.add(new RawReference(
                "@model-ref",
                modelRef,
                source("systems.xml", (int) ordinal + 1)));
        for (RawNodeBody access : accesses) {
            for (RawNodeBody ref : access.children()) {
                references.add(new RawReference(
                        "/" + access.name() + "/ref@view",
                        ref.attributes().get("view"),
                        ref.sourceRef()));
                references.add(new RawReference(
                        "/" + access.name() + "/ref@property",
                        ref.attributes().get("property"),
                        ref.sourceRef()));
            }
        }
        return new RawDefinition(
                RawDefinitionKind.MODEL_ACCESS,
                ordinal,
                source("systems.xml", (int) ordinal + 1),
                Optional.of(owner),
                Optional.of(modelRef),
                attributes,
                references,
                body("model-access", attributes,
                        Arrays.asList(accesses),
                        source("systems.xml", (int) ordinal + 1)),
                DocumentFormat.XML,
                "1.0");
    }

    /** 构造 READ/WRITE 节点。 */
    private static RawNodeBody access(
            String mode,
            String path,
            RawNodeBody... refs) {
        return body(mode, attrs("path", path), Arrays.asList(refs),
                source("systems.xml", 30 + path.length()));
    }

    /** 构造目标 ref。 */
    private static RawNodeBody ref(String view, String property) {
        return body("ref", attrs("view", view, "property", property),
                Collections.<RawNodeBody>emptyList(),
                source("systems.xml", 50 + property.length()));
    }

    /** 构造 property 节点。 */
    private static RawNodeBody property(String name, RawNodeBody... children) {
        return property(name, Arrays.asList(children));
    }

    /** 构造 property 节点。 */
    private static RawNodeBody property(String name, List<RawNodeBody> children) {
        return body("property", attrs("name", name), children,
                source("view.xml", 70 + name.length()));
    }

    /** 构造公开矩阵合法的 RawDefinition。 */
    private static RawDefinition definition(
            RawDefinitionKind kind,
            long ordinal,
            String owner,
            String name,
            Map<String, String> attributes,
            RawNodeBody body) {
        return new RawDefinition(
                kind,
                ordinal,
                body.sourceRef(),
                owner == null ? Optional.<String>empty() : Optional.of(owner),
                name == null ? Optional.<String>empty() : Optional.of(name),
                attributes,
                Collections.<RawReference>emptyList(),
                body,
                DocumentFormat.XML,
                "1.0");
    }

    /** 构造 Raw body。 */
    private static RawNodeBody body(
            String name,
            Map<String, String> attributes,
            List<RawNodeBody> children,
            SourceRef sourceRef) {
        return new RawNodeBody(
                name,
                attributes,
                Optional.<String>empty(),
                children,
                sourceRef);
    }

    /** 构造稳定属性映射。 */
    private static Map<String, String> attrs(String... values) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (int index = 0; index < values.length; index += 2) {
            result.put(values[index], values[index + 1]);
        }
        return result;
    }

    /** 构造来源位置。 */
    private static SourceRef source(String file, int line) {
        return new SourceRef(file, line, 1, "/fixture[" + line + "]");
    }

    /** 展开反射异常根因。 */
    private static Throwable root(InvocationTargetException exception) {
        return exception.getCause() == null ? exception : exception.getCause();
    }

    /** resolver 调用计数结果。 */
    static final class CountingCompilation {
        private final Object result;
        private final int calls;

        private CountingCompilation(Object result, int calls) {
            this.result = result;
            this.calls = calls;
        }

        Object result() {
            return result;
        }

        int calls() {
            return calls;
        }
    }
}
