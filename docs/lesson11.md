# Módulo 11 — HorizontalFloatingToolbar

## Componente coberto

| Componente | API | Onde |
|---|---|---|
| `HorizontalFloatingToolbar` | `@ExperimentalMaterial3ExpressiveApi` | `ProjectDetailScreen` |

---

## Dois padrões de scroll

### 1. FAB slot + `floatingToolbarVerticalNestedScroll` (toolbar lateral/end)

Usado quando a toolbar está no **FAB slot do Scaffold** ou alinhada a uma aresta lateral. O estado `expanded` é gerenciado manualmente e o modifier vai no conteúdo scrollável:

```kotlin
var expanded by rememberSaveable { mutableStateOf(true) }

Scaffold(
    floatingActionButton = {
        HorizontalFloatingToolbar(
            expanded = expanded,
            floatingActionButton = {
                // FAB = ação primária direta (não toggle)
                FloatingToolbarDefaults.VibrantFloatingActionButton(
                    onClick = dropUnlessResumed { onNewTaskClick(NewTask(projectId = project.id)) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nova tarefa")
                }
            },
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        ) {
            // Ações secundárias — visíveis quando expanded = true
            IconButton(
                onClick = { onMarkAllDone() },
                modifier = Modifier.focusProperties { canFocus = expanded },
            ) { Icon(Icons.Default.Done, "Marcar todas concluídas") }

            IconButton(
                onClick = dropUnlessResumed { onDeleteClick(ConfirmDelete(...)) },
                modifier = Modifier.focusProperties { canFocus = expanded },
            ) { Icon(Icons.Default.Delete, "Excluir projeto") }
        }
    }
) { padding ->
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .floatingToolbarVerticalNestedScroll(
                expanded = expanded,
                onExpand = { expanded = true },
                onCollapse = { expanded = false },
            ),
    ) { ... }
}
```

Import necessário:
```kotlin
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
```

### 2. `exitAlwaysScrollBehavior` (toolbar centralizada na base)

Para toolbar em `Alignment.BottomCenter`: a toolbar inteira some ao scrollar. O `expanded` fica sempre `true`, e o comportamento é controlado pelo `scrollBehavior`:

```kotlin
val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = Bottom)

Scaffold(modifier = Modifier.nestedScroll(scrollBehavior)) { padding ->
    HorizontalFloatingToolbar(
        expanded = true,
        scrollBehavior = scrollBehavior,
        modifier = Modifier.align(Alignment.BottomCenter).offset(y = -ScreenOffset),
        ...
    )
}
```

---

## Overload com `floatingActionButton`

| Parâmetro | Descrição |
|---|---|
| `expanded` | Se `false`, apenas o FAB é visível; o `content` está oculto |
| `floatingActionButton` | Use `FloatingToolbarDefaults.VibrantFloatingActionButton` para combinar com `vibrantFloatingToolbarColors()` |
| `colors` | `vibrantFloatingToolbarColors()` ou `standardFloatingToolbarColors()` |
| `floatingActionButtonPosition` | `End` (padrão) alinha o FAB à direita; deve coincidir com `floatingActionButtonPosition` do `Scaffold` |

---

## Acessibilidade: `focusProperties`

Botões dentro de `content` devem impedir foco quando invisíveis:

```kotlin
IconButton(
    onClick = { ... },
    modifier = Modifier.focusProperties { canFocus = expanded },
) { Icon(...) }
```

---

## Guideline: navigation bar vs. toolbar de navegação

> _Don't show a navigation bar and a toolbar with navigation controls at the same time._

O `HorizontalFloatingToolbar` em `ProjectDetailScreen` contém **ações contextuais** (nova tarefa, marcar todas, excluir), não controles de navegação. O `NavigationSuiteScaffold` do módulo 6 gerencia a `NavigationBar`/`NavigationRail` global. Não há conflito.

---

## Dynamic Colors

O `OfficinaTheme` já usa `dynamicColor: Boolean = true` (padrão). Em Android 12+ (API 31+), o esquema de cores é gerado a partir do papel de parede via `dynamicDarkColorScheme`/`dynamicLightColorScheme` — nenhuma alteração necessária.

---

## Arquivos criados/modificados

```
data/
└── ProjectRepository.kt          ← markAllTasksDone(projectId)

viewmodel/
└── ProjectDetailViewModel.kt     ← markAllTasksDone()

screens/
└── ProjectDetailScreen.kt        ← HorizontalFloatingToolbar no FAB slot
                                     onMarkAllDone adicionado
                                     header (Button "Nova Tarefa") e footer (TextButton "Excluir") removidos
                                     floatingToolbarVerticalNestedScroll no LazyColumn

MainActivity.kt                   ← onMarkAllDone = { vm.markAllTasksDone() }
```
