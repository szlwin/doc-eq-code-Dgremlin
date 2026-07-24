# P0 Reactor 模块依赖图

```text
dec-core-context
├─ dec-context-config-parse-xml
├─ dec-context-config-parse-yaml
├─ dec-datasource-orm-sql -> dec-core-datasource
│  └─ dec-datasource-orm-mysql
├─ dec-core-model -> dec-core-datasource + dec-datasource-orm-sql
│  ├─ dec-core-starter -> XML/YAML parsers
│  └─ dec-expand-declaration
└─ dec-demo -> starter + mysql adapter + declaration + YAML parser
```
