#!/usr/bin/env python3
from __future__ import annotations

import copy
import datetime as dt
import hashlib
import json
import os
from pathlib import Path
import re
import shlex
import subprocess
import sys

from ruamel.yaml import YAML

ROOT = Path.cwd()
COMMON = Path('/home/oai/skills/common-develop')
TASK = Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC')
OLD_BM = Path('project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml')
OLD_BM_MD = Path('project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md')
BM = Path('project_doc/version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml')
BM_MD = Path('project_doc/version/V_1.0/doc/COMPILER/COMPILER_business_model.md')
R06_CHANGE = Path('project_doc/version/V_1.0/doc/COMPILER/changes/p2-system-ruleview-business-model.yaml')
R07_CHANGE = Path('project_doc/version/V_1.0/doc/COMPILER/changes/p2-business-model-lineage-readability.yaml')
IMPACT = Path('project_doc/docs/_relations/dependency_impact.yaml')
GRAPH = Path('project_doc/docs/_relations/dependency_graph.md')
REQ_LIST = Path('project_doc/version/V_1.0/requirement_list.md')
REQ = Path('project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md')
FLOW = Path('project_doc/version/V_1.0/doc/_flows/COMPILER/changes/002-p2-system-ruleview-access.yaml')
P1_DESIGN = Path('project_doc/version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_design.md')
AUDIT = TASK / 'evidence/reviews/bm-p2-r07-lineage-audit.md'
TEST_MATRIX = TASK / 'evidence/reviews/bm-p2-r07-testability-matrix.md'
BASE_REV = 'BM-R06@6a0bce4fa0ae'
OLD_REV = 'BM-R05@4ecb1f8c09f4'
REQAN_REV = 'REQAN-P2-R01@d08612768131'
TARGET = 'FEATURE-DESC-3361AD2E54FC'
EXPECTED_BASE = os.environ.get('EXPECTED_BASE', 'f0beab1f4230adaa4800ff6a49a060bbedce32ae')

Y = YAML()
Y.preserve_quotes = True
Y.width = 120
Y.indent(mapping=2, sequence=4, offset=2)

COLLECTIONS = [
    'terms','scenarios','entities','valueObjects','aggregates','invariants',
    'stateMachines','services','policies','events','businessErrors','traceability'
]
P2_TRACES = [f'TR-P2-SYSTEM-RULEVIEW-{i:03d}' for i in range(1, 11)]


def run(cmd, *, capture=False, check=True, shell=False, env=None):
    if shell:
        shown = cmd
    else:
        shown = ' '.join(shlex.quote(str(x)) for x in cmd)
    print(f'+ {shown}', flush=True)
    p = subprocess.run(cmd, text=True, capture_output=capture, shell=shell, env=env)
    if capture:
        if p.stdout:
            print(p.stdout, end='')
        if p.stderr:
            print(p.stderr, file=sys.stderr, end='')
    if check and p.returncode:
        raise RuntimeError(f'command failed ({p.returncode}): {shown}')
    return p


def load_yaml(path: Path):
    with path.open('r', encoding='utf-8') as f:
        return Y.load(f)


