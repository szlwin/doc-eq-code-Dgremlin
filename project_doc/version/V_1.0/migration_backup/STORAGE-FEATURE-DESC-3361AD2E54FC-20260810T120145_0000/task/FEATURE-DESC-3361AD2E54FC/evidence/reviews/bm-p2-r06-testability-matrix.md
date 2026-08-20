# BM-R06 Testability Matrix

Revision: `BM-R06@6a0bce4fa0ae`

| Model boundary | Positive observation | Negative/boundary observation | Trace |
|---|---|---|---|
| Explicit System identity | multi-file order produces same SystemKey set/digest | duplicate SystemKey blocks publication | TR-P2-SYSTEM-RULEVIEW-001,009 |
| RuleView composite identity | `(system,name)` resolves exact owner-qualified RuleView | bare-name fallback and same-system duplicate fail; cross-system same-name remains isolated | TR-P2-SYSTEM-RULEVIEW-002,003 |
| ModelAccess READ/WRITE/EXECUTE | explicitly declared operation can compile to allow | undeclared shared WRITE and invalid target/path deny statically | TR-P2-SYSTEM-RULEVIEW-004,005 |
| Dynamic access Guard | statically legal dynamic access reaches common Guard | denial occurs before Rule/change/custom-action mutation or side effect | TR-P2-SYSTEM-RULEVIEW-006,007 |
| Atomic Context publication | fully valid System/RuleView/access candidate publishes once | any duplicate/reference/path/static access ERROR preserves previous Context | TR-P2-SYSTEM-RULEVIEW-008 |
| P2/P7 declaration boundary | P2 preserves compatibility facts without second authority | P2 cannot delete declaration boundary; P7 remains convergence gate | TR-P2-SYSTEM-RULEVIEW-010 |

The formal Test Design phase will assign final case IDs and executable fixtures. This matrix is review evidence that BM-R06 exposes deterministic, observable normal and failure outcomes without prematurely implementing test code.
