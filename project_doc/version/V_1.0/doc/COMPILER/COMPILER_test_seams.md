# COMPILER P2 Test Seams

> Revision `DESIGN-P2-R28`; Impact `P2-IMPACT-R27`; TestDesign `TESTDESIGN-P2-R29`; status `NEEDS_REVIEW / MACHINE_BLOCKED`.

## CompiledModelSet aggregate seam

Compiler fixture builds two equivalent candidates and one candidate with a different materialization descriptor. Assert `CompiledModelSet.viewMaterializationIndex`, delegated `EngineContext.viewMaterializationIndex`, equals/hashCode and semantic digest all change/stay equal consistently. A dynamic target View with no exact descriptor must fail before `EngineContext` publication. Instrument MODEL to prove runtime `NormalizedBody`/XML/YAML/ViewData/default Context reads are zero.

## MODEL execution-root seam

Use a real origin object, captured Context and an existing MODEL `Container`. `RuntimeModelExecutionRoot.load` must invoke typed ModelDataFactory, construct the existing three-argument ModelLoader, call owned `Container.load`, and freeze a handle around the exact same ModelData. Spy/identity assertions must prove STARTER never creates/injects ModelData. `accessScope` is unavailable before a trusted load and after root close, and no thread-local/global/default registry can recover it.

## Scope/session failure seam

Drive each setup failure separately and assert exact public codes/results: inactive/stale scope, provenance mismatch, duplicate registration, ownership conflict, already-sealed/closed session. Each failure returns no `ProtectedAccessComposition`; resolver/capability/Guard/MODEL effect invocation count is zero. Missing class/setup failure is `INVALID_RED`.

## FLOW-R11 success seam

Normal path remains validate frame -> begin session -> register all trusted handles -> seal -> resolve -> access/capability -> Guard -> MODEL effect. Representative Rule/Change/CustomAction consumers share the same STARTER composition.

## Explicit excluded transaction behavior

Do **not** make post-copy POJO/Map restoration after a later legacy commit failure a blocking assertion. The user confirmed that behavior needs no change. Keep only successful real-origin write-back reachability and pre-effect fail-closed assertions that are already part of BM/Flow.

## Gate

R29 is planned TestDesign only. Risk scan, same-revision specialist Review and machine Evidence remain required before TDD.
