# TP-P1-COMPILER-F01-R28 — TASK-P1-T09 I002 implementation plan

- Revision: `TP-P1-COMPILER-F01-R28@P1-T09-REWORK-I002`
- Status: `PASSED`
- Design: `DESIGN-R32@P1-T09-REWORK-I002`
- Rework Base: `19b14487646c66ab1d7a386e96fc4876581b214c`
- Branch/PR: `feature/p1-t09-engine-context-20260804-1040` / `#24`
- Original Completion: `COMPLETION-P1-T09-R01@ecfe3f53bde7` invalidated and preserved

## Sequential workflow

1. Freeze R32, R28 and TASK I002 before new tests.
2. Convert PR #24 to Draft and keep T10 blocked.
3. Recover R27 revision evidence or establish an explicitly degraded, verifiable pre-RED checkpoint.
4. Add I002 Oracle for padded common, snapshot mismatch and 128/129 depth boundaries.
5. Obtain valid Java 8 RED with errors=0 and all existing regressions green.
6. Add Architecture Skeleton for shared canonical identity and fail-fast input binding; keep controlled RED.
7. Run Architecture and Spec Compliance reviews.
8. Implement canonical common predicate, read-only SymbolTable snapshot predicate, compiler entry gate and parser depth correction.
9. Run T09 I002 directed tests, T09 I001, T08/T07, Compiler full and 12-module clean verify.
10. Run independent negative review tests for no parser/resolver invocation, raw lexical preservation and all mismatch forms.
11. Remove temporary source-snapshot workflow.
12. Run clean-code P0, download Artifact, independently verify ZIP SHA-256 and Surefire XML.
13. Write correction Evidence, Development, Testing, Review, Completion, TASK, resume_context, handoff and machine checkpoint.
14. Confirm only project_doc changes after clean-code Head, then rerun final documented P0 and Artifact verification.
15. Update PR #24 and mark Ready for Review; do not merge.

## Oracle matrix

### Canonical common

- padded common cross-System qualified reference succeeds;
- padded common forbidden Information member fails;
- padded common nonempty System data/view/rule-file section fails;
- padded common ModelAccess fails;
- padded common missing expression fails;
- RawDefinition owner/name lexical remains padded in source facts and diagnostics.

### Snapshot binding

All mismatch cases return only `information.input.snapshot-mismatch`, with no parser/resolver invocation and no partial output:

- current batch deletes an old Information;
- current batch replaces an old Information with ModelAccess at a continuous ordinal;
- current batch adds Information;
- same TypedKey but changed body/expression/SourceRef;
- previous-revision SymbolTable;
- ordering or sourceOrdinal changes.

Matching snapshots continue to compile normally.

### Depth

- 128 nested parentheses succeeds;
- 129 nested parentheses returns `information.expression.limit.exceeded`;
- existing length/token/precedence/operator tests remain green.

### Revision integrity

- invalid historical R27 SHA remains visible only as invalid history;
- correction Evidence points to a GitHub-readable commit/checkpoint;
- exact R27 blob is `20a16d1e7b199088086f496fe94aeb8b8684d8ca`;
- checkpoint predates valid R01 RED `404105e894853b36b0788ed40ac65d23d6ee8899`;
- R32/R28 first commits and blobs are recorded and verified before I002 RED.

## Architecture Skeleton

- introduce `InformationIdentity.isCommon(SystemKey)` and safe canonical owner helpers;
- introduce additive `SymbolTable.isBuiltFrom(RawDefinitionSet)` returning only a boolean;
- wire compiler entry gate with a stable not-yet-implemented failure until concrete logic is enabled;
- parser depth entry changes to zero only in concrete implementation;
- no fixture hard-coding or premature business GREEN.

## Concrete implementation

- construct canonical owner key once per Information and use it for all common decisions;
- validate common System and ModelAccess through canonical SystemKey creation without rewriting raw lexical;
- compare complete immutable RawDefinitionSet snapshot at compile entry;
- return one exclusive snapshot mismatch Diagnostic before allocating indexes or invoking seams;
- start parser root depth at zero and increment only when entering parentheses;
- preserve exact key lookup, full-batch atomic publication and stable diagnostic ordering.

## Validation commands

```bash
./mvnw -pl dec-core-compiler -am \
  -Dtest=InformationInputIdentityReworkTest,InformationOwnershipTest,CommonInformationExpressionTest,InformationIndependentReviewTest test
./mvnw --batch-mode --no-transfer-progress clean verify
```

MySQL without configuration is `SKIPPED_NOT_APPLICABLE`, never PASSED.

## Stop conditions

Stop on mutable snapshot exposure, raw lexical rewriting, fuzzy/cross-owner fallback, evaluation/DAG/cycle/cache work, changes outside allowed scope, invalid RED, open P0/P1 findings, unresolved Evidence binding or final Head differing from the tested Head.
