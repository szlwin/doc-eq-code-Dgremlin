# TASK-P1-T10 Testing Evidence R01

- Revision：`TESTING-P1-T10-R01@9e94bc68d9a8`
- Status：`PASSED`
- Clean-code Head：`9e94bc68d9a8c25351213bb46a6cafa5702105d9`
- P0 Run：`30888758375`
- Artifact：`8884155225`
- Artifact size：`2014703`
- GitHub digest：`sha256:f7dbad60dd352535113f7a8fa74f85a475e7cc3bf40dc9aa29acdc074f11fb24`
- Independent ZIP SHA-256：`f7dbad60dd352535113f7a8fa74f85a475e7cc3bf40dc9aa29acdc074f11fb24`
- Digest match：`true`

## TDD history

### Valid RED

- Head：`f1ff4c03ece86a4c65eee5531ac6e1c89cbe8d3d`
- P0 Run：`30885614810`
- Artifact：`8882952382`
- SHA-256：`fc3259c077bff4ded98e0f700a9462658ad9de62191502495337e891845b0f44`
- Result：`17 failures / 0 errors`
- Java release 8 test compilation：PASSED
- Existing normal tests：`351/351`

### Architecture Skeleton

- Skeleton Head：`6db11965ec79a721a65a75532dabc812f16cc236`
- Connector trigger Head：`617664fc149586c3586873ac20de94e635c8b5c1`
- P0 Run：`30886407036`
- Artifact：`8883253634`
- SHA-256：`b65dbe96019fe16c5a4a7fd43b30b1aa35564e9b65273c51335dc2a2b11d3655`
- Result：`14 controlled failures / 0 errors`

### First GREEN

- Head：`411077d82d1b70a799f1ca6db643b937aa281ce5`
- P0 Run：`30887324648`
- Artifact：`8883611225`
- SHA-256：`bbf945eb94dec2388277bb1a2cfbb8ee0d694186232e1169029a2f7623a3f2ed`
- Original T10 Oracle：`17/17`

### Independent Review GREEN

- Review fix code Head：`6a4c6aaead711c199181115b3c6bb8e615cbdbaa`
- Trusted trigger Head：`6cb5977d0c004c8825a3b3b8aca8d52daa7f55d7`
- P0 Run：`30888668771`
- Artifact：`8884115300`
- SHA-256：`a0847a69fd109ab0a6ceae739b4fa357852dfdede393e49ef34c0679fa36349a`
- Independent Review Oracle：`7/7`

## Clean-code Surefire independent parsing

- Surefire XML：`73`
- Total XML tests：`376`，其中 1 项为故意失败门禁
- Normal tests：`375/375`
- T10：`24/24`
- T09：`36/36`
- Symbol：`66/66`
- Compiler module：`255/255`
- XML：`30/30`
- YAML：`59/59`
- Context normal：`26/26`
- Demo：`4/4`
- Legacy declaration：`1/1`
- Intentional failure gate：`1 expected failure recognized`
- Reactor：`12 modules / PASSED`
- Java release 8：`PASSED`
- MySQL：`SKIPPED_NOT_APPLICABLE`

临时 source workflow 已在 clean-code Head 前删除；最终测试未依赖临时 workflow。
