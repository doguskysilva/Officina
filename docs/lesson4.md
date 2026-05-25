# Módulo 4 — Múltiplos back stacks: histórico por aba

## O problema do Módulo 3

```kotlin
// Módulo 3 — ao trocar de aba, o histórico sumia
onTabSelected = { route ->
    backStack.clear()
    backStack.add(route)
}
```

Se o usuário estava em `ProjectDetail` na aba Projetos, foi para Tarefas e voltou, encontrava `ProjectList` do zero — sem o detalhe que havia aberto.

---

## A solução: um back stack por aba

Em vez de um único `rememberNavBackStack`, criamos um por aba:

```kotlin
val projectsBackStack = rememberNavBackStack(ProjectList)
val tasksBackStack    = rememberNavBackStack(TaskList)
val settingsBackStack = rememberNavBackStack(AppSettings)
```

Cada um é independente. Trocar de aba não toca no back stack das outras abas.

---

## selectedTab vira estado explícito

No Módulo 3, `selectedTab` era derivado do back stack via `derivedStateOf`:

```kotlin
// Módulo 3
val selectedTab by remember {
    derivedStateOf { backStack.firstOrNull { it in topLevelRoutes } ?: ProjectList }
}
```

No Módulo 4 não precisamos derivar nada — a aba selecionada é simplesmente um estado:

```kotlin
// Módulo 4
var selectedTab by remember { mutableStateOf<NavKey>(ProjectList) }
```

**Por que `remember` e não `rememberSaveable`?** `rememberSaveable` precisaria de um `Saver` customizado para `NavKey` (interface, não primitivo). Usar `remember` simples é suficiente: mesmo que o processo morra, os back stacks são restaurados por `rememberNavBackStack` (que usa `rememberSaveable` internamente com `@Serializable`), e o usuário volta à tela inicial da aba — comportamento razoável.

---

## activeBackStack — o back stack do momento

```kotlin
val activeBackStack = when (selectedTab) {
    ProjectList -> projectsBackStack
    TaskList    -> tasksBackStack
    else        -> settingsBackStack
}
```

`NavDisplay` e todas as lambdas de navegação usam `activeBackStack`:

```kotlin
NavDisplay(
    backStack = activeBackStack,
    onBack = { activeBackStack.removeLastOrNull() },
    ...
)
```

---

## onTabSelected simplificado

```kotlin
onTabSelected = { route ->
    selectedTab = route   // só isso — back stack da aba anterior continua intacto
}
```

Comparado ao Módulo 3 (`backStack.clear() + backStack.add()`), a troca de aba agora é uma operação de leitura, não de destruição.

---

## Fluxo completo com múltiplos back stacks

```
Usuário abre o app:
  selectedTab = ProjectList
  activeBackStack = [ProjectList]

Abre "App Mobile":
  activeBackStack = [ProjectList, ProjectDetail("App Mobile")]

Troca para aba Tarefas:
  selectedTab = TaskList
  activeBackStack = [TaskList]      ← back stack de Tarefas
  projectsBackStack intacto = [ProjectList, ProjectDetail("App Mobile")]

Volta para aba Projetos:
  selectedTab = ProjectList
  activeBackStack = [ProjectList, ProjectDetail("App Mobile")]  ← estado preservado ✓
```

---

## Bugs descobertos no tablet — e as correções

### Bug 1: system back saía do app no tablet

**Causa:** `ListDetailSceneStrategy` agrupa todos os entries visíveis numa única `Scene` com `previousEntries = []`. O `NavDisplay` usa `previousEntries` para decidir se habilita o back nativo — quando está vazio, o sistema interpreta como "nada para voltar" e sai do app.

**Correção:** `BackHandler` externo registrado _antes_ do `NavDisplay`. Como handlers compostos depois têm maior prioridade, o de dentro do `NavDisplay` vence quando habilitado (phone, panes separados). Quando o `NavDisplay` não consegue lidar (tablet, tudo na mesma Scene), o nosso entra como fallback:

```kotlin
// Antes do NavDisplay — fallback para o tablet multi-pane
BackHandler(enabled = activeBackStack.size > 1) {
    activeBackStack.removeLastOrNull()
}

NavDisplay(
    backStack = activeBackStack,
    onBack = { activeBackStack.removeLastOrNull() },
    ...
)
```

**Comportamento resultante no tablet:**

| Back stack | System back |
|---|---|
| `[ProjectList, ProjectDetail, NewTask]` | Fecha `NewTask` ✓ |
| `[ProjectList, ProjectDetail]` | Volta para lista + placeholder ✓ |
| `[ProjectList]` | Sai do app ✓ |

---

### Bug 2: `NewTask` ficava aberta ao trocar de projeto no tablet

**Causa:** ao clicar num novo projeto, o código fazia `removeIf { it is ProjectDetail }` mas deixava `NewTask` no back stack. No tablet, `ListDetailSceneStrategy` ainda exibia a `NewTask` do projeto anterior como extraPane do novo projeto.

```
Antes da correção:
  back stack: [ProjectList, ProjectDetail(A), NewTask(A)]
  clica em Projeto B
  removeIf { it is ProjectDetail } → remove ProjectDetail(A)
  add(ProjectDetail(B))
  resultado: [ProjectList, NewTask(A), ProjectDetail(B)]  ← NewTask errada visível
```

**Correção:** limpar também `NewTask` ao trocar de projeto:

```kotlin
onProjectClick = { route ->
    activeBackStack.removeIf { it is ProjectDetail || it is NewTask }
    activeBackStack.add(route)
}
```

---

## O que o Módulo 3 ensinou que ainda vale aqui

| Conceito do Módulo 3 | Módulo 4 |
|---|---|
| `SceneDecoratorStrategy` | Sem alterações — mesma interface |
| `derived key` na `NavDecoratorScene` | Sem alterações |
| `selectedTab` como parâmetro do decorator | Sem alterações |
| `OverlayScene` não é decorada | Sem alterações |

A única mudança foi na fonte de verdade do `selectedTab` e na quantidade de back stacks.

---

## Comportamento no device

| Ação | Módulo 3 | Módulo 4 |
|---|---|---|
| Abrir projeto → trocar aba → voltar | Volta para `ProjectList` (histórico perdido) | Volta para `ProjectDetail` (histórico preservado) ✓ |
| System back no tablet (multi-pane) | — | `BackHandler` fallback faz o pop ✓ |
| Trocar de projeto com `NewTask` aberta | — | `NewTask` é removida junto com `ProjectDetail` ✓ |
| Pressionar botão voltar do SO na raiz de uma aba | Sai do app | Sai do app (comportamento idêntico) |
| Rotacionar tela | Back stack perdido | Back stack preservado (`rememberNavBackStack` é `rememberSaveable`) |

---

## Arquivos modificados

```
MainActivity.kt   ← 3 back stacks, selectedTab explícito, activeBackStack,
                     BackHandler fallback, removeIf corrigido
```

Nenhum outro arquivo foi alterado — `NavigationDecorator.kt`, `Routes.kt` e todas as screens permanecem iguais.
