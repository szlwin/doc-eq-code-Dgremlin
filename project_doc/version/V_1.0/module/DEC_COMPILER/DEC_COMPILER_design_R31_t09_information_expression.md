# DESIGN-R31 — TASK-P1-T09 Information expression

- Revision: `DESIGN-R31@P1-T09-I001`
- Status: `PASSED`
- Base: `dev_all@e47551e0c79984d8f3fafc0ce379da76ad0d5593`
- Dependency: `COMPLETION-P1-T08-R02@bab0993ecfd8`
- Branch/PR: `feature/p1-t09-engine-context-20260804-1040` / `#24`
- Mode: `SEQUENTIAL / auto / architecture_review / git_checkpoint`
- Owner: `dec-core-compiler / dec.core.compiler.information`
- Trace: `TR-P1-COMPILER-004`, `TR-P1-COMPILER-008`

## Goal

Compile System-owned Information expressions into immutable AST, exact `InformationKey` dependencies and `RequiredStage.P3` `DeferredDefinition` objects. P1 does not evaluate expressions, build a DAG, detect cycles, cache values, or create runtime state.

## Input and atomic output

Input is one complete `RawDefinitionSet` plus its `SymbolTable`, with injectable parser and reference resolver seams. Success publishes one immutable `InformationCompilation` containing sorted resolved expressions and one immutable Deferred registry. Any ERROR returns FAILED with no partial AST, dependency list, or Deferred registry.

## Grammar

```text
expression := orExpr
orExpr     := andExpr ("or" andExpr)*
andExpr    := primary ("and" primary)*
primary    := qualifiedInformation | "(" expression ")"
qualifiedInformation := system "." information
```

`and` has higher precedence than `or`; operators are lowercase. References require exactly two nonblank segments. AST canonical form is `ref(system.info)`, `and(left,right)`, or `or(left,right)`. Budgets: 8,192 characters, 1,024 tokens, nesting depth 128.

## Ownership and common rules

- Every Information owner must resolve to its exact `SystemKey` and `InformationKey`.
- Ordinary System expressions may only reference Information owned by the same System.
- Ordinary cross-System references produce `MIX-INFORMATION-CROSS-SYSTEM`.
- Malformed or unqualified ordinary references produce `MIX-INFORMATION-OWNER`.
- Unknown qualified references produce `MIX-REF-UNKNOWN`.
- `common` may reference any existing fully qualified Information, including `common.*`; P1 does not reject indirect cycles.
- Unqualified/malformed common references produce `MIX-COMMON-UNQUALIFIED`.
- common Information must contain only `name + expression`; `view-ref`, `rule-ref`, `rule-data`, `change-data`, missing expression, nonempty data/view/rule-file sections, or ModelAccess produce `MIX-COMMON-MEMBER`.
- Ordinary non-expression Information is not evaluated and does not create an expression Deferred in T09.

## Components

- `InformationExpressionAst`: immutable `REFERENCE/AND/OR` value tree with stable canonical text and preorder references; no evaluate API.
- `InformationExpressionParser`: injectable `parse(expression, sourceRef)` seam.
- `InformationReferenceResolver`: injectable exact-key `resolve(owner, ast, symbols)` seam; success uses only `SymbolTable.find`.
- `InformationOwnershipValidator`: verifies Raw owner identity and same-owner policy.
- `CommonSystemValidator`: validates common structure from `RawNodeBody`, with no DOM/YAML/I/O dependency.
- `InformationCompiler`: stateless coordinator; validates the full batch, parses all expressions, aggregates diagnostics, then publishes all-or-nothing.

Each successful expression creates:

- `DeferredKey(ownerInformationKey, DeferredKind.INFORMATION, 0)`;
- `RequiredStage.P3`;
- `reasonCode=information-expression-evaluation`;
- `NormalizedBody("information-expression-ast/v1", ast.canonical())`;
- sorted resolved `InformationKey` references and the definition `SourceRef`.

## Diagnostics

Pass is `information-compilation`. Stable message keys:

- `information.owner.invalid`
- `information.reference.cross-system`
- `information.common.member.invalid`
- `information.common.reference.unqualified`
- `information.reference.unknown`
- `information.expression.syntax.invalid`
- `information.expression.limit.exceeded`

Diagnostics are deduplicated and sorted by the existing `Diagnostic.compareTo` contract.

## Resource and security constraints

All parser/resolver state is per call. No static mutable state, I/O, network, reflection execution, fuzzy lookup, cross-type downgrade, or evaluation cache. Collections are defensive copies and immutable.

## TDD and architecture gate

RED must compile with Java 8, have zero test errors, preserve all existing T08/Symbol/Raw/Context tests, and fail only on T09 assertions. Architecture Skeleton adds type/result/seam boundaries with controlled not-implemented failures; concrete logic starts only after Architecture and Spec reviews pass.

## Forbidden scope

Do not modify Context, T06 Raw, T07 Symbol or T08 public contracts; do not let BusinessScope own Information; do not implement T10, T11, or P2-P7 semantics. Every `@Override` is on its own line; methods and important parser/owner/common/diagnostic/resource/failure logic use Chinese comments.
