# DESIGN-R32 — TASK-P1-T09 I002 input identity rework

- Revision: `DESIGN-R32@P1-T09-REWORK-I002`
- Status: `PASSED`
- Rework Base: `19b14487646c66ab1d7a386e96fc4876581b214c`
- Original Completion: `COMPLETION-P1-T09-R01@ecfe3f53bde7` — invalidated by independent review, preserved as immutable history
- Dependency: `COMPLETION-P1-T08-R02@bab0993ecfd8`
- Branch/PR: `feature/p1-t09-engine-context-20260804-1040` / `#24`
- Mode: `SEQUENTIAL / auto / architecture_review / git_checkpoint`
- Owner: `dec-core-compiler / information + read-only symbol input binding`

## Rework findings

- `FND-P1-T09-I002-001`: canonical common identity and raw lexical identity diverge.
- `FND-P1-T09-I002-002`: T09 does not prove RawDefinitionSet and SymbolTable belong to the same snapshot.
- `FND-P1-T09-I002-003`: R27 first-commit Evidence points to a nonexistent SHA.
- `FND-P1-T09-I002-004`: parser counts the root expression as nesting depth one, so only 127 parenthesis levels are accepted.

## Canonical common identity

`common` authority and restrictions are decided only from canonical `SystemKey` identity:

```text
isCommon(SystemKey key) := key.name().equals("common")
```

Raw System name and Information owner lexical remain unchanged source facts. They may contain legal surrounding whitespace already accepted by T06/T07. The following paths must use the same canonical decision:

- Information compiler owner classification;
- common System body validation;
- common Information member validation;
- common ModelAccess validation;
- reference resolver cross-System policy.

For `System raw name = " common "` and `Information raw owner = " common "`, the canonical owner is `SystemKey("common")`. The definition receives common cross-System privileges and every common restriction. No raw token is rewritten.

## Complete input binding

Before common validation, parser invocation, owner lookup, reference resolution or Deferred creation, T09 must verify that the supplied SymbolTable was built from exactly the supplied RawDefinitionSet.

SymbolTable may expose one additive read-only predicate without exposing its snapshot:

```text
boolean isBuiltFrom(RawDefinitionSet definitions)
```

The predicate performs full RawDefinitionSet value-semantic equality against the immutable source snapshot already captured by T07. Existing SymbolTable queries, equality, hashCode and registry behavior remain unchanged.

Mismatch returns exactly one stable ERROR:

```text
messageKey = information.input.snapshot-mismatch
```

and must guarantee:

- FAILED status;
- parser not invoked;
- resolver not invoked;
- no owner lookup or common validation;
- no partial AST, dependency list or Deferred registry.

The contract covers deletion, replacement by another kind, addition, same key with changed body/expression/SourceRef, previous-revision table, ordering and ordinal changes.

## Nesting depth

The root expression is depth zero. Parenthesis nesting is counted only when entering `(`. Therefore:

```text
128 parenthesis levels -> PASSED
129 parenthesis levels -> information.expression.limit.exceeded
```

Length 8192 and token 1024 budgets remain unchanged.

## Revision Evidence correction

The R01 Revision Lock is immutable and remains historically wrong. I002 must add a correction Evidence record rather than overwrite it.

Recovery order:

1. Attempt to identify the real first commit for R27 and verify it through GitHub commit API.
2. Verify that the commit predates valid RED `404105e894853b36b0788ed40ac65d23d6ee8899` and contains plan blob `20a16d1e7b199088086f496fe94aeb8b8684d8ca`.
3. If the first commit cannot be recovered, use the earliest verifiable pre-RED checkpoint containing that exact blob, explicitly mark `first_commit_unrecoverable=true`, and do not repeat the invalid SHA as valid.
4. R02 Completion references the correction Evidence and preserves R01 unchanged.

## Components

- `InformationIdentity`: canonical common predicate and safe owner-key construction.
- `SymbolTable.isBuiltFrom`: read-only full snapshot consistency predicate.
- `InformationCompiler`: fail-fast snapshot gate before all semantic work.
- `InformationCommonValidator`: accepts canonical owner identity or derives exact SystemKey safely.
- `DefaultInformationReferenceResolver`: shares the same canonical common predicate.
- `DefaultInformationExpressionParser`: root depth zero and exact 128/129 boundary.

## Diagnostics

Existing diagnostics remain stable. New stable message key:

- `information.input.snapshot-mismatch`

Snapshot mismatch is exclusive at the entry gate; it is not aggregated with downstream diagnostics because downstream work must not start.

## Security and resource constraints

No mutable snapshot exposure, reflection execution, I/O, network, fuzzy lookup, cross-type downgrade, evaluation, DAG, cycle detection or cache. Parser/resolver state remains per call. Collections remain defensive and immutable.

## TDD gate

I002 RED must compile with Java 8, report zero test errors, preserve all I001/T08/T07/Compiler regressions, and fail only on new canonical-common, snapshot, Evidence-contract and depth-boundary assertions. Architecture Skeleton must establish shared identity and snapshot-gate seams while keeping controlled RED.

## Scope

Allowed production changes:

- `dec-core-compiler/src/main/java/dec/core/compiler/information/**`
- additive read-only snapshot predicate in `dec-core-compiler/src/main/java/dec/core/compiler/symbol/SymbolTable.java`

Allowed tests:

- `dec-core-compiler/src/test/java/dec/core/compiler/information/**`
- focused SymbolTable predicate regression if required.

Do not change RawDefinition lexical normalization, existing SymbolTable query semantics, Context, T08 public behavior, Compiler API, systems.xml, T10/T11 or P2-P7 semantics.

Every `@Override` annotation is on its own line. Methods, constructors and important identity, snapshot, parser, Diagnostic, resource and failure logic use Chinese comments.
