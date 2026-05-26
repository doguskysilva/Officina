# Módulo 10 — AppBarWithSearch + ExpandedDockedSearchBarWithGap + LargeFlexibleTopAppBar

## Componentes cobertos

| Componente | API | Onde |
|---|---|---|
| `AppBarWithSearch` | `@ExperimentalMaterial3Api` | `TaskListScreen` |
| `ExpandedDockedSearchBarWithGap` | `@ExperimentalMaterial3Api` | `TaskListScreen` |
| `LargeFlexibleTopAppBar` | `@ExperimentalMaterial3ExpressiveApi` | `ProjectListScreen` |

---

## AppBarWithSearch + ExpandedDockedSearchBarWithGap

### Padrão de estado compartilhado

Os dois componentes consomem o mesmo `state` e o mesmo `inputField`. O estado coordena a transição entre colapsado e expandido:

```kotlin
val textFieldState  = rememberTextFieldState()       // texto da busca
val searchBarState  = rememberSearchBarWithGapState() // colapsado ↔ expandido
val scrollBehavior  = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
val colors          = SearchBarDefaults.appBarWithSearchColors()

val inputField: @Composable () -> Unit = {
    SearchBarDefaults.InputField(
        textFieldState  = textFieldState,
        searchBarState  = searchBarState,
        colors          = colors.searchBarColors.inputFieldColors,
        onSearch        = { scope.launch { searchBarState.animateToCollapsed() } },
        placeholder     = { Text(modifier = Modifier.clearAndSetSemantics {}, text = "Buscar...") },
        leadingIcon     = { Icon(Icons.Default.Search, null) },
        trailingIcon    = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { textFieldState.edit { replace(0, length, "") } }) {
                    Icon(Icons.Default.Close, null)
                }
            }
        },
    )
}

Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
        AppBarWithSearch(
            state         = searchBarState,
            inputField    = inputField,
            colors        = colors,
            scrollBehavior = scrollBehavior,
        )
        ExpandedDockedSearchBarWithGap(
            state      = searchBarState,
            inputField = inputField,
        ) {
            // resultados da busca
        }
    }
) { padding -> /* conteúdo principal */ }
```

### Dois variants de busca expandida

| Componente | Estado | Visual expandido |
|---|---|---|
| `ExpandedDockedSearchBarWithGap` | `rememberSearchBarWithGapState()` | Flutua acima do conteúdo com gap |
| `ExpandedFullScreenContainedSearchBar` | `rememberContainedSearchBarState()` | Tela cheia com container sólido |

Para `ExpandedFullScreenContainedSearchBar`, os colors precisam de `containedColors`:
```kotlin
val colors = SearchBarDefaults.appBarWithSearchColors(
    searchBarColors = SearchBarDefaults.containedColors(state = searchBarState)
)
// e passa colors.searchBarColors para o componente expandido
```

### Filtro reativo via TextFieldState

`textFieldState.text` é `CharSequence` observável — lido dentro de `@Composable` dispara recomposição automaticamente:

```kotlin
val query = textFieldState.text.toString()
val filteredTasks = remember(query, allTasks) {
    if (query.isBlank()) allTasks
    else allTasks.filter { it.task.title.contains(query, ignoreCase = true) }
}
```

### Navegação cross-tab ao clicar no resultado

Clicar em uma task no resultado de busca muda de aba e abre o projeto com a task destacada:

```kotlin
onTaskClick = { projectId, projectName, taskId ->
    selectedTab = ProjectList
    projectsBackStack.removeIf { it is ProjectDetail || it is NewTask }
    projectsBackStack.add(ProjectDetail(projectId, projectName, highlightTaskId = taskId))
}
```

`highlightTaskId` foi adicionado ao `ProjectDetail` NavKey como `Int? = null`. No `ProjectDetailScreen`, `LaunchedEffect` faz scroll até o item e `SegmentedListItem` usa `selected = task.done || task.id == highlightTaskId`.

---

## LargeFlexibleTopAppBar

Substitui o `TopAppBar` simples na `ProjectListScreen`. Expande no topo, recolhe ao scrollar:

```kotlin
val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
        LargeFlexibleTopAppBar(
            title    = { Text("Officina") },
            subtitle = if (uiState is UiState.Success) {
                { Text("${uiState.data.size} projetos") }
            } else null,
            actions  = { /* ícone de ordenação */ },
            scrollBehavior = scrollBehavior,
        )
    }
)
```

Diferenciais do `LargeFlexibleTopAppBar` vs `LargeTopAppBar`:
- Suporta `subtitle` como slot opcional
- `expandedHeight` ajusta automaticamente com/sem subtitle
- `titleHorizontalAlignment` permite centralizar título+subtitle

---

## Arquivos criados/modificados

```
data/
└── TaskWithProject.kt              ← novo — Task + projectId + projectName flat

viewmodel/
└── TaskListViewModel.kt            ← novo — StateFlow com todas as tasks

screens/
├── TaskListScreen.kt               ← novo — AppBarWithSearch + ExpandedDockedSearchBarWithGap
├── ProjectListScreen.kt            ← LargeFlexibleTopAppBar, "Officina"
└── ProjectDetailScreen.kt          ← highlightTaskId, LaunchedEffect scroll, selected OR highlight

navigation/
└── Routes.kt                       ← ProjectDetail.highlightTaskId: Int? = null

MainActivity.kt                     ← entry<TaskList> com TaskListViewModel + onTaskClick cross-tab
```
