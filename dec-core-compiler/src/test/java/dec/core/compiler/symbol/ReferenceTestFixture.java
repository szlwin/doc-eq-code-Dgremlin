package dec.core.compiler.symbol;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.compiler.raw.RawReference;
import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TASK-P1-T08 使用的确定性 RawDefinition/SymbolTable 测试夹具。
 */
final class ReferenceTestFixture {
    private ReferenceTestFixture() {
    }

    /** 构造覆盖 T08 全部合法 P1 引用的前向引用夹具。 */
    static RawDefinitionSet legalDefinitions() {
        List<RawDefinition> values = new ArrayList<RawDefinition>();
        values.add(definition(RawDefinitionKind.ROOT_CONFIG, 0, null, "mix",
                attrs("name", "mix"), refs(), body("orm-config", attrs("name", "mix"), ref("root.xml", 1))));
        values.add(definition(RawDefinitionKind.DATA_SOURCE, 1, "mix", "data1",
                attrs("name", "data1"), refs(), body("orm-datasource", attrs("name", "data1"), ref("root.xml", 2))));
        values.add(definition(RawDefinitionKind.CONNECTION, 2, "mix", "con1",
                attrs("name", "con1"), refs(reference("/data-source-info/data-source@ref", "data1", "root.xml", 3)),
                body("orm-connection", attrs("name", "con1"), ref("root.xml", 3))));
        values.add(data(3, "user", "id", "status"));
        values.add(view(4, "UserInfo", "user", "id"));
        values.add(ruleView(5, "user", "check", "UserInfo"));
        values.add(system(6, "user", "user", "UserInfo"));
        values.add(definition(RawDefinitionKind.INFORMATION, 7, "user", "active",
                attrs("name", "active"), refs(), body("information", attrs("name", "active"), ref("systems.xml", 8))));
        values.add(definition(RawDefinitionKind.BUSINESS_SCOPE, 8, null, "scope",
                attrs("name", "scope"), refs(), body("business-config", attrs("name", "scope"), ref("business.xml", 1))));
        values.add(directory(9, "scope", "start", "user.active"));
        values.add(definition(RawDefinitionKind.DIRECTORY, 10, "scope", "ordered",
                attrs("name", "ordered"), refs(
                        reference("@information-ref", "user.active", "business.xml", 20),
                        reference("/subdirectory-info/subdirectory@rel", "start", "business.xml", 21)),
                body("directory", attrs("name", "ordered"), ref("business.xml", 20))));
        values.add(definition(RawDefinitionKind.ACTION, 11, "ordered", "save",
                attrs("name", "save"), refs(
                        reference("@system-ref", "user", "business.xml", 30),
                        reference("@rule-ref", "check", "business.xml", 31)),
                body("action", attrs("name", "save"), ref("business.xml", 30))));
        values.add(definition(RawDefinitionKind.PRODUCE, 12, "ordered/save", null,
                attrs("information-ref", "user.active"), refs(
                        reference("@information-ref", "user.active", "business.xml", 40)),
                body("produce", attrs("information-ref", "user.active"), ref("business.xml", 40))));
        return new RawDefinitionSet(values);
    }

    /** 构造 unknown、类型、property、owner 与 rule-system 多错误夹具。 */
    static RawDefinitionSet invalidDefinitions() {
        List<RawDefinition> values = new ArrayList<RawDefinition>();
        values.add(definition(RawDefinitionKind.ROOT_CONFIG, 0, null, "mix",
                attrs("name", "mix"), refs(), body("orm-config", attrs("name", "mix"), ref("z-root.xml", 1))));
        values.add(definition(RawDefinitionKind.DATA_SOURCE, 1, "mix", "data1",
                attrs("name", "data1"), refs(), body("orm-datasource", attrs("name", "data1"), ref("z-root.xml", 2))));
        values.add(definition(RawDefinitionKind.CONNECTION, 2, "mix", "wrongType",
                attrs("name", "wrongType"), refs(reference("/data-source-info/data-source@ref", "user", "b-root.xml", 7)),
                body("orm-connection", attrs("name", "wrongType"), ref("b-root.xml", 7))));
        values.add(definition(RawDefinitionKind.CONNECTION, 3, "mix", "unknown",
                attrs("name", "unknown"), refs(reference("/data-source-info/data-source@ref", "missing", "a-root.xml", 6)),
                body("orm-connection", attrs("name", "unknown"), ref("a-root.xml", 6))));
        values.add(data(4, "user", "id", "status"));
        values.add(view(5, "UserInfo", "user", "Id"));
        values.add(view(6, "OtherView", "user", "id"));
        values.add(ruleView(7, "user", "check", "OtherView"));
        values.add(system(8, "user", "user", "UserInfo"));
        values.add(definition(RawDefinitionKind.INFORMATION, 9, "user", "active",
                attrs("name", "active"), refs(), body("information", attrs("name", "active"), ref("systems.xml", 20))));
        values.add(ruleView(10, "order", "orderOnly", "OtherView"));
        values.add(system(11, "order", "user", "OtherView"));
        values.add(definition(RawDefinitionKind.BUSINESS_SCOPE, 12, null, "scope",
                attrs("name", "scope"), refs(), body("business-config", attrs("name", "scope"), ref("business.xml", 1))));
        values.add(directory(13, "scope", "start", "active"));
        values.add(definition(RawDefinitionKind.DIRECTORY, 14, "scope", "ordered",
                attrs("name", "ordered"), refs(
                        reference("@information-ref", "ghost.missing", "d-business.xml", 22),
                        reference("/subdirectory-info/subdirectory@rel", "absent", "c-business.xml", 21)),
                body("directory", attrs("name", "ordered"), ref("d-business.xml", 22))));
        values.add(definition(RawDefinitionKind.ACTION, 15, "ordered", "save",
                attrs("name", "save"), refs(
                        reference("@system-ref", "user", "f-business.xml", 30),
                        reference("@rule-ref", "orderOnly", "e-business.xml", 31)),
                body("action", attrs("name", "save"), ref("f-business.xml", 30))));
        RawReference duplicate = reference("@information-ref", "ghost.missing", "g-business.xml", 40);
        values.add(definition(RawDefinitionKind.PRODUCE, 16, "ordered/save", null,
                attrs("information-ref", "ghost.missing"), refs(duplicate, duplicate),
                body("produce", attrs("information-ref", "ghost.missing"), ref("g-business.xml", 40))));
        return new RawDefinitionSet(values);
    }

