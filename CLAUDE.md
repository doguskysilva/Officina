# Officina — Referência de Padrões Android

Este projeto é um laboratório de aprendizagem Android. Cada módulo implementa um conjunto de tecnologias modernas com documentação detalhada em `docs/`. Use este arquivo como índice de padrões ao refatorar outros projetos.

Repositório: `github.com/doguskysilva/Officina`  
Stack: Kotlin 2.2.10 · Compose BOM 2026.05.01 · Material3 1.5.0-alpha20 · minSdk 26

---

## Índice de padrões

### Navegação

| Padrão | Onde aprender | Arquivo de referência |
|---|---|---|
| Navigation3 básico — NavKey, NavDisplay, back stack | [lesson1.md](docs/lesson1.md) | `MainActivity.kt` |
| Rotas type-safe com `@Serializable` | [lesson1.md](docs/lesson1.md) | `navigation/Routes.kt` |
| Dialog como destino de navegação | [lesson2.md](docs/lesson2.md) | `screens/ConfirmDeleteDialog.kt` |
| Lista+detalhe adaptativo (phone/tablet automático) | [lesson2.1.md](docs/lesson2.1.md) | `MainActivity.kt` |
| Terceiro painel (extraPane) em tablets largos | [lesson2.2.md](docs/lesson2.2.md) | `MainActivity.kt` |
| BottomSheet como destino de navegação | [lesson5.md](docs/lesson5.md) | `scenes/BottomSheetSceneStrategy.kt` |
| Múltiplos back stacks (abas independentes) | [lesson4.md](docs/lesson4.md) | `MainActivity.kt` |
| NavigationSuiteScaffold — NavBar/Rail/Drawer automático | [lesson6.md](docs/lesson6.md) | `MainActivity.kt` |
| Animações de transição entre destinos | [lesson8.md](docs/lesson8.md) | `ui/AnimationConfig.kt` |

### Arquitetura

| Padrão | Onde aprender | Arquivo de referência |
|---|---|---|
| ViewModel keyed por rota no Navigation3 | [lesson7.md](docs/lesson7.md) | `MainActivity.kt` |
| UiState selado (Loading / Success / Error) | [lesson7.md](docs/lesson7.md) | `ui/UiState.kt` |
| StateFlow + collectAsStateWithLifecycle | [lesson7.md](docs/lesson7.md) | `viewmodel/ProjectListViewModel.kt` |
| Repository pattern com MutableStateFlow | [lesson7.md](docs/lesson7.md) | `data/ProjectRepository.kt` |

### Material3 Expressive

| Padrão | Onde aprender | Arquivo de referência |
|---|---|---|
| ListItem com estado selected | [lesson9.md](docs/lesson9.md) | `screens/TaskListScreen.kt` |
| AppBarWithSearch + ExpandedDockedSearchBarWithGap | [lesson10.md](docs/lesson10.md) | `screens/TaskListScreen.kt` |
| LargeFlexibleTopAppBar com subtítulo | [lesson10.md](docs/lesson10.md) | `screens/ProjectDetailScreen.kt` |
| HorizontalFloatingToolbar — FAB + ações secundárias | [lesson11.md](docs/lesson11.md) | `screens/ProjectDetailScreen.kt` |
| floatingToolbarVerticalNestedScroll | [lesson11.md](docs/lesson11.md) | `screens/ProjectDetailScreen.kt` |

### Glance Widgets

| Padrão | Onde aprender | Arquivo de referência |
|---|---|---|
| Estrutura base GlanceAppWidget + Receiver + XML | [lesson12.md](docs/lesson12.md) | `widget/SummaryWidget.kt` |
| SizeMode.Responsive — layouts por breakpoint | [lesson12.md](docs/lesson12.md) | `widget/SummaryWidget.kt` |
| GlanceTheme com dynamic colors | [lesson12.md](docs/lesson12.md) | `widget/SummaryWidget.kt` |
| CircleIconButton / FilledButton no widget | [lesson12.md](docs/lesson12.md) | `widget/SummaryWidget.kt` |
| LazyColumn + LinearProgressIndicator no widget | [lesson12.md](docs/lesson12.md) | `widget/ProjectListWidget.kt` |
| ActionParameters — deep link do widget para tela | [lesson12.md](docs/lesson12.md) | `widget/ProjectListWidget.kt` |
| PreferencesGlanceStateDefinition — estado por instância | [lesson12.md](docs/lesson12.md) | `widget/ProjectTasksWidget.kt` |
| ActionCallback — ação sem abrir o app | [lesson12.md](docs/lesson12.md) | `widget/ToggleTaskAction.kt` |
| Configuration Activity — os 4 contratos | [lesson12.md](docs/lesson12.md) | `widget/ProjectTasksConfigActivity.kt` |

### Previews

| Padrão | Onde aprender | Arquivo de referência |
|---|---|---|
| Anotações multi-preview customizadas | [lesson13.md](docs/lesson13.md) | `ui/preview/PreviewAnnotations.kt` |
| PreviewParameterProvider — múltiplos estados | [lesson13.md](docs/lesson13.md) | `ui/preview/PreviewData.kt` |
| Dados fake centralizados | [lesson13.md](docs/lesson13.md) | `ui/preview/PreviewData.kt` |
| Estratégia: quando usar cada anotação | [lesson13.md](docs/lesson13.md) | — |

---

## Dependências — versões testadas e funcionando

