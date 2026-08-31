# BM-R07 Stable ID / Lineage Audit

- Historical baseline: `BM-R05@4ecb1f8c09f4` (`DEC_COMPILER`)
- P2 baseline: `BM-R06@6a0bce4fa0ae` (`COMPILER`)
- Candidate: `BM-R07@7d7bf504ca9d` (`COMPILER`)
- Result: **PASSED** — every BM-R05 stable ID remains present in BM-R06/R07; R07 adds lineage only and does not delete/rename an existing stable ID.

## Collection counts

| Collection | BM-R05 IDs | BM-R06 IDs | Missing |
|---|---:|---:|---|
| terms | 14 | 18 | - |
| scenarios | 8 | 12 | - |
| entities | 8 | 9 | - |
| valueObjects | 10 | 13 | - |
| aggregates | 2 | 3 | - |
| invariants | 15 | 22 | - |
| stateMachines | 1 | 1 | - |
| services | 7 | 11 | - |
| policies | 6 | 9 | - |
| events | 3 | 3 | - |
| businessErrors | 23 | 30 | - |
| traceability | 9 | 19 | - |

## BM-R06 declared updates to existing stable IDs

`AGG-COMPILATION-SESSION`, `INV-COMPILER-008`, `POL-DEFERRED-BOUNDARY`, `POL-MODEL-ACCESS-SELECTOR`, `SVC-REFERENCE-RESOLUTION`, `TERM-DEFERRED-DEFINITION`, `VO-DEFERRED-DEFINITION`, `VO-MODEL-ACCESS-BINDING`

## Lineage assertion

- `DEC_COMPILER` and `COMPILER` are one logical compiler model lineage, not parallel current modules.
- Structured relation: `REL-COMPILER-DOC-LINEAGE` (`DEC-CORE-COMPILER SUPERSEDES DEC-COMPILER-DOC-LEGACY` for documentation identity/history only).
- P2 business semantics from BM-R06 remain unchanged except the new lineage term; readability is a projection change, not a silent domain-rule rewrite.
