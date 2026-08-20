# P2 Security Authority Remediation R32 Change Note

Revision: `DESIGN-P2-R32`.

This change note records the design delta caused by `P2-CR-001(P0)` and `P2-CR-002(P1)` on exact code revision `7925ec4f218c167240fc12571336244e1f7849ad`.

- Requirement/BM remain unchanged.
- READ/WRITE only; EXECUTE=N/A.
- Guard becomes the only authority mint for executable MODEL effects.
- raw MODEL operation-port acquisition is removed from supported production surface.
- exact runtime Context identity replaces structural plan equality for provenance ownership.
- Development must obtain real RED before production changes.
- Expected implementation footprint: CONTEXT/MODEL/STARTER and tests only.
