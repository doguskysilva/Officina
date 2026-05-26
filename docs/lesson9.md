# Módulo 9 — Material3 Expressive: ListItem(selected) + SegmentedListItem

## O que são os novos componentes Expressive

O Material3 Expressive (M3E) introduz overloads dos componentes existentes com comportamento de seleção embutido. Em vez de gerenciar manualmente background, shape e animação de seleção, os componentes cuidam disso sozinhos.

Requer `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`.

---

## Fase 1 — implementação manual (o problema)

```kotlin
// Para indicar seleção: lógica manual de cor + shape
ListItem(
    headlineContent = { Text(project.name) },
    modifier = Modifier
        .background(
            color = if (selected) MaterialTheme.colorScheme.secondaryContainer
                    else Color.Transparent,
            shape = RoundedCornerShape(12.dp)
        )
        .clickable { ... }
)
```

Problemas:
- Shape precisa ser calculado manualmente para grupos (primeiro, meio, último)
- Cor de seleção não anima
- Acessibilidade de seleção não está declarada

---

## Fase 2 — ListItem(selected) e SegmentedListItem

### ListItem(selected) — lista com seleção única

```kotlin
ListItem(
    selected = project.id == selectedProjectId,
    onClick = dropUnlessResumed {
        onProjectClick(ProjectDetail(project.id, project.name))
    },
    colors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
    supportingContent = { Text("${project.tasks.size} tarefas") },
    trailingContent = { Text("→") },
    modifier = Modifier.fillMaxWidth(),
) { Text(project.name) }
```

`selected = true` aplica automaticamente: highlight de cor, shape arredondado e animação de transição.

### SegmentedListItem — grupo com shape adaptativo

```kotlin
Column(
    modifier = Modifier.fillMaxWidth().selectableGroup(),
    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
) {
    tasks.forEachIndexed { index, task ->
        SegmentedListItem(
            selected = task.done,
            onClick = { onTaskToggle(task.id) },
            shapes = ListItemDefaults.segmentedShapes(index, tasks.size),
            colors = ListItemDefaults.segmentedColors(),
            modifier = Modifier.fillMaxWidth(),
        ) { Text(task.title) }
    }
}
```

| Utilitário | O que faz |
|---|---|
| `segmentedShapes(index, count)` | Calcula cantos: topo arredondado no primeiro, base no último, retos nos do meio |
| `segmentedColors()` | Cores do estilo segmentado do M3E |
| `SegmentedGap` | Espaçamento padrão entre itens segmentados |
| `selectableGroup()` | Semântica de acessibilidade: um item selecionável por vez |

---

## Descoberta das assinaturas reais da API

O compilador rejeitou `headlineContent = { ... }` porque a API Expressive usa `content` como trailing lambda (último parâmetro posicional):

```
// ERRADO — parâmetro da API clássica
ListItem(headlineContent = { Text(name) })

// CORRETO — API Expressive usa trailing lambda
ListItem(selected = ..., onClick = ...) { Text(name) }
```

O mesmo vale para `SegmentedListItem`. O erro do compilador "None of the following candidates is applicable" revelou as assinaturas corretas.

---

## Armadilha: @Composable dentro de LazyListScope

`ListItemDefaults.colors()` é uma função `@Composable`. O bloco de conteúdo do `LazyColumn` (`LazyListScope`) **não** é `@Composable` — chamadas assim dentro dele causam erro de compilação:

```
@Composable invocations can only happen from the context of a @Composable function
```

Solução: hoistar o cálculo para antes do `LazyColumn`:

```kotlin
// ERRADO — LazyListScope não é @Composable
LazyColumn { 
    val colors = ListItemDefaults.colors(...)  // ← erro aqui
    items(...) { ... }
}

// CORRETO — calcular fora do LazyColumn
is UiState.Success -> {
    val colors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
    LazyColumn {
        items(...) { ... }
    }
}
```

---

## contentPadding vs padding no LazyColumn

Para alinhar o espaçamento lateral e vertical da lista de projetos com o `Column` do segmented list (que usa `padding(horizontal = 16.dp, vertical = 12.dp)`):

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
)
```

`contentPadding` (não `modifier.padding`) é o correto para LazyColumn: o padding é aplicado ao conteúdo e o scroll se estende até as bordas da área de toque.

---

## Arquivos criados/modificados

```
data/
└── ProjectRepository.kt        ← novo toggleTask(projectId, taskId)

viewmodel/
└── ProjectDetailViewModel.kt   ← novo toggleTask(taskId)

screens/
├── ProjectListScreen.kt        ← ListItem(selected), colors, contentPadding
└── ProjectDetailScreen.kt      ← SegmentedListItem para tasks, onTaskToggle

MainActivity.kt                 ← passa onTaskToggle = { vm.toggleTask(it) }
```
