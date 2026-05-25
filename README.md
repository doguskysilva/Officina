# Officina

Projeto de aprendizagem Android desenvolvido como laboratório prático para dominar as tecnologias modernas do ecossistema Android.

## Módulos

| Módulo | Conteúdo | Commit | Lesson |
|---|---|---|---|
| **1** | Navigation3 fundamentos — NavKey, NavEntry, NavDisplay, back stack | [`7c3650a`](../../commit/7c3650a) | [lesson1.md](docs/lesson1.md) |
| **2** | Scenes — DialogSceneStrategy, ListDetailScene customizada | [`6e9a0c3`](../../commit/6e9a0c3) | [lesson2.md](docs/lesson2.md) |
| **2.1** | ListDetail com Material Adaptive — detailPlaceholder, extraPane, foldables | [`75d82d8`](../../commit/75d82d8) | [lesson2.1.md](docs/lesson2.1.md) |

## Tópicos de Aprendizagem

### Jetpack Compose
- Fundamentos de `@Composable`, estado e recomposição
- State hoisting e fluxo de dados unidirecional
- Efeitos: `LaunchedEffect`, `SideEffect`, `DisposableEffect`
- `LazyColumn`, `LazyRow` e listas de alta performance

### Navigation 3
- Modelo mental: back stack como lista mutável
- `NavKey`, `NavEntry`, `NavDisplay`
- `entryProvider` DSL e navegação type-safe com `@Serializable`
- `rememberNavBackStack` e persistência de estado
- Múltiplos back stacks (navegação por abas)
- Deep links
- Animações entre destinos

### Scenes (Navigation 3)
- `Scene` e `SceneStrategy` — o sistema adaptativo do Nav3
- `SinglePaneSceneStrategy` — comportamento padrão
- `DialogSceneStrategy` — destinos como diálogo
- `ListDetailSceneStrategy` — layout lista-detalhe adaptativo (Material Adaptive)
- `BottomSheetSceneStrategy` — destinos como bottom sheet
- `SceneDecoratorStrategy` — envolver scenes com UI comum (top bar, nav rail)
- Metadata em `NavEntry` para comunicação com a Scene pai

### Adaptive Layout
- `WindowSizeClass` (Compact / Medium / Expanded)
- `NavigationSuiteScaffold` — NavBar / NavRail / NavDrawer automático
- `ListDetailPaneScaffold` e `SupportingPaneScaffold`
- Testando em phone e tablet com split-screen e rotação

### Arquitetura
- `ViewModel` integrado ao Navigation 3
- `StateFlow` + `UiState` selado
- Modularização de código de navegação

## Dispositivos de Teste

- **Phone** — layout Compact, navegação linear com `NavigationBar`
- **Tablet** — layout Expanded, lista+detalhe lado a lado com `NavigationRail`

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Kotlin | 2.2.10 |
| Compose BOM | 2026.05.01 |
| Material3 | 1.5.0-alpha20 |
| Navigation 3 | 1.1.2 |
| Material3 Adaptive Nav3 | 1.3.0-beta02 |
| AGP | 9.2.1 |
| minSdk | 26 |
