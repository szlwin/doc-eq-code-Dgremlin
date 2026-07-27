from __future__ import annotations

import unittest
import xml.etree.ElementTree as ET
from pathlib import Path


REPO = Path(__file__).resolve().parents[6]
MAIN = REPO / "dec-demo/src/main/resources/mix"
TEST = REPO / "dec-demo/src/test/resources/mix"


class SystemInformationContractTest(unittest.TestCase):
    def setUp(self) -> None:
        self.systems = ET.parse(MAIN / "system/systems.xml").getroot()
        self.business = ET.parse(MAIN / "business/order-business.xml").getroot()
        self.views = ET.parse(MAIN / "view/orm-view.xml").getroot()

    def test_main_and_test_fixtures_match(self) -> None:
        for relative in (
            "system/systems.xml",
            "view/orm-view.xml",
            "rule/user-rule.xml",
            "business/order-business.xml",
        ):
            self.assertEqual((MAIN / relative).read_bytes(), (TEST / relative).read_bytes(), relative)

    def test_information_is_system_owned_and_view_scoped(self) -> None:
        keys: set[str] = set()
        system_views: dict[str, set[str]] = {}
        for system in self.systems.findall("system"):
            system_name = system.attrib["name"]
            views = {node.attrib["name"] for node in system.findall("./view-info/view-ref")}
            system_views[system_name] = views
            infos = system.findall("./information-info/information")
            self.assertTrue(infos, system_name)
            for info in infos:
                local_name = info.attrib["name"]
                self.assertNotIn(".", local_name)
                self.assertNotIn("system-ref", info.attrib)
                self.assertNotIn("model-ref", info.attrib)
                if "view-ref" in info.attrib:
                    self.assertIn(info.attrib["view-ref"], views)
                keys.add(f"{system_name}.{local_name}")

        self.assertEqual(16, len(keys))
        self.assertEqual(set(), system_views["common"])
        self.assertIn("common.paySuccess", keys)
        self.assertIn("common.payError", keys)
        self.assertNotIn("order.paySuccess", keys)
        self.assertNotIn("order.payError", keys)
        common = next(node for node in self.systems.findall("system") if node.attrib["name"] == "common")
        self.assert_cross_system_expression(common, "paySuccess", "payment.success", "order.paySuccessStatus")
        self.assert_cross_system_expression(common, "payError", "payment.error", "order.payErrorStatus")
        self.assertEqual({"UserInfo"}, system_views["user"])
        self.assertEqual([], self.business.findall(".//information-info"))
        self.assertEqual([], self.business.findall(".//information"))

        for element in self.business.iter():
            ref = element.attrib.get("information-ref")
            if ref:
                self.assertIn(ref, keys)

    def assert_cross_system_expression(
        self, system: ET.Element, name: str, first_ref: str, second_ref: str
    ) -> None:
        info = next(
            node for node in system.findall("./information-info/information")
            if node.attrib["name"] == name
        )
        expression = info.attrib["expression"]
        self.assertIn(first_ref, expression)
        self.assertIn(second_ref, expression)
        self.assertNotIn("view-ref", info.attrib)
        self.assertNotIn("rule-ref", info.attrib)

    def test_user_shared_model_mapping_is_explicit(self) -> None:
        user = next(node for node in self.systems.findall("system") if node.attrib["name"] == "user")
        access = next(
            node for node in user.findall("./model-access-info/model-access")
            if node.attrib["model-ref"] == "OrderInfo"
        )
        read = next(node for node in access.findall("read") if node.attrib["path"] == "user")
        refs = read.findall("ref")
        self.assertEqual(1, len(refs))
        self.assertEqual("UserInfo", refs[0].attrib["view"])
        self.assertEqual("user", refs[0].attrib["property"])

        user_info = next(node for node in self.views.findall("view") if node.attrib["name"] == "UserInfo")
        self.assertEqual("user", user_info.attrib["target-main"])
        self.assertEqual("TARGET_MAIN", self.resolve_view_selector(user_info, "user"))
        self.assertEqual("PROPERTY", self.resolve_view_selector(user_info, "name"))
        with self.assertRaises(AssertionError):
            self.resolve_view_selector(user_info, "missing")

    def resolve_view_selector(self, view: ET.Element, selector: str) -> str:
        if selector == view.attrib.get("target-main"):
            return "TARGET_MAIN"
        property_info = view.find("property-info")
        self.assertIsNotNone(property_info)
        for prop in property_info.findall("property"):
            if selector == prop.attrib.get("name"):
                return "PROPERTY"
        raise AssertionError(
            f"View selector matches neither target-main nor property: {view.attrib.get('name')}.{selector}"
        )

    def test_rule_views_use_views_declared_by_their_system(self) -> None:
        system_views = {
            system.attrib["name"]: {node.attrib["name"] for node in system.findall("./view-info/view-ref")}
            for system in self.systems.findall("system")
        }
        for relative in ("rule/user-rule.xml", "rule/order-rule.xml", "rule/payment-rule.xml"):
            root = ET.parse(MAIN / relative).getroot()
            for rule_view in root.findall("rule-view-info"):
                self.assertIn(rule_view.attrib["view-ref"], system_views[rule_view.attrib["system"]])

    def test_all_mix_xml_is_well_formed(self) -> None:
        paths = sorted(MAIN.rglob("*.xml")) + sorted(TEST.rglob("*.xml"))
        self.assertEqual(20, len(paths))
        for path in paths:
            ET.parse(path)


if __name__ == "__main__":
    unittest.main()
