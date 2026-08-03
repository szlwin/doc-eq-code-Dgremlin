package dec.core.compiler.symbol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.ActionKey;
import dec.core.context.model.BusinessScopeKey;
import dec.core.context.model.ConnectionKey;
import dec.core.context.model.DataKey;
import dec.core.context.model.DataSourceKey;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DirectoryKey;
import dec.core.context.model.InformationKey;
import dec.core.context.model.ProduceKey;
import dec.core.context.model.RuleViewKey;
import dec.core.context.model.SystemKey;
import dec.core.context.model.ViewKey;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T07 对 Context 已发布 TypedKey 的类型、owner 和不可变合同 Oracle。
 */
class TypedKeyContractTest {

    /**
     * 不同类型即使使用相同 lexical name，也必须保持独立身份并可同时存在。
     */
    @Test
    void isolatesSameNameAcrossTypedKeyClasses() {
        DataKey data = new DataKey("shared");
        ViewKey view = new ViewKey("shared");
        SystemKey system = new SystemKey("shared");
        DataSourceKey dataSource = new DataSourceKey("shared");
        ConnectionKey connection = new ConnectionKey("shared");

        assertNotEquals(data, view);
        assertNotEquals(view, system);
        TreeSet<DefinitionKey> keys = new TreeSet<DefinitionKey>();
        keys.addAll(Arrays.<DefinitionKey>asList(
                data, view, system, dataSource, connection));
        assertEquals(5, keys.size());
    }

    /**
     * Information 与 RuleView 必须精确绑定 System owner。
     */
    @Test
    void preservesSystemOwnerForInformationAndRuleView() {
        SystemKey order = new SystemKey("order");
        SystemKey payment = new SystemKey("payment");
        InformationKey orderInfo = new InformationKey(order, "status");
        InformationKey paymentInfo = new InformationKey(payment, "status");
        RuleViewKey ruleView = new RuleViewKey(order, "submit");

        assertEquals(order, orderInfo.owner());
        assertEquals(order, ruleView.owner());
        assertNotEquals(orderInfo, paymentInfo);
        assertTrue(orderInfo.canonical().contains(order.canonical()));
    }

    /**
     * Business owner 链必须逐层保留 Scope、Directory 和 Action 身份。
     */
    @Test
    void preservesBusinessOwnerChain() {
        BusinessScopeKey scope = new BusinessScopeKey("order-scope");
        DirectoryKey directory = new DirectoryKey(scope, "checkout");
        ActionKey action = new ActionKey(directory, "submit");
        ProduceKey produce = new ProduceKey(action, 17);

        assertEquals(scope, directory.owner());
        assertEquals(directory, action.owner());
        assertEquals(action, produce.owner());
        assertEquals(17, produce.sourceOrdinal());
    }

    /**
     * 无名 Produce 必须通过 sourceOrdinal 区分，不能依赖可选 name。
     */
    @Test
    void isolatesUnnamedProduceBySourceOrdinal() {
        ActionKey action = new ActionKey(
                new DirectoryKey(new BusinessScopeKey("scope"), "directory"),
                "action");
        ProduceKey first = new ProduceKey(action, 3);
        ProduceKey second = new ProduceKey(action, 4);

        assertNotEquals(first, second);
        assertNotEquals(first.canonical(), second.canonical());
        assertThrows(IllegalArgumentException.class,
                () -> new ProduceKey(action, -1));
    }

    /**
     * TypedKey 必须没有 public 写方法，公共实例方法只读。
     */
    @Test
    void exposesNoPublicMutator() {
        List<Class<?>> keyTypes = Arrays.<Class<?>>asList(
                DataSourceKey.class,
                ConnectionKey.class,
                DataKey.class,
                ViewKey.class,
                SystemKey.class,
                RuleViewKey.class,
                BusinessScopeKey.class,
                InformationKey.class,
                DirectoryKey.class,
                ActionKey.class,
                ProduceKey.class);
        for (Class<?> type : keyTypes) {
            for (Method method : type.getMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                String name = method.getName();
                assertFalse(name.startsWith("set"), type.getName() + "#" + name);
                assertFalse(name.startsWith("put"), type.getName() + "#" + name);
                assertFalse(name.startsWith("remove"), type.getName() + "#" + name);
                assertFalse(name.startsWith("clear"), type.getName() + "#" + name);
            }
        }
    }
}
