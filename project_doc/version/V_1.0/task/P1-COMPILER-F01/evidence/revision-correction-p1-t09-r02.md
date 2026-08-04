# EVD-000646 — R27 Revision Evidence Correction

- Task: `TASK-P1-T09 / I002`
- Finding: `FND-P1-T09-I002-003`
- Historical invalid binding: `4483ce64c6ecffc989e3adcbd3a8178d301cace9`
- Historical source: R01 Completion and Revision Lock; preserved unchanged as invalid history
- Correct first commit: `e7713c4499271b79b958d0c0e0793c02e6be5428`
- Commit message: `docs(t09): freeze information expression plan`
- R27 blob at correct first commit: `20a16d1e7b199088086f496fe94aeb8b8684d8ca`
- Valid R01 RED: `404105e894853b36b0788ed40ac65d23d6ee8899`
- Relation: correct first commit is an ancestor of valid RED and precedes it by 7 commits
- Recovery source: full Git history Artifact `8881368845`
- Recovery Artifact SHA-256: `7dfcf97b4bbbb5ea9a1f8bbe329cc39f402d8daf3e608b62ffcc6f23542cf19f`
- Status: `CORRECTED / VERIFIED`

## Verification

1. GitHub/local full-history commit lookup resolves `e7713c449927...` successfully.
2. The commit adds only `DEC_COMPILER_implementation_plan_R27_t09_information_expression.md`.
3. `git rev-parse e7713c4:<plan-path>` returns the exact final blob `20a16d1e...8dca`.
4. `git merge-base --is-ancestor e7713c4 404105e8` succeeds.
5. The originally recorded `4483ce64...` cannot be resolved and is never reused as valid Evidence.

R01 files are not overwritten. R02 Completion references this correction record as the authoritative R27 Revision Integrity evidence.
