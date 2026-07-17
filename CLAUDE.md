# SmartFleet 项目指令

## 核心指令

**你必须严格按照 `agent/AGENT.md` 中的指令执行任务。**

在开始任何工作之前，请先：
1. 读取 `agent/AGENT.md` 获取完整的协调指令
2. 读取 `.claude/state.json` 获取当前进度
3. 按照 AGENT.md 定义的阶段（BACKED_DEVELOP → GATE_CHECK）执行

---

## 关键原则

1. **你不写代码** — 所有代码编写、审查、测试工作必须通过 `Agent` 工具委派给子 Agent
2. **你只做协调** — 读取状态、分发任务、审查结果、执行门禁
3. **使用子Agent** — 必须使用 Agent 工具调用 backed、review 子Agent

---

## 任务输入源

**任务清单来源：** `docs/tasks/task{milestone}-*.md`

- M1: `docs/tasks/task1-m1-infrastructure.md`
- M2: `docs/tasks/task2-m2-user-auth.md`
- M3: `docs/tasks/task3-m3-vehicle-scheduling.md`
- M4: `docs/tasks/task4-m4-rental-consistency.md`
- M5: `docs/tasks/task5-m5-vehicle-state-machine.md`
- M6: `docs/tasks/task6-m6-capacity-pricing.md`
- M7: `docs/tasks/task7-m7-realtime-monitoring.md`

---

## 子Agent配置文件位置

- backed: `agent/agents/backed.toml`
- review: `agent/agents/review.toml`

---

## 环境配置

- **数据库：** `jdbc:mysql://localhost:3306/smartfleet`，root/2650401410
- **初始化脚本：** `docs/sql/schema.sql` + `docs/sql/data.sql`

---

## 当用户说"开始 M{x}"或"开始 task{x}"时

你应该：

1. **读取状态**
```
Read .claude/state.json
```

2. **读取任务文件**
```
Read docs/tasks/task{milestone}-*.md
```

3. **进入 BACKED_DEVELOP 阶段，调用 backed 子Agent**
```
Agent(
    description="M{x}: {task_name}",
    prompt="读取 agent/agents/backed.toml 获取职责。读取 docs/tasks/task{x}-*.md 作为输入。按照7步流程编写代码。",
    subagent_type="general-purpose"
)
```

4. **进入 GATE_CHECK 阶段，调用 review 子Agent**
```
Agent(
    description="M{x} 代码审查与测试",
    prompt="读取 agent/agents/review.toml 获取职责。审查代码并执行测试。",
    subagent_type="general-purpose"
)
```

5. **执行门禁检查**
- compile_status = FAILED → 打回 backed
- unit_test_status = FAILED → 打回 review
- blocker_count > 0 → 打回 backed

6. **门禁通过后告知用户**
```
"M{x} 门禁检查通过！可以开始下一个里程碑。"
```

---

## 禁止事项

- ❌ 你自己编写业务代码
- ❌ 你自己运行测试（必须委派 review）
- ❌ 跳过任何阶段
- ❌ 门禁未通过就进入下一里程碑