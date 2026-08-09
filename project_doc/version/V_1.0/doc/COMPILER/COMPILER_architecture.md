# COMPILER P2 Architecture

> Revision：`DESIGN-P2-R15`  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`

## 1. Module boundaries

- `dec-core-context`
  - neutral immutable `SystemKey`, `SystemVersionIdentity`, `CompiledSystem`, `RuleViewKey`, `CompiledRuleView`, `ModelPath`, `CompiledModelAccessRule`, `ModelAccessPolicyIndex`, `CompiledModelSet`, `EngineContext` read contracts;
  - no dependency on compiler/starter/business executors.
- `dec-core-compiler`
  - System/ownership collection, RuleView composite resolution, shared ModelPath compilation, access classification, PolicyIndex construction, semantic digest, candidate construction/publication;
  - no dependency on starter.
- `dec-context-config-parse-xml` / YAML frontend
  - preserve explicit System/RuleView/view-ref/rule-ref/model-access/raw-path/SourceRef facts only;
  - no authority or ownership inference outside declared configuration.
- `dec-core-starter`
  - production Bridge, resolver, capability, Gateway, Guard, runtime proof adapter composition;
  - no second System/RuleView/permission registry.
- `dec-demo`
  - real fixture/E2E verification only; never production dependency.
- P3/P4/P6
  - future consumers of the P2 seam; concrete execution remains downstream.

## 2. Publication closure

<a id="2-发布闭包"></a>

```text
canonical sources
 -> System symbols
 -> owner-qualified Data/View/RuleView/Rule/Information/access facts
 -> immutable CompiledSystem ownership snapshots
 -> CompiledRuleView(key + resolvedView + ordered rules)
 -> canonical ModelPaths shared across consumers
 -> exact access rules
 -> ModelAccessPolicyIndex
 -> SemanticDigestInput
 -> DigestBoundCompiledInput
 -> CompiledModelSet.published
 -> EngineContext
```

All nodes above are one candidate revision. No ownership/policy rebuild after digest. Any ERROR leaves old Context unchanged.

System `declaredVersion` is optional. Source semantic digest + schema compatibility identity are mandatory. No artificial version value is created when source does not declare one.

## 3. Ownership architecture

System ownership is not a side table independent from CompiledModelSet. It is a deterministic immutable view over the same final compiled facts.

Rules:
- every System-owned fact appears in exactly one owner snapshot;
- every snapshot key resolves to a current compiled fact;
- RuleView owner and `resolvedViewKey` ownership must be compatible;
- snapshot/version changes are semantic changes and enter digest;
- Contexts do not share mutable ownership sets.

## 4. Shared ModelPath architecture

One compiler service canonicalizes paths for `RULE`, `CHANGE`, `QUERY_CONTRACT`, `MODEL_ACCESS`. Consumer kind is provenance only.

P6 Query execution is not introduced in P2; only the stable compile/IR contract is frozen. This avoids two path interpreters later.

`read path="*"` exists only before finite compile-time expansion. Published rules contain exact paths.

## 5. Protected-access architecture

<a id="3-动态权限边界"></a>

```text
business/future consumer
 -> ProtectedExecutionBridge.execute(ruleKey, op, frame, owner, cursor)
 -> starter-internal issuance
 -> starter-internal target resolution
 -> one-shot capability(actual target + op + context + plan/proof)
 -> Gateway
 -> Guard
 -> exact current PolicyIndex lookup once
 -> STATIC_ALLOW or runtime proof
 -> bound protected operation
```

Direct rule/op selection follows persistent Decision; no token layer exists.

### No-legal-bypass architecture

P2 no-bypass is defined structurally:
- no public issued-pair/capability mint;
- no public post-Guard operation API;
- no business-accessible secondary policy registry;
- operation adapter is wired by composition but not exposed to business consumer through Context/Bridge getters;
- legacy adapters are read-only;
- current Context is the only policy authority.

Concrete P3/P4/P6 integrations are downstream acceptance obligations, not hidden P2 implementation work.

## 6. Runtime denial observability

Guard/Gateway return stable denial classification with System, optional RuleView provenance, exact operation/path and policy SourceRef. Runtime data values are excluded. Repeating the same denial against the same immutable Context must produce stable classification/provenance.

## 7. Operation independence

Policy lookup treats operation as part of exact authority. READ permission cannot authorize WRITE/EXECUTE and vice versa. There is no path-level `hasAnyPermission` shortcut.

## 8. Concurrency

- compilation candidates are session-local;
- publication is atomic;
- EngineContext and ownership snapshots are immutable;
- bridge calls are independent invocations;
- same one-shot capability concurrent terminal success <= 1;
- target/operation cannot change after capability mint;
- no token/claim/replay state exists in P2.

## 9. Migration architecture

<a id="4-迁移架构"></a>

P2 keeps only surviving read-only declaration/System compatibility. `dec-expand-declaration` remains retired. P7 owns final deletion/resource/runtime convergence.

## 10. Architecture gate

Architecture content is candidate-only. Exact Architecture/Impact/CrossModule/Concurrency reviews and machine risk detection remain required before phase progression.
