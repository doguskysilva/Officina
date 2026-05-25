# Módulo 7 — ViewModel + StateFlow + UiState selado

## O problema que motivou a mudança

Antes deste módulo, `ProjectListScreen` tinha os dados hardcoded como `private val sampleProjects = listOf(...)` diretamente no arquivo da tela. O problema é que uma tela de verdade precisa:

1. **Carregar dados** (de rede, banco de dados ou outro source)
2. **Sobreviver a config changes** (rotação de tela recria o `Activity` — `remember {}` some)
3. **Compartilhar estado** entre entries diferentes do back stack (ex: adicionar uma tarefa em `NewTask` e ver o resultado imediatamente em `ProjectDetail`)

A solução é mover o estado para fora do composable.

---

## A sequência de evolução

```
// 1. Estado hardcoded no arquivo — não muda, não persiste
private val sampleProjects = listOf(Project(1, "App Mobile", ...), ...)

// 2. Estado local no composable — muda, mas some ao sair da tela
var projects by remember { mutableStateOf(sampleProjects) }

// 3. ViewModel + StateFlow — muda, persiste, compartilhado ← onde chegamos
val uiState by vm.uiState.collectAsStateWithLifecycle()
```

---

## UiState selado

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
```

`out T` (covariância) permite que `UiState<Nothing>` seja atribuído onde `UiState<T>` é esperado — por isso `Loading` e `Error` podem ser `UiState<Nothing>` em vez de precisar de um tipo concreto.

O `when` no composable fica exaustivo e sem `else`:
```kotlin
when (uiState) {
    is UiState.Loading -> CircularProgressIndicator()
    is UiState.Error   -> Text(uiState.message)
    is UiState.Success -> LazyColumn { items(uiState.data) { ... } }
}
```

---

## ProjectRepository — fonte única de verdade

```kotlin
object ProjectRepository {
    private val _projects = MutableStateFlow(listOf(...))
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    fun addTask(projectId: Int, title: String) { _projects.update { ... } }
    fun deleteProject(id: Int) { _projects.update { ... } }
}
```

`object` = singleton. `MutableStateFlow` é privado; a exposição pública é `StateFlow` (read-only). `update {}` é atômico — usa `compareAndSet` internamente, seguro para coroutines.

---

## ViewModel + stateIn

```kotlin
class ProjectListViewModel : ViewModel() {
    val uiState: StateFlow<UiState<List<Project>>> = ProjectRepository.projects
        .map<List<Project>, UiState<List<Project>>> { UiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading,
        )
}
```

`stateIn` converte um `Flow` em `StateFlow`:
- `viewModelScope` — coroutine cancelada quando o ViewModel é destruído
- `WhileSubscribed(5_000)` — para de coletar o upstream 5 segundos depois do último subscriber desaparecer (cobre rotação de tela sem relançar o Flow)
- `initialValue = UiState.Loading` — o que o collector vê antes do primeiro emit

---

## ViewModel com parâmetro — ProjectDetailViewModel

```kotlin
class ProjectDetailViewModel(private val projectId: Int) : ViewModel() {
    val uiState: StateFlow<UiState<Project>> = ProjectRepository.projects
        .map { projects ->
            val project = projects.find { it.id == projectId }
            if (project != null) UiState.Success(project)
            else UiState.Error("Projeto não encontrado")
        }
        .stateIn(...)
}
```

Para criar com parâmetro no composable:
```kotlin
val vm: ProjectDetailViewModel = viewModel { ProjectDetailViewModel(route.projectId) }
```

O lambda `{ ProjectDetailViewModel(route.projectId) }` é a factory — chamada apenas uma vez quando o ViewModel é criado pela primeira vez. Se o composable recompor, o ViewModel existente é retornado sem recriar.

---

## Navigation3 + ViewModel: cada entry é um ViewModelStoreOwner

O artefato `lifecycle-viewmodel-navigation3` configura um `ViewModelStoreOwner` independente para cada entry no back stack. Isso significa:

- `viewModel()` dentro de um entry cria um ViewModel **scoped àquele entry**
- Quando o entry é removido do back stack → ViewModel é destruído (`.onCleared()` é chamado)
- Dois entries diferentes de `ProjectDetail` (projetos diferentes) têm ViewModels diferentes

```kotlin
entry<ProjectList> {
    val vm: ProjectListViewModel = viewModel()        // scoped a este entry
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    ProjectListScreen(uiState = uiState, ...)
}

entry<ProjectDetail> { route ->
    val vm: ProjectDetailViewModel = viewModel { ProjectDetailViewModel(route.projectId) }
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    ProjectDetailScreen(uiState = uiState, ...)
}
```

---

## Reatividade automática entre entries

`ProjectRepository._projects` é um único `MutableStateFlow`. Quando `NewTask` chama `ProjectRepository.addTask()`:

1. `_projects` emite o novo valor
2. `ProjectDetailViewModel.uiState` (que observa `ProjectRepository.projects`) recalcula automaticamente
3. `ProjectDetailScreen` recompõe mostrando a nova tarefa

Nenhum callback explícito entre entries é necessário. Esse é o benefício de ter uma fonte única de verdade.

---

## Arquivos criados/modificados

```
data/
├── Project.kt             ← novo — modelo de domínio
├── Task.kt                ← novo — modelo de domínio
└── ProjectRepository.kt   ← novo — fonte única de verdade (MutableStateFlow)

ui/
└── UiState.kt             ← novo — sealed interface Loading/Success/Error

viewmodel/
├── ProjectListViewModel.kt   ← novo
└── ProjectDetailViewModel.kt ← novo

screens/
├── ProjectListScreen.kt      ← recebe UiState<List<Project>>, mostra Loading/Error/Success
└── ProjectDetailScreen.kt    ← recebe UiState<Project>, lista tarefas reais

MainActivity.kt               ← viewModel() + collectAsStateWithLifecycle() nos entries
                                 onSave de NewTask chama ProjectRepository.addTask()
                                 onConfirm de ConfirmDelete chama ProjectRepository.deleteProject()
```
