# TP-P1-COMPILER-F01-R27 — TASK-P1-T09 implementation plan

- Revision: `TP-P1-COMPILER-F01-R27@P1-T09-I001`
- Status: `PASSED`
- Design: `DESIGN-R31@P1-T09-I001`
- Base: `dev_all@e47551e0c79984d8f3fafc0ce379da76ad0d5593`
- Dependency: `COMPLETION-P1-T08-R02@bab0993ecfd8`
- Branch/PR: `feature/p1-t09-engine-context-20260804-1040` / `#24`

## Sequential workflow

1. Freeze TASK, R31 and R27 before new tests.
2. Review grammar, ownership, common, diagnostics, Deferred and scope boundaries.
3. Add reflection-based T09 Oracle while production remains at baseline; obtain Java 8 RED with errors=0.
4. Add Architecture Skeleton for AST/results/parser/resolver/validators/compiler; keep controlled RED.
5. Run ArchitectureReviewAgent and SpecComplianceReviewAgent.
6. Implement tokenizer/parser, exact Key resolution, ordinary owner policy, common policy and P3 Deferred.
7. Run `InformationOwnershipTest` and `CommonInformationExpressionTest`, plus independent negative review tests.
8. Run full P0, download Artifact, independently verify SHA-256 and Surefire XML.
9. Run Code/Architecture/Security/TDD/TestEvidence/Completion reviews.
10. Delete the temporary source-snapshot workflow.
11. Write Completion, review, resume_context, handoff and machine checkpoint.
12. Re-run P0 and Artifact verification at the final documented Head.
13. Update PR #24 to Ready for Review; do not merge.

## Allowed files

- Production: `dec-core-compiler/src/main/java/dec/core/compiler/information/**`
- Tests: `dec-core-compiler/src/test/java/dec/core/compiler/information/**`
- T09 design/task/evidence/review/resume/handoff/checkpoint documents.
- Current `systems.xml` is already correct and remains unchanged unless a confirmed contract mismatch is found.

## Test Oracle

`InformationOwnershipTest` covers local qualified references, cross-System rejection, malformed/unqualified and unknown references, owner mismatch, no partial output, stable diagnostics, immutability and absence of evaluate/cache/global state.

`CommonInformationExpressionTest` covers the real Canonical → Raw → Symbol → T09 path for common.paySuccess/payError, four exact dependency facts, two P3 Information Deferred definitions, common member restrictions, unknown/unqualified references, allowed indirect cycles, precedence and parentheses.

## Architecture Skeleton

Public/package types compile on Java 8 but default implementations return a stable `information.not-implemented` ERROR. Skeleton must not hard-code fixtures or make T09 tests green.

## Concrete implementation

- single-pass tokenizer with length/token budgets;
- recursive-descent parser with depth budget;
- immutable AST canonical/reference traversal;
- strict two-segment target parser;
- one exact `InformationKey` + `SymbolTable.find` per reference;
- same-owner ordinary policy and complete common Raw-body validation;
- full-batch diagnostic aggregation/deduplication;
- construct resolved expressions and immutable Deferred registry only after all checks pass.

## Validation

```bash
./mvnw -pl dec-core-compiler -am \
  -Dtest=InformationOwnershipTest,CommonInformationExpressionTest test
./mvnw --batch-mode --no-transfer-progress clean verify
```

MySQL without configuration is `SKIPPED_NOT_APPLICABLE`, never PASSED.

## Stop conditions

Stop on new expression semantics, P1 evaluation/DAG/cycle/cache work, public-contract changes outside allowed scope, fuzzy/cross-owner lookup, open P0/P1 findings, invalid RED, or final Head differing from the tested Head.
