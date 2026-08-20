# BM-R07 Testability Matrix

- Revision: `BM-R07@7d7bf504ca9d`

| Case | Rule / observable expectation | Negative boundary |
|---|---|---|
| BM-R07-LINEAGE-01 | DEC_COMPILER historical evidence resolves to the same logical COMPILER model lineage | No second runtime/module authority may be inferred |
| BM-R07-SYSTEM-01 | Explicit SystemKey registration is deterministic across source order | Duplicate/implicit/file-derived System identity fails |
| BM-R07-RULEVIEW-01 | Same RuleView name in different Systems resolves independently by `(SystemKey,name)` | Bare name / unknown composite key / same-System duplicate fails |
| BM-R07-ACCESS-STATIC-01 | Explicit legal READ/WRITE/EXECUTE can compile to an allow fact | Undeclared shared WRITE or invalid path fails before publication |
| BM-R07-ACCESS-RUNTIME-01 | Truly dynamic legal access is marked RuntimeGuardRequired | Runtime DENY happens before mutation/external side effect |
| BM-R07-PATH-01 | ModelPath exact semantics are shared by future consumers | Unknown/fuzzy/cross-model/non-composite path fails |
| BM-R07-DEFERRED-01 | P2 System/RuleView/model-access semantics are consumed; P3-P8 remain explicit Deferred | P2 must not execute P3-P7 runtime semantics early |
| BM-R07-PUBLICATION-01 | Any static ERROR leaves caller-held old EngineContext unchanged | No partial registry/context publication |
