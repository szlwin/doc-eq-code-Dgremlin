# COMPILER P2 Test Seams

> Revision：`DESIGN-P2-R20`。Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

## Stable seams

- shared source View resolution: sourceModel -> existing ViewKey/SymbolTable -> TargetKey(ViewKey);
- policy identity: owner System separate from TargetKey/ModelPath/op;
- policy truth table validation at compiler and PolicyIndex construction;
- `WriteIntentResolver` 0/1/N result before Guard;
- immutable `ResolvedProtectedAccess` and one-shot capability;
- `RuntimeFactValue` closed domain and deterministic serialization;
- opaque runtime ID exact value semantics;
- production `RuntimeModelOperationPort` implemented by dec-core-model and wired only through starter production assembly.

## Production-vs-test rule

A controlled/fake RuntimeModelOperationPort may test Bridge/Guard sequencing, but cannot satisfy production reachability. The production integration test must acquire the normal starter composition and observe an actual `dec-core-model` object/path READ or WRITE mutation/receipt.

## Concurrency

Use latch/barrier to race the same capability. Exactly one operation may reach the production model adapter; no sleep-based oracle.

## Denial oracle

For policy/proof/intent/guard/capability failures: production operation invocation=0, mutation=0, readValue/writeReceipt absent, stable non-sensitive denial present.
