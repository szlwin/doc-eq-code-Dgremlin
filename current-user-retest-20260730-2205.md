# MySQL CURRENT_USER retest

This temporary marker validates the latest `.github/workflows/verify-and-open-pr.yml` from `dev_all` after replacing the conflicting alias with `SELECT CURRENT_USER();`.

Verified workflow blob before this commit: `9c7bcbb4185e0d446e9f22d026bb075fac4e0847`.

This file does not affect application runtime, Maven compilation, or test behavior.