    /** 使用真实 T07 Builder 创建完整 SymbolTable。 */
    static SymbolTable symbols(RawDefinitionSet definitions) {
        SymbolBuildResult result = new SymbolTableBuilder().build(definitions);
        if (result.status() != SymbolBuildStatus.BUILT) {
            throw new AssertionError("测试夹具必须先通过 T07 Symbol 注册: " + result.diagnostics());
        }
        return result.symbolTable().get();
    }

    private static RawDefinition data(long ordinal, String name, String... properties) {
        List<RawNodeBody> propertyNodes = new ArrayList<RawNodeBody>();
        for (int index = 0; index < properties.length; index++) {
            propertyNodes.add(body("property", attrs("name", properties[index]),
                    ref("data-" + name + ".xml", 10 + index)));
        }
        RawNodeBody body = new RawNodeBody(
                "data",
                attrs("name", name),
                Optional.<String>empty(),
                Collections.singletonList(new RawNodeBody(
                        "property-info",
                        attrs(),
                        Optional.<String>empty(),
                        propertyNodes,
                        ref("data-" + name + ".xml", 9))),
                ref("data-" + name + ".xml", 1));
        return definition(RawDefinitionKind.DATA, ordinal, null, name,
                attrs("name", name), refs(), body);
    }

    private static RawDefinition view(long ordinal, String name, String data, String property) {
        return definition(RawDefinitionKind.VIEW, ordinal, null, name,
                attrs("name", name, "target-main", data),
                refs(reference("/property-info/property@ref-property", property,
                        "view-" + name + ".xml", 10)),
                body("view", attrs("name", name, "target-main", data),
                        ref("view-" + name + ".xml", 1)));
    }

    private static RawDefinition ruleView(long ordinal, String system, String name, String view) {
        return definition(RawDefinitionKind.RULE_VIEW, ordinal, system, name,
                attrs("system", system, "name", name, "view-ref", view),
                refs(reference("@view-ref", view, "rule-" + name + ".xml", 2)),
                body("rule-view-info", attrs("system", system, "name", name, "view-ref", view),
                        ref("rule-" + name + ".xml", 1)));
    }

    private static RawDefinition system(long ordinal, String name, String data, String view) {
        return definition(RawDefinitionKind.SYSTEM, ordinal, null, name,
                attrs("name", name), refs(
                        reference("/data-info/data-ref@ref", data, "systems.xml", 10 + (int) ordinal),
                        reference("/view-info/view-ref@ref", view, "systems.xml", 20 + (int) ordinal)),
                body("system", attrs("name", name), ref("systems.xml", 1 + (int) ordinal)));
    }

    private static RawDefinition directory(long ordinal, String scope, String name, String information) {
        return definition(RawDefinitionKind.DIRECTORY, ordinal, scope, name,
                attrs("name", name, "information-ref", information),
                refs(reference("@information-ref", information, "business.xml", 10 + (int) ordinal)),
                body("directory", attrs("name", name), ref("business.xml", 10 + (int) ordinal)));
    }

    private static RawDefinition definition(
            RawDefinitionKind kind,
            long ordinal,
            String owner,
            String name,
            Map<String, String> attributes,
            List<RawReference> references,
            RawNodeBody body) {
        return new RawDefinition(
                kind,
                ordinal,
                body.sourceRef(),
                owner == null ? Optional.<String>empty() : Optional.of(owner),
                name == null ? Optional.<String>empty() : Optional.of(name),
                attributes,
                references,
                body,
                DocumentFormat.XML,
                "1.0");
    }

    private static RawReference reference(
            String role,
            String target,
            String sourceId,
            int line) {
        return new RawReference(role, target, ref(sourceId, line));
    }

    private static List<RawReference> refs(RawReference... references) {
        return Arrays.asList(references);
    }

    private static RawNodeBody body(
            String name,
            Map<String, String> attributes,
            SourceRef sourceRef) {
        return new RawNodeBody(
                name,
                attributes,
                Optional.<String>empty(),
                Collections.<RawNodeBody>emptyList(),
                sourceRef);
    }

    private static Map<String, String> attrs(String... values) {
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        for (int index = 0; index < values.length; index += 2) {
            attributes.put(values[index], values[index + 1]);
        }
        return attributes;
    }

    private static SourceRef ref(String sourceId, int line) {
        return new SourceRef(sourceId, line, 1, "/definition[" + line + "]");
    }
}
