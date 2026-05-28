# Lesson 17 — Domain Layer + Sort Projects + Project Status

## O que foi feito

Três funcionalidades entregues juntas que fecham o modelo de domínio do app:

1. **Camada de domínio** — package `domain/` com modelos puros Kotlin, sem qualquer import Android
2. **Sort Projects funcional** — `SortProjectsSheet` agora reordena a lista em tempo real
3. **Status de projeto** — ciclo de vida completo com regras de negócio enforçadas na camada certa

---

## Parte 1 — Camada de Domínio

### Por que separar domínio de dados?

```kotlin
// ❌ antes — Priority conhecia Android e R.string
enum class Priority(@StringRes val labelRes: Int) {
    LOW(R.string.priority_low), ...
}

// ✓ depois — Priority é Kotlin puro, testável sem emulador
enum class Priority { LOW, MEDIUM, HIGH }
```

Quando uma entidade de domínio importa `androidx.annotation.StringRes` ou `com.doguskytech.officina.R`, ela fica acoplada ao Android. Isso significa:
- **Não é testável** com `junit` puro (precisa de contexto Android)
- O domínio "sabe" sobre detalhes de apresentação (IDs de string)

### Estrutura

```
domain/
├── model/          ← enums e data classes puros
│   ├── Priority.kt
│   ├── TaskStatus.kt    ← PENDING / DONE / CANCELLED
│   ├── ProjectStatus.kt ← WAITING / IN_PROGRESS / DONE / CANCELLED
│   ├── SortOrder.kt
│   ├── Task.kt
│   └── Project.kt
├── rules/
│   └── ProjectRules.kt  ← funções puras, zero imports Android
└── repository/
    └── ProjectRepository.kt  ← interface (StateFlow é kotlinx, não Android)
```

### `domain/rules/ProjectRules.kt` — funções puras

```kotlin
object ProjectRules {
    fun canAddTask(project: Project): Boolean    = project.isActive
    fun canCompleteTasks(project: Project): Boolean = project.isActive
    fun canStart(project: Project): Boolean   = project.status == ProjectStatus.WAITING
    fun canFinish(project: Project): Boolean  = project.isActive && project.canFinish
    fun canCancel(project: Project): Boolean  = project.status != ProjectStatus.DONE
    fun canDelete(project: Project): Boolean  = !project.isActive
}
```

`ProjectRules` não tem nenhum import. É testável com `assertEquals(true, ProjectRules.canStart(project))` num teste JUnit simples.

### Interface do repositório no domínio

`StateFlow` é de `kotlinx.coroutines.flow` — não é Android. Portanto é válido no domínio:

```kotlin
// domain/repository/ProjectRepository.kt
interface ProjectRepository {
    val projects: StateFlow<List<Project>>   // kotlinx ✓
    val sortOrder: StateFlow<SortOrder>
    fun startProject(projectId: Int)
    fun finishProject(projectId: Int)
    // ...
}
```

### Ponte Android: `ui/StringResExt.kt`

A camada de UI precisa mapear enums de domínio para IDs de string do Android. A solução é uma extension property no package `ui/`:

```kotlin
// ui/StringResExt.kt — Android fica aqui, não no domínio
val Priority.labelRes: Int
    @StringRes get() = when (this) {
        Priority.LOW    -> R.string.priority_low
        Priority.MEDIUM -> R.string.priority_medium
        Priority.HIGH   -> R.string.priority_high
    }

val ProjectStatus.labelRes: Int
    @StringRes get() = when (this) { ... }
```

Nos screens, o uso é idêntico ao anterior:
```kotlin
Text(stringResource(priority.labelRes))       // mesma sintaxe
Text(stringResource(project.status.labelRes)) // nova
```

### Backward compat via type alias

Para não precisar atualizar todos os imports de uma vez, os arquivos antigos viram type aliases:

```kotlin
// data/Priority.kt
package com.doguskytech.officina.data
typealias Priority = com.doguskytech.officina.domain.model.Priority
```

`data/ProjectRepository.kt` virou um objeto delegante que mantém `toggleTask` para os widgets legados:

```kotlin
object ProjectRepository : DomainProjectRepository by InMemoryProjectRepository {
    fun toggleTask(projectId: Int, taskId: Int) =
        InMemoryProjectRepository.completeTasks(projectId, setOf(taskId))
}
```

---

## Parte 2 — Sort Projects Funcional

### O problema

`SortProjectsSheet` tinha `clickable { }` vazio — sem estado, sem efeito.

### `SortProjectsViewModel` (mínimo)

```kotlin
class SortProjectsViewModel : ViewModel() {
    val currentSort: StateFlow<SortOrder> = InMemoryProjectRepository.sortOrder
    fun setSort(order: SortOrder) = InMemoryProjectRepository.setSortOrder(order)
}
```

### `ProjectListViewModel` — `combine {}`

O ViewModel agora combina dois flows:

```kotlin
combine(InMemoryProjectRepository.projects, InMemoryProjectRepository.sortOrder) { projects, sort ->
    UiState.Success(
        when (sort) {
            SortOrder.NAME_ASC -> projects.sortedBy { it.name.lowercase() }
            SortOrder.NEWEST   -> projects.sortedByDescending { it.createdAt }
            SortOrder.OLDEST   -> projects.sortedBy { it.createdAt }
        }
    )
}
```