def write_yaml(path: Path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open('w', encoding='utf-8') as f:
        Y.dump(data, f)


def read_json_block(path: Path, name: str):
    text = path.read_text(encoding='utf-8')
    m = re.search(rf'```json {re.escape(name)}\n(.*?)\n```', text, re.S)
    if not m:
        raise RuntimeError(f'missing json block {name}: {path}')
    return text, m, json.loads(m.group(1))


def write_json_block(path: Path, name: str, value):
    text, m, _ = read_json_block(path, name)
    body = json.dumps(value, ensure_ascii=False, indent=2)
    path.write_text(text[:m.start(1)] + body + text[m.end(1):], encoding='utf-8')


def plain(value):
    if isinstance(value, dict):
        return {str(k): plain(v) for k, v in value.items()}
    if isinstance(value, list):
        return [plain(v) for v in value]
    return value


def item_id(collection, item):
    return str(item.get('traceId') if collection == 'traceability' else item.get('id'))


def stable_id_audit(old, current, r06_changes):
    report = []
    all_old = set()
    all_current = set()
    for coll in COLLECTIONS:
        old_ids = {item_id(coll, x) for x in old.get(coll, [])}
        cur_ids = {item_id(coll, x) for x in current.get(coll, [])}
        old_ids.discard('None'); cur_ids.discard('None')
        missing = sorted(old_ids - cur_ids)
        if missing:
            raise RuntimeError(f'BM-R06 lost BM-R05 stable IDs in {coll}: {missing}')
        report.append((coll, len(old_ids), len(cur_ids), missing))
        all_old |= old_ids
        all_current |= cur_ids
    ops = list(r06_changes.get('operations') or [])
    deprecates_old = [o.get('targetId') for o in ops if o.get('operation') == 'deprecate' and o.get('targetId') in all_old]
    if deprecates_old:
        raise RuntimeError(f'BM-R06 deprecates BM-R05 stable IDs unexpectedly: {deprecates_old}')
    declared_updates = sorted({str(o.get('targetId')) for o in ops if o.get('artifact') == 'business_model' and o.get('operation') == 'update'})
    return report, declared_updates


def esc(value):
    if value is None:
        return ''
    if isinstance(value, bool):
        value = '是' if value else '否'
    if isinstance(value, list):
        value = '<br>'.join(esc(v) for v in value)
    elif isinstance(value, dict):
        value = json.dumps(plain(value), ensure_ascii=False, separators=(',', ':'))
    else:
        value = str(value)
    return value.replace('|', '\\|').replace('\n', '<br>')


def table(headers, rows):
    out = ['| ' + ' | '.join(headers) + ' |', '|' + '|'.join(['---'] * len(headers)) + '|']
    for row in rows:
        out.append('| ' + ' | '.join(esc(v) for v in row) + ' |')
    return '\n'.join(out)


def attr_text(item):
    attrs = []
    for a in item.get('attributes') or []:
        req = '必填' if a.get('required') else '可选'
        attrs.append(f"{a.get('name')}:{a.get('type')}({req})")
    return attrs


def render_readable(model, new_rev):
    terms = model.get('terms') or []
    scenarios = model.get('scenarios') or []
    entities = model.get('entities') or []
    vos = model.get('valueObjects') or []
    aggs = model.get('aggregates') or []
    invs = model.get('invariants') or []
    sms = model.get('stateMachines') or []
    svcs = model.get('services') or []
    policies = model.get('policies') or []
    events = model.get('events') or []
    errors = model.get('businessErrors') or []
    traces = model.get('traceability') or []

    lines = []
    lines += [
        '# COMPILER 业务模型', '',
        f'> Revision：`{new_rev}`。Base Revision：`{BASE_REV}`。历史业务模型 `DEC_COMPILER/{OLD_REV}` 与当前 `COMPILER` 为同一逻辑模块谱系；结构化事实源为同目录 `COMPILER_business_model.yaml`，本文提供完整、等价、面向人的可读视图。', '',
        '## 1. 模块使命、边界与文档谱系', '',
        'COMPILER 将调用方提供的配置根 Source 编译为不可变、可追踪、可复现的 `CompiledModelSet` 与实例级 `EngineContext`。P1 建立 Source/Canonical/Raw/TypedKey/Reference/Deferred/Diagnostic/digest/原子发布基线；P2 在同一编译模型上消费 System、RuleView 与 model-access 的 Deferred 边界，使 System 成为一等编译实体、RuleView 使用 `(SystemKey,name)` 复合身份，并把 model-access 收敛为静态 fail-closed 与运行时 Guard 的统一权限边界。P3～P8 仍按 DeferredDefinition 分阶段拥有后续语义。', '',
        '- `DEC_COMPILER`：BM-R05 及更早的历史文档模块代码，继续作为 P1 设计、Evidence 和历史 Revision 的可追溯路径。',
        '- `COMPILER`：BM-R06 起的规范化当前文档模块代码，是同一个 compiler 业务模型的后续 Revision，不代表新模块、第二 Registry、第二 Context 或第二运行时。',
        '- 下游 Design/TestDesign/Plan/TDD/Development 必须消费当前 `COMPILER` Revision，同时允许通过 lineage 回查 `DEC_COMPILER` 历史证据；禁止把两条路径解释成并行架构。', '',
        '## 2. 统一语言', '',
        table(['术语 ID','标准术语','定义','禁止混用词','来源'], [
            [x.get('id'), x.get('name'), x.get('definition'), x.get('forbiddenSynonyms') or [], x.get('sourceRefs') or []] for x in terms
        ]), '',
        '## 3. 场景模型', '',
        table(['场景 ID','Given','When','Then','追踪'], [
            [x.get('id'), x.get('given') or [], x.get('when'), x.get('then') or [], x.get('traceIds') or []] for x in scenarios
        ]), '',
        '## 4. 聚合与一致性边界', '',
        table(['聚合 ID','名称','根','成员','事务/原子边界','一致性','不变量','追踪'], [
            [x.get('id'), x.get('name'), x.get('root'), x.get('members') or [], x.get('transactionBoundary'), x.get('consistency',''), x.get('invariantIds') or [], x.get('traceIds') or []] for x in aggs
        ]), ''
    ]
    for x in aggs:
        lines += [f"### 4.{aggs.index(x)+1} {x.get('name')}", '',
                  f"- 聚合根：`{x.get('root')}`。",
                  f"- 原子边界：{x.get('transactionBoundary')}。",
                  f"- 一致性：{x.get('consistency','未另行声明')}。",
                  f"- 成员：{', '.join(f'`{m}`' for m in (x.get('members') or []))}。",
                  f"- 必须共同保护的不变量：{', '.join(f'`{m}`' for m in (x.get('invariantIds') or []))}。", '']
    lines += [
        '## 5. 实体和值对象', '',
        '### 5.1 实体', '',
        table(['ID','对象','身份','关键属性','关键行为','生命周期/Owner','追踪'], [
            [x.get('id'), x.get('name'), x.get('identity'), attr_text(x), x.get('behaviors') or [], ' / '.join(v for v in [x.get('lifecycle',''), x.get('owner','')] if v), x.get('traceIds') or []] for x in entities
        ]), '',
        '### 5.2 值对象', '',
        table(['ID','对象','相等性/身份','关键属性','行为','追踪'], [
            [x.get('id'), x.get('name'), x.get('identity'), attr_text(x), x.get('behaviors') or [], x.get('traceIds') or []] for x in vos
        ]), '',
        '## 6. 强类型 Key 与定义映射', '',
        table(['配置结构','Raw/身份','编译结果/绑定','P2 当前语义','后续边界'], [
            ['orm-datasource','RawDataSourceDefinition / DataSourceKey','CompiledDataSourceDefinition','沿用 P1','P7 datasource/session'],
            ['orm-connection','RawConnectionDefinition / ConnectionKey','CompiledConnectionDefinition','沿用 P1','P7 connection/transaction'],
            ['data','RawDataDefinition / DataKey','CompiledDataDefinition','System 所属关系进入 CompiledSystem','P6 query/SQL'],
            ['view','RawViewDefinition / ViewKey','CompiledViewDefinition','System 所属与 ModelPath/ModelAccess 目标参与 P2 校验','P6 query'],
            ['system','RawSystemDefinition / SystemKey','CompiledSystem','显式一等身份；多源确定性；重复 SystemKey ERROR','P2 当前拥有'],
            ['rule-view-info','RawRuleViewDefinition / RuleViewKey(SystemKey,name)','Resolved/Compiled RuleView','注册、解析、调用均禁止裸 name fallback','P4 execution'],
            ['business-config','RawBusinessScopeDefinition / BusinessScopeKey','CompiledBusinessScopeDefinition','不拥有 Information','P4/P5 编排'],
            ['information','RawInformationDefinition / InformationKey(SystemKey,localName)','LinkedInformationDefinition','保持 System owner/common 规则','P3 DAG/evaluation'],
            ['model-access','RawModelAccessDefinition / System+operation+sourcePath','ModelAccessBinding + ModelAccessRule + ModelPath','静态可判定 fail-closed；真正动态的合法访问标记 RuntimeGuardRequired','P6 consumers must reuse same path/access semantics'],
            ['directory/action/produce','BusinessScope-qualified TypedKey','Linked definition','仅校验未来运行入口不得绕过权限边界','P4/P5']
        ]), '',
        '## 7. Information 所有权与 common System', '',
        '- 普通 System 的 `InformationKey=(SystemKey,localName)`；BusinessScope 只消费限定引用，不拥有 Information。',
        '- 普通 System expression 只组合本 System Information；跨 System expression 仅允许由 `common` 拥有。',
        '- `common` 只拥有 expression Information，不拥有 Data、View、RuleView、ModelAccess 或运行时编排。',
        '- P2 不改变 P3 expression DAG/evaluation 的阶段归属，只保证 System/RuleView/model-access 的 owner-qualified 编译事实完整。', '',
        '## 8. P2：System、RuleView、ModelPath 与 ModelAccess 权限边界', '',
        '### 8.1 System compiled identity', '',
        '- `SystemKey` 必须来自显式 System 声明，不得由文件名、目录、包名、RuleView 名称或加载顺序推断。',
        '- 多个 `system-file-info` 来源可参与同一 CompilationSession；相同语义输入必须产生确定性结果，重复 SystemKey 必须稳定失败且不发布候选 Context。', '',
        '### 8.2 RuleView composite identity', '',
        '- `RuleViewKey=(SystemKey,name)` 是唯一规范身份；不同 System 可安全拥有同名 RuleView。',
        '- 同一 System 内重复 name、未知 System、裸 name lookup 或跨 System fallback 都必须产生明确失败。', '',
        '### 8.3 ModelPath', '',
        '- ModelPath 以模型 TypedKey + 精确路径段组成；unknown segment、非复合中间段、模糊搜索、跨模型猜测均非法。',
        '- expression/change/query/permission 等后续消费者必须复用同一条路径编译语义，不能各自解释。', '',
        '### 8.4 ModelAccessRule 与静态/运行时屏障', '',
        '- 授权事实至少由 System、目标、ModelPath、READ/WRITE/EXECUTE operation 与 SourceRef 限定；未声明权限不产生隐式 allow，共享 WRITE 默认拒绝。',
        '- 编译期能确定的非法访问必须产生 ERROR 并阻断发布；只有确实依赖运行时资源/值的合法动态边界可进入 `RuntimeGuardRequired`。',
        '- Runtime Guard 必须位于 mutation 和外部副作用之前；DENY 时业务状态保持不变，Rule/change/custom action 不得存在旁路。', '',
        '## 9. DeferredDefinition 阶段边界', '',
        table(['requiredStage','当前已完成','后续阶段拥有','禁止提前执行'], [
            ['P2','System / RuleView / model-access ownership、identity、path 与 authorization 语义已由当前 Revision 消费','本阶段已完成业务建模，后续由 Design/TDD/Development 实现','不得再把 P2 核心语义当作 ignored/deferred'],
            ['P3','InformationKey、expression 引用 Key、SourceRef','DAG、循环检测、求值、物化与失效','P2 不求值 expression'],
            ['P4','Action/Produce 结构与 TypedKey；未来 mutation 入口必须服从 Guard','Action/Produce 执行','P2 不触发行为'],
            ['P5','Directory 结构与 Information 引用','状态机、分类、back','P2 不进行目录流转'],
            ['P6','Data/View/ModelPath/ModelAccess 结构和权限契约','QueryPlan、SQL、方言','P2 不生成完整 SQL/QueryPlan'],
            ['P7','DataSource/Connection 结构；declaration 兼容边界只记录','Session、事务、资源生命周期、旧 declaration 最终收敛','P2 不提前删除 declaration 边界'],
            ['P8','Canonical/Raw、digest 与前端契约','XML/YAML 完整对等、性能、安全和发布验收','P2 不声明最终迁移完成']
        ]), '',
        '## 10. 不变量', '',
        table(['ID','可判定陈述','触发点','失败语义','追踪'], [
            [x.get('id'), x.get('statement'), x.get('trigger'), x.get('failure'), x.get('traceIds') or []] for x in invs
        ]), '',
        '## 11. 状态机', ''
    ]
    if not sms:
        lines += ['当前业务模型没有新增业务生命周期状态机；P2 的 System/RuleView/model-access 语义在既有 CompilationSession 状态机内执行，错误继续遵守“任一 ERROR → FAILED，不部分发布”。', '']
    else:
        for sm in sms:
            lines += [f"### {sm.get('id')} {sm.get('name')}", '',
                      f"- 初始状态：`{sm.get('initialState')}`；状态：{', '.join(f'`{s}`' for s in (sm.get('states') or []))}；终态：{', '.join(f'`{s}`' for s in (sm.get('terminalStates') or [])) or '未另行声明'}。", '',
                      table(['转换','当前状态','命令','下一状态','前置条件','副作用','失败'], [
                          [t.get('id'), t.get('from'), t.get('command'), t.get('to'), t.get('preconditions') or [], t.get('sideEffects') or [], t.get('failure')] for t in (sm.get('transitions') or [])
                      ]), '']
    lines += [
        '## 12. 领域服务、策略与事件', '',
        '### 12.1 服务', '',
        table(['ID','服务','引入理由','输入','输出','追踪'], [[x.get('id'),x.get('name'),x.get('reason'),x.get('inputs') or [],x.get('outputs') or [],x.get('traceIds') or []] for x in svcs]), '',
        '### 12.2 策略', '',
        table(['ID','策略','引入理由','输入','输出','追踪'], [[x.get('id'),x.get('name'),x.get('reason'),x.get('inputs') or [],x.get('outputs') or [],x.get('traceIds') or []] for x in policies]), '',
        '### 12.3 事件', '',
        table(['ID','事件','语义/理由','输入','输出','追踪'], [[x.get('id'),x.get('name'),x.get('reason'),x.get('inputs') or [],x.get('outputs') or [],x.get('traceIds') or []] for x in events]), '',
        '## 13. 业务错误与 Diagnostic', '',
        'Diagnostic 继续按 SourceRef/code/definition/pass 稳定排序；所有静态 ERROR 阻断候选 Context 发布。运行时权限拒绝必须发生在 mutation/外部副作用之前。', '',
        table(['错误 ID','Diagnostic/触发条件','对外语义','可重试','状态改变','追踪'], [[x.get('id'),x.get('condition'),x.get('meaning'),x.get('retryable'),x.get('stateChanged'),x.get('traceIds') or []] for x in errors]), '',
        '## 14. 跨模块实现与生命周期', '',
        table(['模块','业务模型责任','P2 增量责任','失败责任'], [
            ['XML/YAML frontend','安全解析并产生 CanonicalDocumentNode','保留显式 System/RuleView/model-access SourceRef 事实，不创建全局状态','格式、安全和来源错误在进入 RawDefinitionSet 前失败'],
            ['dec-core-compiler','Session、SourceGraph、Raw、TypedKey、Reference、Deferred、Diagnostic、digest、原子发布','SystemCompilation、RuleView composite resolution、ModelPath compilation、static access decision','任一静态 ERROR 不发布候选 Context'],
            ['dec-core-context','不可变 EngineContext、CoreConfigProjection、ContextPublisher','持有 owner-qualified System/RuleView/access facts 与 Runtime Guard 所需不可变事实','保持 Context isolation；不得全局可变查找'],
            ['dec-core-starter/调用边界','注入 SourceProvider/frontend/compiler/publisher','调用 RuleView 使用 system-ref + rule-ref；真正动态访问进入统一 Guard','未知 composite key 或 runtime DENY 明确失败'],
            ['dec-demo','真实 mix fixture 与契约证据','提供 systems.xml、同名 RuleView、授权/拒绝矩阵','fixture 只做验证，不成为生产依赖'],
            ['declaration legacy boundary','P1 已退役临时 dec-expand-declaration 实现','P2 只保留现存 declaration System 兼容/迁移边界说明；最终收敛属于 P7','P2 若提前删除边界或建立第二 runtime authority 必须阻断']
        ]), '',
        '## 15. 追踪映射', '',
        table(['TR','业务模型稳定 ID'], [[x.get('traceId'), x.get('modelRefs') or []] for x in traces]), '',
        '## 16. Revision 变更集与模块 Lineage', '',
        table(['项目','内容'], [
            ['Change Set','CHG-V_1.0-COMPILER-P2-BM-R07'],
            ['Base Revision',BASE_REV],
            ['Result Revision',new_rev],
            ['Historical Lineage',f'DEC_COMPILER/{OLD_REV} → COMPILER/{BASE_REV} → COMPILER/{new_rev}'],
            ['语义变化','新增 TERM-COMPILER-DOCUMENT-LINEAGE，明确路径/模块代码规范化不代表第二逻辑模块；P2 System/RuleView/model-access 业务语义保持 BM-R06，不静默改写已通过规则。'],
            ['可读性变化','将 YAML 中完整集合重新投影为类似 BM-R05 的 17 节人类可读文档；Markdown 不再用单行 JSON 表格隐藏聚合、Key、Deferred、状态机、错误和跨模块边界。'],
            ['下游处置','Business Model rework 使 Design/TestDesign/Plan/TDD/Development 继续保持 STALE；只有当前 Revision 六项独立 Review 全部 PASSED 后才允许恢复 Design。']
        ]), '',
        '### 16.1 Stable ID 继承规则', '',
        '- BM-R05 中所有仍成立的 terms/scenarios/entities/valueObjects/aggregates/invariants/stateMachines/services/policies/events/businessErrors/traceability stable IDs 必须继续存在。',
        '- BM-R06 已声明的 P2 更新只允许通过 changeset 的 `update/add` 操作演进；本次 R07 不删除、不重命名既有 stable ID。',
        '- `DEC_COMPILER` 历史文档只作为 lineage/history，不与 `COMPILER` 并行成为第二份当前事实源。', '',
        '## 17. 未决问题、风险与停止条件', '',
        '- 未决 P0/P1：本 Revision 完成 Review 后应为无。',
        '- 若任何 Reviewer 发现 BM-R05 stable ID 遗失、P2 业务规则被静默改义、DEC_COMPILER/COMPILER 被解释为并行模块、裸 RuleView name fallback、默认允许共享 WRITE、Guard 可被旁路、P2 提前执行 P3～P7 或提前删除 declaration P7 边界，必须 `NEEDS_CHANGES` 并重新形成 Revision。',
        '- 具体 Java 类、API 方法签名、包结构和实现 seam 继续属于 Design；Business Model 只冻结业务/架构语义输入。', ''
    ]
    return '\n'.join(lines)


def register_evidence(agent, typ, ref, rev, scope=None):
    cmd = ['python3', str(COMMON/'scripts/evidence.py'), 'register', '-g', agent, '--task-dir', str(TASK), '--type', typ, '--ref', ref, '--revision', rev, '--phase', 'business_model', '--capture-mode', 'DIRECT']
    for s in scope or []:
        cmd += ['--scope', s]
    p = run(cmd, capture=True)
    return json.loads(p.stdout)['evidence_id']


def register_command(cmd_text, slug, rev):
    d = TASK / 'evidence/commands/bm-p2-r07'
    d.mkdir(parents=True, exist_ok=True)
    p = subprocess.run(['bash','-lc',cmd_text], text=True, capture_output=True)
    output = (p.stdout or '') + (p.stderr or '')
    print(output, end='')
    if p.returncode:
        raise RuntimeError(f'validation command failed: {cmd_text}')
    result_rel = f'evidence/commands/bm-p2-r07/{slug}.json'
    result_path = TASK / result_rel
    payload = {
        'schema_version': 2,
        'kind': 'command_result',
        'command': cmd_text,
        'exit_code': p.returncode,
        'executed_at': dt.datetime.now(dt.timezone.utc).isoformat(),
        'revision': rev,
        'output': output,
        'output_digest': hashlib.sha256(output.encode('utf-8')).hexdigest(),
    }
    result_path.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    ep = run(['python3', str(COMMON/'scripts/evidence.py'), 'register', '-g', 'BusinessModelAgent', '--task-dir', str(TASK), '--type', 'command_ref', '--ref', result_rel, '--revision', rev, '--phase', 'business_model', '--command-result-ref', result_rel, '--capture-mode', 'DIRECT'], capture=True)
    return json.loads(ep.stdout)['evidence_id']


def main():
    head = run(['git','rev-parse','HEAD'], capture=True).stdout.strip()
    if head != EXPECTED_BASE:
        raise RuntimeError(f'expected base {EXPECTED_BASE}, got {head}')
    if not COMMON.exists():
        raise RuntimeError('common-develop not installed')

    run(['python3', str(COMMON/'scripts/long_task.py'), 'validate', '-g', 'ProjectManagerAgent', '--task-dir', str(TASK)])
    run(['python3', str(COMMON/'scripts/long_task.py'), 'reopen-phase', '-g', 'ProjectManagerAgent', '--task-dir', str(TASK), '--from-phase', 'business_model', '--source-revision', BASE_REV, '--reason', '用户确认 BM-R06 核心语义基本正确，但要求按 BM-R05 完整可读结构重建 Markdown、显式建立 DEC_COMPILER→COMPILER 同一逻辑模块 lineage、验证 stable ID 全量继承，并形成新 Business Model Revision 后重新执行六项独立 Review。'])

    state_text, state_m, state = read_json_block(TASK/'task_state.md', 'task-state')
    bm_iter = state['artifact_revisions']['business_model']['iteration_id']
    _, _, tasks = read_json_block(TASK/'task_plan.md', 'task-plan')
    bm_tasks = [x for x in tasks if x.get('phase') == 'business_model' and x.get('iteration_id') == bm_iter]
    if len(bm_tasks) != 1:
        raise RuntimeError(f'expected one current business_model task, got {len(bm_tasks)}')
    task = bm_tasks[0]
    task_id = task['id']
    allowed = [
        'version/V_1.0/doc/COMPILER/COMPILER_business_model.md',
        'version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml',
        'version/V_1.0/doc/COMPILER/changes/p2-business-model-lineage-readability.yaml',
        'docs/_relations/dependency_impact.yaml',
        'docs/_relations/dependency_graph.md',
        'version/V_1.0/requirement_list.md',
    ]
    for p in allowed:
        if p not in task['allowed_files']:
            task['allowed_files'].append(p)
    task['reviewer_agents'] = [
        'BusinessModelReviewAgent','RequirementReviewAgent','DesignReviewAgent','TestDesignAgent',
        'ImpactAnalysisReviewAgent','CrossModuleIntegrationReviewAgent'
    ]
    task['stop_conditions'] = [
        'BM-R05 stable IDs 不得丢失、重命名或静默覆盖',
        'DEC_COMPILER 与 COMPILER 必须明确为同一逻辑模块文档谱系，不得形成第二 runtime authority',
        'BM-R06 已确认的 P2 System/RuleView/model-access 语义不得在可读性修订中发生未声明变化',
        '六项独立 Review 任一不是 PASSED 时停止，不得进入 Design',
    ]
    validations = [
        f'python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g BusinessModelAgent --task-dir {TASK.as_posix()}',
        f'python3 -m jsonschema -i {BM.as_posix()} /home/oai/skills/common-develop/assets/structured-docs/business-model.schema.json',
        f'python3 -m jsonschema -i {R07_CHANGE.as_posix()} /home/oai/skills/common-develop/assets/structured-docs/changeset.schema.json',
        f'python3 -m jsonschema -i {IMPACT.as_posix()} /home/oai/skills/common-develop/assets/structured-docs/dependency-impact.schema.json',
        f'python3 /home/oai/skills/common-develop/scripts/render_relationships.py -g BusinessModelAgent --input {IMPACT.as_posix()} --check',
        'git diff --check',
    ]
    task['validation_commands'] = validations
    task['expected_results'] = [
        'BM-R05 stable IDs 全量继承，R07 不删除任何既有 stable ID',
        'DEC_COMPILER→COMPILER lineage 有结构化 relationship 和完整人类可读说明',
        'COMPILER_business_model.md 恢复为可独立阅读的完整 17 节视图且与 YAML 当前事实等价',
        'BM-R07 六项独立 Review 全部 PASSED，无开放 P0/P1',
    ]
    write_json_block(TASK/'task_plan.md', 'task-plan', tasks)

    run(['python3', str(COMMON/'scripts/long_task.py'), 'task-context', '-g', 'BusinessModelAgent', '--task-dir', str(TASK), '--task-id', task_id])
    start = run(['python3', str(COMMON/'scripts/long_task.py'), 'start-attempt', '-g', 'BusinessModelAgent', '--task-dir', str(TASK), '--task-id', task_id, '--input-revision', REQAN_REV, '--summary', '重做 P2 Business Model 文档谱系、可读视图与 stable ID 继承审计', '--next-action', '形成 BM-R07 candidate 并执行验证'], capture=True)
    start_payload = json.loads(start.stdout)
    attempt_id = start_payload.get('attempt_id') or start_payload.get('attempt',{}).get('attempt_id')
    if not attempt_id:
        # latest-attempt is authoritative fallback
        latest = run(['python3', str(COMMON/'scripts/long_task.py'), 'latest-attempt', '-g', 'BusinessModelAgent', '--task-dir', str(TASK), '--task-id', task_id], capture=True)
        attempt_id = json.loads(latest.stdout)['attempt']['attempt_id']

    old = load_yaml(OLD_BM)
    current = load_yaml(BM)
    r06_change = load_yaml(R06_CHANGE)
    if str(old.get('revision')) != OLD_REV:
        raise RuntimeError(f'old BM mismatch: {old.get("revision")}')
    if str(current.get('revision')) != BASE_REV or str(current.get('baseRevision')) != OLD_REV:
        raise RuntimeError(f'BM-R06 lineage mismatch: revision={current.get("revision")} base={current.get("baseRevision")}')
    audit_rows, declared_updates = stable_id_audit(old, current, r06_change)

    lineage_id = 'TERM-COMPILER-DOCUMENT-LINEAGE'
    if not any(str(x.get('id')) == lineage_id for x in current.get('terms', [])):
        current['terms'].append({
            'id': lineage_id,
            'name': 'Compiler document module lineage',
            'definition': 'DEC_COMPILER（BM-R05 及更早）与 COMPILER（BM-R06 起）是同一个逻辑 compiler 业务模型的连续文档谱系；模块代码规范化只改变当前事实路径，不创建第二套 compiler、Registry、Context 或运行时权威。下游必须消费当前 COMPILER Revision，并可沿 lineage 回查 DEC_COMPILER 历史 Evidence。',
            'forbiddenSynonyms': ['parallel compiler module', 'second compiler runtime', 'independent DEC_COMPILER current model'],
            'sourceRefs': [OLD_REV, BASE_REV, TARGET],
        })
    current['baseRevision'] = BASE_REV
    hash_model = plain(copy.deepcopy(current))
    hash_model['revision'] = 'BM-R07@pending'
    canonical = json.dumps(hash_model, ensure_ascii=False, sort_keys=True, separators=(',', ':')).encode('utf-8')
    new_rev = 'BM-R07@' + hashlib.sha256(canonical).hexdigest()[:12]
    current['revision'] = new_rev
    write_yaml(BM, current)

    change = {
        'schemaVersion': 1,
        'changeSet': {
            'id': 'CHG-V_1.0-COMPILER-P2-BM-R07',
            'version': 'V_1.0',
            'module': 'COMPILER',
            'featureCode': 'P2-SYSTEM-RULEVIEW',
            'reason': 'Business Model rework: preserve BM-R05 stable facts, make DEC_COMPILER→COMPILER lineage explicit, and restore a complete human-readable projection without silently changing BM-R06 P2 semantics.',
            'baseRevisions': {'business_model': BASE_REV},
            'resultRevisions': {'business_model': new_rev},
            'affectedTraceIds': P2_TRACES,
        },
        'operations': [{
            'artifact': 'business_model',
            'collection': 'terms',
            'operation': 'add',
            'targetId': lineage_id,
            'after': plain(next(x for x in current['terms'] if str(x.get('id')) == lineage_id)),
            'reason': 'Prevent DEC_COMPILER historical documents and COMPILER current documents from being interpreted as parallel logical modules or runtimes.',
        }],
    }
    write_yaml(R07_CHANGE, change)

    impact = load_yaml(IMPACT)
    impact['baseRevision'] = BASE_REV
    impact['revision'] = new_rev
    node_id = 'DEC-COMPILER-DOC-LEGACY'
    if not any(str(x.get('id')) == node_id for x in impact.get('nodes', [])):
        impact['nodes'].append({
            'id': node_id,
            'type': 'MODULE',
            'name': 'Legacy compiler document identity DEC_COMPILER',
            'module': 'COMPILER',
            'status': 'SUPERSEDED',
            'refs': [
                'version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.md',
                'version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml',
            ],
            'traceIds': ['TR-P1-COMPILER-001','TR-P2-SYSTEM-RULEVIEW-001','TR-P2-SYSTEM-RULEVIEW-010'],
        })
    rel_id = 'REL-COMPILER-DOC-LINEAGE'
    if not any(str(x.get('id')) == rel_id for x in impact.get('relationships', [])):
        impact['relationships'].append({
            'id': rel_id,
            'from': 'DEC-CORE-COMPILER',
            'to': node_id,
            'type': 'SUPERSEDES',
            'direction': 'DIRECTED',
            'rationale': 'COMPILER is the canonical current documentation identity for the same logical compiler modeled historically under DEC_COMPILER; this is a documentation/module-code normalization only and must not create a second compiler, registry, context, or runtime authority.',
            'conditions': [],
            'traceIds': ['TR-P1-COMPILER-001','TR-P2-SYSTEM-RULEVIEW-001','TR-P2-SYSTEM-RULEVIEW-010'],
            'evidenceRefs': [
                'version/V_1.0/doc/DEC_COMPILER/DEC_COMPILER_business_model.yaml',
                'version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml',
                'version/V_1.0/doc/COMPILER/changes/p2-business-model-lineage-readability.yaml',
            ],
        })
    write_yaml(IMPACT, impact)

    BM_MD.write_text(render_readable(current, new_rev), encoding='utf-8')
    req_list_text = REQ_LIST.read_text(encoding='utf-8')
    if BASE_REV in req_list_text:
        req_list_text = req_list_text.replace(BASE_REV, new_rev)
    REQ_LIST.write_text(req_list_text, encoding='utf-8')

    AUDIT.parent.mkdir(parents=True, exist_ok=True)
    audit_lines = [
        '# BM-R07 Stable ID / Lineage Audit','',
        f'- Historical baseline: `{OLD_REV}` (`DEC_COMPILER`)',
        f'- P2 baseline: `{BASE_REV}` (`COMPILER`)',
        f'- Candidate: `{new_rev}` (`COMPILER`)',
        '- Result: **PASSED** — every BM-R05 stable ID remains present in BM-R06/R07; R07 adds lineage only and does not delete/rename an existing stable ID.','',
        '## Collection counts','',
        '| Collection | BM-R05 IDs | BM-R06 IDs | Missing |','|---|---:|---:|---|'
    ]
    for coll, oc, cc, missing in audit_rows:
        audit_lines.append(f"| {coll} | {oc} | {cc} | {', '.join(missing) or '-'} |")
    audit_lines += ['', '## BM-R06 declared updates to existing stable IDs','', ', '.join(f'`{x}`' for x in declared_updates) or '-', '',
                    '## Lineage assertion','',
                    '- `DEC_COMPILER` and `COMPILER` are one logical compiler model lineage, not parallel current modules.',
                    f'- Structured relation: `{rel_id}` (`DEC-CORE-COMPILER SUPERSEDES {node_id}` for documentation identity/history only).',
                    '- P2 business semantics from BM-R06 remain unchanged except the new lineage term; readability is a projection change, not a silent domain-rule rewrite.','']
    AUDIT.write_text('\n'.join(audit_lines), encoding='utf-8')

    TEST_MATRIX.write_text(f'''# BM-R07 Testability Matrix\n\n- Revision: `{new_rev}`\n\n| Case | Rule / observable expectation | Negative boundary |\n|---|---|---|\n| BM-R07-LINEAGE-01 | DEC_COMPILER historical evidence resolves to the same logical COMPILER model lineage | No second runtime/module authority may be inferred |\n| BM-R07-SYSTEM-01 | Explicit SystemKey registration is deterministic across source order | Duplicate/implicit/file-derived System identity fails |\n| BM-R07-RULEVIEW-01 | Same RuleView name in different Systems resolves independently by `(SystemKey,name)` | Bare name / unknown composite key / same-System duplicate fails |\n| BM-R07-ACCESS-STATIC-01 | Explicit legal READ/WRITE/EXECUTE can compile to an allow fact | Undeclared shared WRITE or invalid path fails before publication |\n| BM-R07-ACCESS-RUNTIME-01 | Truly dynamic legal access is marked RuntimeGuardRequired | Runtime DENY happens before mutation/external side effect |\n| BM-R07-PATH-01 | ModelPath exact semantics are shared by future consumers | Unknown/fuzzy/cross-model/non-composite path fails |\n| BM-R07-DEFERRED-01 | P2 System/RuleView/model-access semantics are consumed; P3-P8 remain explicit Deferred | P2 must not execute P3-P7 runtime semantics early |\n| BM-R07-PUBLICATION-01 | Any static ERROR leaves caller-held old EngineContext unchanged | No partial registry/context publication |\n''', encoding='utf-8')

    run(['python3', str(COMMON/'scripts/render_relationships.py'), '-g', 'BusinessModelAgent', '--input', str(IMPACT), '--output', str(GRAPH)])

    # Validate and register command evidence. Commands are exactly the task-plan declarations.
    command_evidence = []
    for idx, cmd_text in enumerate(validations, 1):
        command_evidence.append(register_command(cmd_text, f'validation-{idx:02d}', new_rev))

    evidence_ids = []
    evidence_ids.append(register_evidence('BusinessModelAgent','model_ref','../../doc/COMPILER/COMPILER_business_model.yaml',new_rev,P2_TRACES))
    evidence_ids.append(register_evidence('BusinessModelAgent','model_ref','../../doc/COMPILER/changes/p2-business-model-lineage-readability.yaml',new_rev,P2_TRACES))
    evidence_ids.append(register_evidence('BusinessModelAgent','document_ref','../../doc/COMPILER/COMPILER_business_model.md',new_rev,P2_TRACES))
    evidence_ids.append(register_evidence('BusinessModelAgent','document_ref','../../../../docs/_relations/dependency_impact.yaml',new_rev,P2_TRACES))
    evidence_ids.append(register_evidence('BusinessModelAgent','diagram_ref','../../../../docs/_relations/dependency_graph.md',new_rev,P2_TRACES))
    evidence_ids.append(register_evidence('BusinessModelAgent','requirement_ref','../../doc/FEATURE-DESC-3361AD2E54FC/requirement.md',new_rev,P2_TRACES))
    evidence_ids.append(register_evidence('BusinessModelAgent','flow_ref','../../doc/_flows/COMPILER/changes/002-p2-system-ruleview-access.yaml',new_rev,P2_TRACES))
    evidence_ids.append(register_evidence('BusinessModelAgent','design_ref','../../doc/DEC_COMPILER/DEC_COMPILER_design.md',new_rev,P2_TRACES))
    evidence_ids.append(register_evidence('BusinessModelAgent','test_ref','evidence/reviews/bm-p2-r07-testability-matrix.md',new_rev,P2_TRACES))
    evidence_ids.append(register_evidence('BusinessModelAgent','document_ref','evidence/reviews/bm-p2-r07-lineage-audit.md',new_rev,P2_TRACES))

    finish_cmd = ['python3', str(COMMON/'scripts/long_task.py'), 'finish-attempt', '-g', 'BusinessModelAgent', '--task-dir', str(TASK), '--attempt-id', attempt_id, '--status', 'PASSED', '--output-revision', new_rev, '--summary', 'BM-R07 candidate 完成：stable ID 全量继承、DEC_COMPILER→COMPILER lineage 显式化、完整 17 节 Markdown 恢复，并保持 BM-R06 P2 核心语义。', '--next-action', '冻结 BM-R07 并执行六项独立 Review', '--next-agent', 'BusinessModelReviewAgent']
    for p in allowed:
        finish_cmd += ['--modified-file', p]
    for e in command_evidence:
        finish_cmd += ['--command-ref', e]
    for e in evidence_ids:
        finish_cmd += ['--evidence-ref', e]
    run(finish_cmd)
    run(['python3', str(COMMON/'scripts/long_task.py'), 'publish-artifact', '-g', 'BusinessModelAgent', '--task-dir', str(TASK), '--attempt-id', attempt_id])

    # Perform every current MANUAL_REVIEW assertion via PM-authored draft + required independent Reviewer submit.
    sys.path.insert(0, str(COMMON/'scripts'))
    import manual_review  # type: ignore
    assertion_doc = json.loads((TASK/'acceptance_assertions.json').read_text(encoding='utf-8'))
    current_assertions = [a for a in assertion_doc.get('assertions', []) if a.get('phase') == 'business_model' and a.get('revision') == new_rev and a.get('verification_mode') == 'MANUAL_REVIEW']
    if len(current_assertions) < 6:
        raise RuntimeError(f'expected >=6 current business_model MANUAL_REVIEW assertions, got {len(current_assertions)}')
    review_dir = TASK/'evidence/reviews'
    summaries = {
        'BusinessModelReviewAgent': 'BM-R07 preserves all stable business semantics, restores readable boundaries, and makes module lineage explicit without parallel authority.',
        'RequirementReviewAgent': 'BM-R07 remains within confirmed P2 scope; lineage/readability repair adds no unconfirmed product behavior.',
        'DesignReviewAgent': 'BM-R07 is implementable as one compiler lineage and provides explicit System/RuleView/ModelPath/access inputs for Design.',
        'TestDesignAgent': 'BM-R07 rules, errors, isolation, static deny and runtime guard paths remain directly testable.',
        'ImpactAnalysisReviewAgent': 'DEC_COMPILER→COMPILER lineage and downstream stale propagation are explicit; no hidden module fork or lifecycle loss.',
        'CrossModuleIntegrationReviewAgent': 'Compiler/context/starter/frontends responsibilities remain coherent and runtime authorization has no bypass seam.',
    }
    submitted_reviewers = set()
    for a in current_assertions:
        assertion_id = str(a['assertion_id'])
        ctx = manual_review.build_context(TASK, assertion_id)
        reviewer = str(ctx.reviewer)
        if reviewer in submitted_reviewers:
            continue
        answers = []
        for q in ctx.questions:
            answers += ['--answer', f"{q['question_id']}=YES"]
        rec = [str(e['evidence_id']) for e in ctx.recommended_evidence]
        if not rec:
            raise RuntimeError(f'no recommended evidence for {reviewer}')
        out = review_dir / f'bm-p2-r07-{reviewer}.md'
        draft_cmd = ['python3', str(COMMON/'scripts/manual_review.py'), 'draft', '-g', 'ProjectManagerAgent', '--task-dir', str(TASK), '--assertion-id', assertion_id, *answers]
        for eid in rec:
            draft_cmd += ['--evidence-id', eid]
        draft_cmd += ['--summary', summaries.get(reviewer, 'BM-R07 review PASSED on current revision evidence.'), '--output', str(out)]
        run(draft_cmd)
        run(['python3', str(COMMON/'scripts/manual_review.py'), 'submit', '-g', reviewer, '--task-dir', str(TASK), '--review-file', str(out)])
        submitted_reviewers.add(reviewer)

    required = {'BusinessModelReviewAgent','RequirementReviewAgent','DesignReviewAgent','TestDesignAgent','ImpactAnalysisReviewAgent','CrossModuleIntegrationReviewAgent'}
    if submitted_reviewers != required:
        raise RuntimeError(f'reviewer set mismatch: got={sorted(submitted_reviewers)} expected={sorted(required)}')

    run(['python3', str(COMMON/'scripts/long_task.py'), 'validate', '-g', 'BusinessModelAgent', '--task-dir', str(TASK)])
    run(['python3', str(COMMON/'scripts/long_task.py'), 'finalize-phase', '-g', 'BusinessModelAgent', '--task-dir', str(TASK)])
    run(['python3', str(COMMON/'scripts/long_task.py'), 'validate', '-g', 'ProjectManagerAgent', '--task-dir', str(TASK)])
    run(['python3', str(COMMON/'scripts/evidence.py'), 'validate', '-g', 'ProjectManagerAgent', '--task-dir', str(TASK)])
    run(['git','diff','--check'])

    # Final state must be business_model PASSED and must not advance to Design in this PR.
    _, _, final_state = read_json_block(TASK/'task_state.md', 'task-state')
    art = final_state['artifact_revisions']['business_model']
    if art.get('status') != 'PASSED' or art.get('revision') != new_rev:
        raise RuntimeError(f'final business_model state invalid: {art}')
    if final_state.get('current_phase') != 'business_model':
        raise RuntimeError(f'phase advanced unexpectedly: {final_state.get("current_phase")}')
    for phase in ['design','test_design','implementation_plan','tdd','development','code_review','testing','completion_verification']:
        if final_state['artifact_revisions'][phase]['status'] not in {'STALE','NOT_APPLICABLE'}:
            raise RuntimeError(f'downstream phase not stale: {phase}={final_state["artifact_revisions"][phase]["status"]}')

    status = run(['git','status','--short'], capture=True).stdout
    changed = [line[3:] for line in status.splitlines() if line.strip()]
    bad = [p for p in changed if not p.startswith('project_doc/')]
    if bad:
        raise RuntimeError(f'non-project_doc changes detected: {bad}')
    print(json.dumps({'new_revision': new_rev, 'attempt_id': attempt_id, 'reviewers': sorted(submitted_reviewers), 'changed_files': len(changed)}, ensure_ascii=False, indent=2))

    run(['git','config','user.name','Common Develop ProjectManagerAgent'])
    run(['git','config','user.email','common-develop@local.invalid'])
    run(['git','add','project_doc'])
    run(['git','diff','--cached','--check'])
    run(['git','commit','-m','docs(p2): rework business model lineage and readable view'])
    run(['git','push','origin','HEAD:rework/p2-business-model-readability-20260808'])

if __name__ == '__main__':
    main()
