package dec.demo.contract;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MixContractTest {

    private static final List<String> ROOTS = Arrays.asList(
            "mix/orm-config.xml", "mix/data/User.xml", "mix/data/Order.xml", "mix/data/Pay.xml",
            "mix/view/orm-view.xml", "mix/system/systems.xml", "mix/rule/user-rule.xml",
            "mix/rule/order-rule.xml", "mix/rule/payment-rule.xml", "mix/business/order-business.xml");

    @Test
    void mixFilesAreWellFormedAndReferencesExist() throws Exception {
        Set<String> visited = new LinkedHashSet<String>();
        for (String root : ROOTS) {
            validate(root, visited);
        }
        assertTrue(visited.size() >= 10, "Expected the complete mix document set");
    }

    @Test
    void informationBelongsToSystemAndUsesOnlySystemViews() throws Exception {
        Document systems = parse("mix/system/systems.xml");
        Map<String, Set<String>> systemViews = new LinkedHashMap<String, Set<String>>();
        Set<String> informationKeys = new LinkedHashSet<String>();

        NodeList systemNodes = systems.getElementsByTagName("system");
        for (int i = 0; i < systemNodes.getLength(); i++) {
            Element system = (Element) systemNodes.item(i);
            String systemName = system.getAttribute("name");
            Set<String> views = childRefNames(system, "view-info", "view-ref", "name");
            systemViews.put(systemName, views);

            Element informationInfo = directChild(system, "information-info");
            assertNotNull(informationInfo, "System must own information-info: " + systemName);
            for (Element information : directChildren(informationInfo, "information")) {
                String localName = information.getAttribute("name");
                assertFalse(localName.contains("."),
                        "System-owned Information name must be local: " + systemName + "." + localName);
                assertTrue(information.getAttribute("system-ref").isEmpty(),
                        "System-owned Information must not repeat system-ref: " + systemName + "." + localName);

                String viewRef = information.getAttribute("view-ref");
                if (!viewRef.isEmpty()) {
                    assertTrue(views.contains(viewRef),
                            "Information may only reference a View declared by its System: "
                                    + systemName + "." + localName + " -> " + viewRef);
                }
                assertTrue(information.getAttribute("model-ref").isEmpty(),
                        "Information must use view-ref instead of shared model-ref: "
                                + systemName + "." + localName);
                informationKeys.add(systemName + "." + localName);
            }
        }

        assertEquals(16, informationKeys.size(), "Expected all Information definitions to be System-owned");
        assertEquals(Collections.emptySet(), systemViews.get("common"),
                "common System must not own a View");
        assertTrue(informationKeys.contains("common.paySuccess"));
        assertTrue(informationKeys.contains("common.payError"));
        assertFalse(informationKeys.contains("order.paySuccess"));
        assertFalse(informationKeys.contains("order.payError"));
        Element commonSystem = findByName(systemNodes, "common");
        assertCrossSystemExpression(commonSystem, "paySuccess", "payment.success", "order.paySuccessStatus");
        assertCrossSystemExpression(commonSystem, "payError", "payment.error", "order.payErrorStatus");
        assertEquals(Collections.singleton("UserInfo"), systemViews.get("user"),
                "user System must not directly own OrderInfo");

        Element userSystem = findByName(systemNodes, "user");
        Element modelAccessInfo = directChild(userSystem, "model-access-info");
        Element orderInfoAccess = findByAttribute(
                directChildren(modelAccessInfo, "model-access"), "model-ref", "OrderInfo");
        Element userRead = findByAttribute(directChildren(orderInfoAccess, "read"), "path", "user");
        Element ref = directChild(userRead, "ref");
        assertNotNull(ref, "OrderInfo.user access must declare an explicit View mapping");
        assertEquals("UserInfo", ref.getAttribute("view"));
        assertEquals("user", ref.getAttribute("property"));

        Document views = parse("mix/view/orm-view.xml");
        Element userInfo = findByAttribute(elements(views, "view"), "name", "UserInfo");
        assertEquals("user", userInfo.getAttribute("target-main"),
                "ref@property=user must resolve by UserInfo.target-main first");
        assertEquals("TARGET_MAIN", resolveViewSelector(userInfo, "user"));
        assertEquals("PROPERTY", resolveViewSelector(userInfo, "name"));
        assertThrows(AssertionError.class, () -> resolveViewSelector(userInfo, "missing"));

        assertRuleViewsBelongToSystemViews(systemViews, "mix/rule/user-rule.xml");
        assertRuleViewsBelongToSystemViews(systemViews, "mix/rule/order-rule.xml");
        assertRuleViewsBelongToSystemViews(systemViews, "mix/rule/payment-rule.xml");

        Document business = parse("mix/business/order-business.xml");
        assertEquals(0, business.getElementsByTagName("information-info").getLength(),
                "BusinessScope must not own information-info");
        assertEquals(0, business.getElementsByTagName("information").getLength(),
                "BusinessScope must not define Information");

        NodeList all = business.getElementsByTagName("*");
        for (int i = 0; i < all.getLength(); i++) {
            Element element = (Element) all.item(i);
            String informationRef = element.getAttribute("information-ref");
            if (!informationRef.isEmpty()) {
                assertTrue(informationKeys.contains(informationRef),
                        "BusinessScope must reference a system-qualified InformationKey: " + informationRef);
            }
        }
    }

    private void assertCrossSystemExpression(
            Element commonSystem, String informationName, String firstRef, String secondRef) {
        Element informationInfo = directChild(commonSystem, "information-info");
        Element information = findByAttribute(
                directChildren(informationInfo, "information"), "name", informationName);
        String expression = information.getAttribute("expression");
        assertTrue(expression.contains(firstRef), informationName + " must reference " + firstRef);
        assertTrue(expression.contains(secondRef), informationName + " must reference " + secondRef);
        assertTrue(information.getAttribute("view-ref").isEmpty(),
                "common expression Information must not own a View");
        assertTrue(information.getAttribute("rule-ref").isEmpty(),
                "common expression Information must not own a RuleView");
    }

    private String resolveViewSelector(Element view, String selector) {
        if (selector.equals(view.getAttribute("target-main"))) {
            return "TARGET_MAIN";
        }
        Element propertyInfo = directChild(view, "property-info");
        assertNotNull(propertyInfo, "View must declare property-info for fallback lookup");
        for (Element property : directChildren(propertyInfo, "property")) {
            if (selector.equals(property.getAttribute("name"))) {
                return "PROPERTY";
            }
        }
        throw new AssertionError("View selector matches neither target-main nor property: "
                + view.getAttribute("name") + "." + selector);
    }

    private void assertRuleViewsBelongToSystemViews(
            Map<String, Set<String>> systemViews, String resource) throws Exception {
        Document rules = parse(resource);
        NodeList ruleViews = rules.getElementsByTagName("rule-view-info");
        for (int i = 0; i < ruleViews.getLength(); i++) {
            Element ruleView = (Element) ruleViews.item(i);
            String system = ruleView.getAttribute("system");
            String viewRef = ruleView.getAttribute("view-ref");
            assertTrue(systemViews.containsKey(system), "Unknown RuleView System: " + system);
            assertTrue(systemViews.get(system).contains(viewRef),
                    "RuleView may only reference a View declared by its System: "
                            + system + "." + ruleView.getAttribute("name") + " -> " + viewRef);
        }
    }

    private Set<String> childRefNames(
            Element owner, String containerName, String childName, String attributeName) {
        Element container = directChild(owner, containerName);
        assertNotNull(container, "Missing " + containerName + " under " + owner.getTagName());
        Set<String> values = new LinkedHashSet<String>();
        for (Element child : directChildren(container, childName)) {
            values.add(child.getAttribute(attributeName));
        }
        return values;
    }

    private Element findByName(NodeList nodes, String name) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            if (name.equals(element.getAttribute("name"))) {
                return element;
            }
        }
        throw new AssertionError("Missing element named " + name);
    }

    private Element findByAttribute(List<Element> elements, String attribute, String value) {
        for (Element element : elements) {
            if (value.equals(element.getAttribute(attribute))) {
                return element;
            }
        }
        throw new AssertionError("Missing element where " + attribute + "=" + value);
    }

    private List<Element> elements(Document document, String name) {
        NodeList nodes = document.getElementsByTagName(name);
        java.util.ArrayList<Element> values = new java.util.ArrayList<Element>();
        for (int i = 0; i < nodes.getLength(); i++) {
            values.add((Element) nodes.item(i));
        }
        return values;
    }

    private Element directChild(Element parent, String name) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE && name.equals(node.getNodeName())) {
                return (Element) node;
            }
        }
        return null;
    }

    private List<Element> directChildren(Element parent, String name) {
        java.util.ArrayList<Element> values = new java.util.ArrayList<Element>();
        if (parent == null) {
            return values;
        }
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE && name.equals(node.getNodeName())) {
                values.add((Element) node);
            }
        }
        return values;
    }

    private Document parse(String path) throws Exception {
        URL url = getClass().getClassLoader().getResource(path);
        assertNotNull(url, "Missing mix resource: " + path);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setExpandEntityReferences(false);
        try (InputStream in = url.openStream()) {
            return factory.newDocumentBuilder().parse(in);
        }
    }

    private void validate(String path, Set<String> visited) throws Exception {
        if (!visited.add(path)) {
            return;
        }
        Document doc = parse(path);
        NodeList all = doc.getElementsByTagName("*");
        for (int i = 0; i < all.getLength(); i++) {
            Element element = (Element) all.item(i);
            String ref = element.getAttribute("path");
            if (ref.startsWith("classpath:")) {
                String target = ref.substring("classpath:".length());
                if (target.endsWith("/")) {
                    assertNotNull(getClass().getClassLoader().getResource(target),
                            "Missing directory: " + target);
                } else {
                    validate(target, visited);
                }
            }
        }
    }
}
