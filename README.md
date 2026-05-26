# Officina

Projeto de aprendizagem Android desenvolvido como laboratório prático para dominar as tecnologias modernas do ecossistema Android.

## Módulos

| Módulo | Conteúdo | Commit | Lesson |
|---|---|---|---|
| **1** | Navigation3 fundamentos — NavKey, NavEntry, NavDisplay, back stack | [`7c3650a`](../../commit/7c3650a) | [lesson1.md](docs/lesson1.md) |
| **2** | Scenes — DialogSceneStrategy, ListDetailScene customizada | [`6e9a0c3`](../../commit/6e9a0c3) | [lesson2.md](docs/lesson2.md) |
| **2.1** | ListDetail com Material Adaptive — detailPlaceholder, extraPane, foldables | [`74e548f`](../../commit/74e548f) | [lesson2.1.md](docs/lesson2.1.md) |
| **2.2** | extraPane na prática — NewTask como terceiro painel em tablets largos | [`253ed9a`](../../commit/253ed9a) | [lesson2.2.md](docs/lesson2.2.md) |
| **3** | Scene Decorators — NavigationBar/Rail adaptativo, derived key, OverlayScene | [`48fc407`](../../commit/48fc407) | [lesson3.md](docs/lesson3.md) |
| **4** | Múltiplos back stacks — histórico por aba, selectedTab explícito | [`d5d5cc9`](../../commit/d5d5cc9) | [lesson4.md](docs/lesson4.md) |
| **5** | BottomSheetSceneStrategy — OverlayScene customizada com ModalBottomSheet | [`a07c1f1`](../../commit/a07c1f1) | [lesson5.md](docs/lesson5.md) |
| **6** | NavigationSuiteScaffold — NavBar/Rail/Drawer automático, remove NavDecoratorStrategy | [`8b561d3`](../../commit/8b561d3) | [lesson6.md](docs/lesson6.md) |
| **7** | ViewModel + StateFlow + UiState selado — fonte única de verdade, reatividade automática | [`9c8b81a`](../../commit/9c8b81a) | [lesson7.md](docs/lesson7.md) |
| **8** | Animações de transição — AnimationConfig feature flag, 5 presets, transitionSpec/popTransitionSpec | WIP | [lesson8.md](docs/lesson8.md) |
| **9** | Material3 Expressive — ListItem(selected), SegmentedListItem, toggleTask | [`44c0178`](../../commit/44c0178) | [lesson9.md](docs/lesson9.md) |

## Tópicos de Aprendizagem

### Jetpack Compose
- Fundamentos de `@Composable`, estado e recomposição
- State hoisting e fluxo de dados unidirecional
- Efeitos: `LaunchedEffect`, `SideEffect`, `DisposableEffect`
- `LazyColumn`, `LazyRow` e listas de alta performance

### Navigation 3
- ✅ Modelo mental: back stack como lista mutável
- ✅ `NavKey`, `NavEntry`, `NavDisplay`
- ✅ `entryProvider` DSL e navegação type-safe com `@Serializable`
- ✅ `rememberNavBackStack` e persistência de estado
- ✅ Múltiplos back stacks (navegação por abas)
- Deep links
- ✅ Animações entre destinos

### Scenes (Navigation 3)
- ✅ `Scene` e `SceneStrategy` — o sistema adaptativo do Nav3
- ✅ `SinglePaneSceneStrategy` — comportamento padrão
- ✅ `DialogSceneStrategy` — destinos como diálogo
- ✅ `ListDetailSceneStrategy` — layout lista-detalhe adaptativo (Material Adaptive)
- ✅ `BottomSheetSceneStrategy` — destinos como bottom sheet
- ✅ `SceneDecoratorStrategy` — envolver scenes com UI comum (top bar, nav rail)
- ✅ Metadata em `NavEntry` para comunicação com a Scene pai

### Adaptive Layout
- ✅ `WindowSizeClass` (Compact / Medium / Expanded)
- ✅ `NavigationSuiteScaffold` — NavBar / NavRail / NavDrawer automático
- ✅ `ListDetailPaneScaffold` e `SupportingPaneScaffold`
- ✅ Testando em phone e tablet com split-screen e rotação

### Arquitetura
- ✅ `ViewModel` integrado ao Navigation 3
- ✅ `StateFlow` + `UiState` selado
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
