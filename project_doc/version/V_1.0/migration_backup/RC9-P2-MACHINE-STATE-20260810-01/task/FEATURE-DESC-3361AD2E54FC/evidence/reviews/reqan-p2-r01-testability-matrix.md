# P2 REQAN-R01 Testability Matrix

Revision: `REQAN-P2-R01@d08612768131`
Source requirement: `../../doc/FEATURE-DESC-3361AD2E54FC/requirement.md`
Flow: `FLOW-CONFIG-COMPILE`

This is review-support evidence derived from the frozen requirement-analysis revision. It is not a Test Design phase deliverable and does not add implementation choices.

| Acceptance | Normal path | Boundary | Failure / forbidden side effect | Observable result |
|---|---|---|---|---|
| AC-P2-SYSTEM-RULEVIEW-001 | multiple System sources compile in stable order | source order permutation | duplicate System key blocks publication | identical semantic result/digest or deterministic duplicate diagnostic |
| AC-P2-SYSTEM-RULEVIEW-002 | same RuleView name in different Systems resolves independently | same name + different owner | duplicate `(system,name)` in same System blocks publication | owner-qualified Registry keys and stable diagnostic |
| AC-P2-SYSTEM-RULEVIEW-003 | `system-ref + rule-ref` resolves exact RuleView | valid cross-file reference | bare-name fallback / unknown System / unknown RuleView rejected | exact compiled target or source-aware error |
| AC-P2-SYSTEM-RULEVIEW-004 | declared READ/WRITE/EXECUTE permission succeeds independently | operation matrix and shared-model boundary | undeclared shared WRITE denied; no default allow | compile/runtime decision with operation + target + path |
| AC-P2-SYSTEM-RULEVIEW-005 | valid model path compiles consistently | nested path, collection/property boundary | unknown/invalid path statically rejected | same normalized path semantics across expression/change/query users |
| AC-P2-SYSTEM-RULEVIEW-006 | legal runtime-dependent access passes guard | runtime-dependent target/path | denied access fails before mutation/side effect | guard decision emitted before mutation; state remains unchanged on deny |
| AC-P2-SYSTEM-RULEVIEW-007 | Rule/change/custom-action mutation uses same guard | each mutation entry point | any bypass path is a failure | all entry points converge on identical authorization decision |
| AC-P2-SYSTEM-RULEVIEW-008 | successful compile publishes one immutable context | concurrent/multi-context compile | any compile error publishes nothing and preserves prior context | atomic publication + context isolation |
| AC-P2-SYSTEM-RULEVIEW-009 | equivalent invalid input yields stable diagnostics | source ordering permutation | ambiguous/guessing fallback forbidden | stable code/location/entity/order without secret leakage |
| AC-P2-SYSTEM-RULEVIEW-010 | declaration compatibility remains readable in P2 | legacy declaration coexists | P2 must not remove declaration runtime or create second authority | migration boundary remains explicit for P7 |

Coverage dimensions required for later Test Design: normal, boundary, duplicate/ambiguity, unknown reference, static deny, runtime deny, no-side-effect, deterministic ordering, context isolation, and migration compatibility.
