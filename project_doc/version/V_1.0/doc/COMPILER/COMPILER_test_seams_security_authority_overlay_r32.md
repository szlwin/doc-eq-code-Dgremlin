# COMPILER P2 Test Seams — Security Authority Overlay R32

> Design revision `DESIGN-P2-R32`.

TestDesign may rely on these stable observables:

1. package/external public-surface compilation or reflection scan proving whether raw `RuntimeModelOperationPort` is publicly obtainable;
2. model effect counter / receipt plus pre/post model value/version for zero-side-effect denial;
3. exact context/scope composition result and `PROVENANCE_MISMATCH` code;
4. same-plan contexts constructed with distinct EngineContext instances and differing policy; additionally, structurally identical plan/policy/digest contexts must still be identity-isolated;
5. existing guarded READ/WRITE success receipts, default-DENY codes, mutation-stamp failure, one-shot/replay behavior and Container rollback observables.

The TestDesign must not treat exception-message text, object `toString()`, plan hash or policy digest as authority identity.
