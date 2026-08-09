# P2 Dependency Graph

- Project：`doc-eq-code`
- Version：`V_1.0`
- Current authoritative candidate：`BM-R20 / FLOW-R10 / DESIGN-P2-R24 / TESTDESIGN-P2-R25`
- CrossModule / Impact projection：`P2-IMPACT-R23`
- Status：`NEEDS_REVIEW / MACHINE_BLOCKED`
- Decisions：AC-007 `OPTION_B / ACTIVE`；AccessOperation `READ_WRITE_ONLY / ACTIVE`

## Authoritative revision direction

```text
REQAN-P2-R01@d08612768131 + Overlay R04
        |
        v
BM-R20
        |
        v
FLOW-R10
        |
        v
DESIGN-P2-R24
        |
        v
TESTDESIGN-P2-R25
```

`P2-IMPACT-R23` is a parallel impact/cross-module projection and not an authoritative upstream Design input.

BM-R20 / FLOW-R10 use stable downstream artifact/trace refs. Exact downstream revision linkage lives here and in the central traceability projection.

## Compiler -> runtime binding transport

```text
P1 ModelAccessBinding
  ViewKey targetView
  SystemViewSelector                 compiler-only lexical input
  TargetPropertyPath(kind,value)     compiler-resolved
        |
        v
dec-core-compiler
  CompiledTargetBinding(
    targetViewKey,
    TARGET_MAIN|PROPERTY_PATH,
    exactResolvedValue)
  RuntimeBindingPlan(sourceTargetKey, compiledBinding)
        |
        v
dec-core-context
  immutable EngineContext / PolicyIndex
        |
        v
dec-core-starter production assembly
  RuntimeModelRegistrationInput(
    sourceTargetKey,
    compiledBinding,
    ModelData)
  exact EngineContext membership validation
        |
        v
dec-core-model RuntimeModelSession
  sealed typed association
        |
        v
RuntimeTargetResolver
  exact sourceTargetKey + compiledBinding + typed context
```

No stage may infer the binding from ModelData name, ViewData, list order, raw definitions, selector parsing or first-match iteration.

## Protected operation path

```text
typed invocation
 -> composition frame/owner equality
 -> exact typed runtime registration match
 -> unique ResolvedRuntimeTarget
 -> WRITE intent 0/1/N if WRITE
 -> one-shot capability
 -> Guard(ModelAccessRuleKey + same target/proof)
 -> dec-core-model actual READ or transactional WRITE
```

## Actual ModelData concurrency boundary

```text
actual ModelData/runtime handle
        |
        1:1
        v
RuntimeModelCoordinationCell
  activeSessionLease
  per-ModelPath lock + RuntimeMutationVersion
```

Registration provenance is not permission authority. `ModelAccessRuleKey + PolicyIndex + Guard` remains the sole READ/WRITE authority.

## Dependency direction

```text
compiler -> context              allowed
starter -> context               allowed
starter -> model                 allowed production assembly
model -> context                 allowed/existing
context -> compiler              forbidden
model -> starter                 forbidden
P3/P4/P6 core -> context         allowed
P3/P4/P6 core -> starter         forbidden
```

## Gate

20 formal P1 findings remain OPEN. Current same-revision specialist Review, risk scan, TDD and execution Evidence are still required; Implementation Plan/TDD/Development remain BLOCKED.