```toml
# gradle/libs.versions.toml
kotlin                  = "2.2.10"
composeBom              = "2026.05.01"
material3               = "1.5.0-alpha20"
material3Adaptive       = "1.5.0-alpha20"
navigation3             = "1.1.2"
lifecycleViewmodelNav3  = "2.10.0"
material3AdaptiveNav3   = "1.3.0-beta02"
glance                  = "1.3.0-alpha01"
kotlinxSerialization    = "1.8.0"
agp                     = "9.2.1"
```

Dependências críticas que costumam causar conflito:

```toml
# Navigation3 precisa dessas três juntas
androidx-navigation3-runtime        = { group = "androidx.navigation3", name = "navigation3-runtime" }
androidx-navigation3-ui             = { group = "androidx.navigation3", name = "navigation3-ui" }
androidx-lifecycle-viewmodel-navigation3 = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-navigation3" }

# Glance precisa dessas duas
androidx-glance-appwidget  = { group = "androidx.glance", name = "glance-appwidget" }
androidx-glance-material3  = { group = "androidx.glance", name = "glance-material3" }
```

---

## Packages do Glance — armadilhas comuns

O Glance tem seus próprios composables. Importar os do Compose UI causa erros silenciosos.

| Símbolo | Package correto |
|---|---|
| `LinearProgressIndicator`, `CircularProgressIndicator` | `androidx.glance.appwidget` (não `.components`) |
| `LazyColumn`, `items`, `VerticalScrollMode` | `androidx.glance.appwidget.lazy` |
| `FilledButton`, `CircleIconButton`, `SquareIconButton` | `androidx.glance.appwidget.components` |
| `ActionCallback`, `actionRunCallback` | `androidx.glance.appwidget.action` |
| `updateAppWidgetState` | `androidx.glance.appwidget.state` |
| `PreferencesGlanceStateDefinition` | `androidx.glance.state` |
| `currentState`, `LocalSize`, `GlanceTheme` | `androidx.glance` |
| `defaultWeight()` | membro de `RowScope`/`ColumnScope` — não importar |

---

## Padrões de código recorrentes

### Rota type-safe

```kotlin
// navigation/Routes.kt
@Serializable
data class ProjectDetail(
    val projectId: Int,
    val projectName: String,
    val highlightTaskId: Int? = null,
) : NavKey
```

### ViewModel keyed por rota

```kotlin
// Dentro do entry<ProjectDetail>
val vm: ProjectDetailViewModel = viewModel(key = route.projectId.toString()) {
    ProjectDetailViewModel(route.projectId)
}
```

### UiState selado

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
```

### Deep link do widget para tela

```kotlin
// Widget — passa parâmetros via ActionParameters
actionStartActivity<MainActivity>(
    actionParametersOf(PROJECT_ID_KEY to project.id, PROJECT_NAME_KEY to project.name)
)

// MainActivity.onCreate — monta back stack inicial
val deepLinkProjectId = intent.getIntExtra("project_id", -1).takeIf { it != -1 }
val projectsInitialStack: Array<NavKey> = if (savedInstanceState == null && deepLinkProjectId != null)
    arrayOf(ProjectList, ProjectDetail(deepLinkProjectId, deepLinkProjectName!!))
else arrayOf(ProjectList)
val projectsBackStack = rememberNavBackStack(*projectsInitialStack)
```

### Anotação de preview reutilizável

```kotlin
@Preview(name = "Phone · Light", device = "spec:width=411dp,height=891dp")
@Preview(name = "Phone · Dark",  device = "spec:width=411dp,height=891dp", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Tablet · Light", device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(name = "Tablet · Dark",  device = "spec:width=1280dp,height=800dp,dpi=240", uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class OfficinaPreviews
```

---

## Estrutura de arquivos do projeto

```
app/src/main/java/com/doguskytech/officina/
├── MainActivity.kt                  ← NavDisplay + NavigationSuiteScaffold
├── data/
│   ├── Project.kt / Task.kt         ← modelos de domínio
│   ├── TaskWithProject.kt
│   └── ProjectRepository.kt         ← MutableStateFlow in-memory
├── navigation/
│   └── Routes.kt                    ← todas as NavKey (@Serializable)
├── scenes/
│   └── BottomSheetSceneStrategy.kt  ← SceneStrategy customizada
├── screens/
│   ├── ProjectListScreen.kt
│   ├── ProjectDetailScreen.kt       ← HorizontalFloatingToolbar
│   ├── TaskListScreen.kt            ← AppBarWithSearch
│   ├── *Previews.kt                 ← previews separados por tela
│   └── ...
├── ui/
│   ├── UiState.kt
│   ├── AnimationConfig.kt           ← feature flag de animações
│   ├── preview/
│   │   ├── PreviewAnnotations.kt
│   │   └── PreviewData.kt
│   └── theme/
│       └── OfficinaTheme.kt
├── viewmodel/
│   ├── ProjectListViewModel.kt
│   ├── ProjectDetailViewModel.kt
│   └── TaskListViewModel.kt
└── widget/
    ├── SummaryWidget.kt             ← SizeMode.Responsive
    ├── ProjectListWidget.kt         ← LazyColumn + ActionParameters
    ├── ProjectTasksWidget.kt        ← PreferencesGlanceStateDefinition
    ├── ProjectTasksConfigActivity.kt
    └── ToggleTaskAction.kt          ← ActionCallback
```