`combine {}` emite sempre que **qualquer** dos dois flows mudar. Assim, trocar a ordenação reordena a lista sem refetch.

### `Project` e `Task` ganharam `createdAt` e `completedAt`

```kotlin
data class Project(
    ...
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
)
```

`createdAt` é necessário para as ordenações por data. `completedAt` registra quando o projeto foi finalizado.

---

## Parte 3 — Status de Projeto

### Ciclo de vida

```
WAITING → IN_PROGRESS → DONE
                      ↘ CANCELLED
```

Transições válidas (`ProjectRules`):
- `WAITING → IN_PROGRESS`: via `startProject()`
- `IN_PROGRESS → DONE`: via `finishProject()` — só quando `canFinish == true`
- `IN_PROGRESS / WAITING / CANCELLED → CANCELLED`: via `cancelProject()`
- Excluir: apenas projetos não-ativos (`canDelete = !isActive`)

### `Task` também tem status

```kotlin
enum class TaskStatus { PENDING, DONE, CANCELLED }
```

- `task.done` → computed property: `status == TaskStatus.DONE` (backward compat)
- `task.isPending` → `status == TaskStatus.PENDING`
- Tasks DONE e CANCELLED são **imutáveis** — não há `reopenTask`

### Guards no repositório

Cada operação verifica as regras antes de executar:

```kotlin
override fun addTask(projectId: Int, title: String, priority: Priority) {
    _projects.update { current ->
        val project = current.find { it.id == projectId } ?: return@update current
        if (!ProjectRules.canAddTask(project)) return@update current // guard
        // ...adiciona task
    }
}
```

O guard garante integridade mesmo se a UI tiver um bug e chamar a operação indevida.

### UI por status — `AnimatedContent` no toolbar

O toolbar muda conforme o status do projeto:

```kotlin
AnimatedContent(
    targetState = if (isInSelectionMode) null else project.status,
    label = "toolbarActions",
) { status ->
    Row {
        if (status == null) {
            // modo seleção: Complete / Cancel tasks / Delete
        } else when (status) {
            WAITING     -> { PlayArrow (Iniciar), Delete }
            IN_PROGRESS -> { DoneAll, Done (Finalizar, enabled=canFinish) }
            DONE, CANCELLED -> { Delete }
        }
    }
}
```

`null` representa o modo de seleção. O `AnimatedContent` anima a transição entre os conjuntos de ações.

### Banner "pronto para finalizar"

Quando `project.canFinish && project.isActive`, aparece um `Card` no topo da lista:

```kotlin
if (project.canFinish && project.isActive && !isInSelectionMode) {
    item {
        Card(modifier = Modifier.fillMaxWidth().padding(...)) {
            Row {
                Text(stringResource(R.string.all_tasks_done_banner), ...)
                TextButton(onClick = onFinishProject) {
                    Text(stringResource(R.string.action_finish_now))
                }
            }
        }
    }
}
```

### Tasks canceladas — visual distinto

```kotlin
// Trailing icon
TaskStatus.CANCELLED -> Icon(Icons.Default.Close, ...)

// Texto tachado
Text(
    text = task.title,
    textDecoration = if (task.status == TaskStatus.CANCELLED)
        TextDecoration.LineThrough else null,
)
```

---

## Arquivos modificados / criados

| Arquivo | Operação |
|---|---|
| `domain/model/Priority.kt` | novo — enum puro |
| `domain/model/TaskStatus.kt` | novo |
| `domain/model/ProjectStatus.kt` | novo |
| `domain/model/SortOrder.kt` | novo |
| `domain/model/Task.kt` | novo — campos status, createdAt, completedAt |
| `domain/model/Project.kt` | novo — campos status, createdAt, completedAt |
| `domain/rules/ProjectRules.kt` | novo — funções puras testáveis |
| `domain/repository/ProjectRepository.kt` | novo — interface |
| `data/InMemoryProjectRepository.kt` | novo — implementação com guards |
| `data/Priority.kt` | type alias → domain.model.Priority |
| `data/Task.kt` | type alias → domain.model.Task |
| `data/Project.kt` | type alias → domain.model.Project |
| `data/ProjectRepository.kt` | objeto delegante (backward compat widgets) |
| `ui/StringResExt.kt` | novo — extension props para @StringRes |
| `ui/PriorityUi.kt` | atualiza import para domain.model |
| `viewmodel/ProjectListViewModel.kt` | combine com sortOrder |
| `viewmodel/ProjectDetailViewModel.kt` | novos métodos de status |
| `viewmodel/TaskListViewModel.kt` | usa InMemoryProjectRepository |
| `viewmodel/SortProjectsViewModel.kt` | novo — mínimo |
| `screens/SortProjectsSheet.kt` | funcional — currentSort + check mark |
| `screens/ProjectListScreen.kt` | badge de status por projeto |
| `screens/ProjectDetailScreen.kt` | toolbar por status, banner, tasks canceladas |
| `screens/TaskListScreen.kt` | labelRes import, filtro PENDING usa isPending |
| `screens/NewTaskScreen.kt` | labelRes import |
| `screens/SimpleScreenPreviews.kt` | atualiza SortProjectsSheet preview |
| `ui/preview/PreviewData.kt` | fake data com status e timestamps |
| `res/values/strings.xml` | novas strings (status, ações, banner) |
| `res/values-en/strings.xml` | tradução EN |
