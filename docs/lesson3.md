# Módulo 3 — Scene Decorators

## O problema que Scene Decorators resolvem

Você quer `NavigationBar` no phone e `NavigationRail` no tablet em todas as telas. As opções sem Decorator:

1. Colocar em cada tela individualmente — repetição, acoplamento
2. Colocar no `Scaffold` da `MainActivity` e passar padding — funciona para single-pane, quebra no ListDetail (o Rail ficaria dentro do painel da lista, não do lado da Scene inteira)

O Scene Decorator resolve isso de forma elegante: ele envolve a Scene **depois** que a SceneStrategy decidiu o layout. Então o Rail fica do lado da Scene inteira — seja ela single-pane ou list-detail.

```
SceneStrategy calcula:   [ListDetailScene]
                               ↓
SceneDecoratorStrategy envolve:
                    ┌──────────────────────────┐
                    │  NavigationRail  │ Scene  │
                    │                  │ (L+D)  │
                    └──────────────────────────┘
```

---

## Os dois conceitos novos

| Conceito | O que é |
|---|---|
| `SceneDecoratorStrategy` | Recebe uma Scene, decide se decora e devolve uma Scene (nova ou a mesma) |
| `DecoratorScene` | A Scene "embrulhada" — copia entries/previousEntries/metadata e envolve o content |

**O fluxo completo com decorator:**

```
back stack
    ↓
SceneStrategy → Scene calculada
    ↓
SceneDecoratorStrategy → Scene decorada (com NavigationBar/Rail em volta)
    ↓
NavDisplay renderiza
```

---

## Regra crítica: OverlayScene não é decorada

`NavDisplay` **nunca** chama o decorator para `OverlayScene` (Dialog, BottomSheet). Faz sentido — você não quer uma NavigationBar dentro de um diálogo. Isso é garantido pela biblioteca, não precisa de nenhum `if` no seu código.

---

## Implementação: NavDecoratorStrategy

```kotlin
class NavDecoratorStrategy<T : Any>(
    private val windowSizeClass: WindowSizeClass,
    private val selectedTab: NavKey,
    private val onTabSelected: (NavKey) -> Unit,
) : SceneDecoratorStrategy<T> {

    override fun SceneDecoratorStrategyScope<T>.decorateScene(scene: Scene<T>): Scene<T> =
        NavDecoratorScene(scene, windowSizeClass, selectedTab, onTabSelected)
}
```

Simples: sempre decora. Se você quiser decorar seletivamente (ex: só quando `scene` não é `SomeSpecialScene`), basta adicionar um `if` e retornar `scene` diretamente.

---

## Implementação: NavDecoratorScene

```kotlin
class NavDecoratorScene<T : Any>(
    private val scene: Scene<T>,
    private val windowSizeClass: WindowSizeClass,
    private val selectedTab: NavKey,
    private val onTabSelected: (NavKey) -> Unit,
) : Scene<T> {

    // REGRA 1 — derived key: combina classe + key da scene interna.
    // Sem isso: NavDisplay não detecta mudança de SinglePane → ListDetail
    // e as animações param de funcionar.
    override val key = scene::class to scene.key

    // REGRA 2 — sempre copiar estas três propriedades da scene interna.
    // Elas definem navegação e back — o decorator não muda isso.
    override val entries = scene.entries
    override val previousEntries = scene.previousEntries
    override val metadata = scene.metadata

    override val content: @Composable () -> Unit = {
        val isCompact = !windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)

        if (isCompact) {
            // Phone: NavigationBar na base via Scaffold
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        appTabs.forEach { tab ->
                            NavigationBarItem(
                                selected = selectedTab == tab.route,
                                onClick = { onTabSelected(tab.route) },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) { scene.content() }
            }
        } else {
            // Tablet: NavigationRail na lateral
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail {
                    appTabs.forEach { tab ->
                        NavigationRailItem(
                            selected = selectedTab == tab.route,
                            onClick = { onTabSelected(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) { scene.content() }
            }
        }
    }
}
```

### Por que `scene::class to scene.key`?

O `NavDisplay` anima entre Scenes quando a combinação `(classe, key)` muda. Se o decorator sobrescrevesse com uma key fixa ou copiasse só `scene.key`, a mudança de `SinglePaneScene` para `ListDetailScene` (ao rotacionar o tablet) teria a mesma key e **não animaria**.

```
Exemplo sem derived key (errado):
  portrait:  NavDecoratorScene(key=ProjectList)  ← mesma key
  landscape: NavDecoratorScene(key=ProjectList)  ← mesma key → sem animação

Com derived key (certo):
  portrait:  NavDecoratorScene(key=SinglePaneScene::class to ProjectList)
  landscape: NavDecoratorScene(key=ListDetailScene::class to ProjectList) ← diferente → anima ✓
```

---

## Conectando ao NavDisplay

```kotlin
val navDecorator = rememberNavDecoratorStrategy<NavKey>(
    selectedTab = selectedTab,
    onTabSelected = { route ->
        // Módulo 3: troca simples — limpa o back stack, sem preservar histórico por aba.
        // Módulo 4 vai preservar o estado de cada aba com múltiplos back stacks.
        backStack.clear()
        backStack.add(route)
    }
)

NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    sceneStrategies = listOf(dialogStrategy, listDetailStrategy),
    sceneDecoratorStrategies = listOf(navDecorator), // ← aqui
    entryProvider = entryProvider<NavKey> { ... }
)
```

**Ordem em `sceneDecoratorStrategies`:** cada decorator recebe a saída do anterior. Se você tivesse dois decorators — um para NavigationBar e outro para TopAppBar — o segundo envolve o primeiro. A ordem define a hierarquia de envolvimento.

---

## Aba selecionada com derivedStateOf

```kotlin
val topLevelRoutes = remember { appTabs.map { it.route }.toSet() }

val selectedTab by remember {
    derivedStateOf {
        backStack.firstOrNull { it in topLevelRoutes } ?: ProjectList
    }
}
```

`derivedStateOf` cria um estado derivado que só recomputa quando `backStack` muda **e** o resultado muda. Sem isso, qualquer mudança no back stack (incluindo navegar para ProjectDetail dentro da aba) dispararia uma recomposição do decorator inteiro desnecessariamente.

---

## Comportamento no device

| Device | Scene calculada | Depois do decorator |
|---|---|---|
| Phone | `SinglePaneScene` | `NavDecoratorScene(Single)` — NavigationBar na base |
| Tablet | `ListDetailScene` | `NavDecoratorScene(ListDetail)` — NavigationRail na lateral |
| Qualquer | `DialogScene` (OverlayScene) | **Não decorada** — sem NavigationBar/Rail |

---

## Limitação do Módulo 3 (resolvida no Módulo 4)

Trocar de aba faz `backStack.clear()` — todo o histórico de navegação dentro da aba é perdido. Se o usuário estava em `ProjectDetail` na aba Projetos, foi para Tarefas e voltou, começa de `ProjectList` novamente.

O Módulo 4 resolve isso com **múltiplos back stacks**: cada aba tem sua própria lista independente que preserva estado.

---

## Arquivos criados/modificados

```
scenes/
└── NavigationDecorator.kt   ← NavDecoratorStrategy + NavDecoratorScene + appTabs (novo)

navigation/
└── Routes.kt                ← + TaskList, AppSettings

screens/
├── TaskListScreen.kt        ← placeholder (novo)
└── SettingsScreen.kt        ← placeholder (novo)

MainActivity.kt              ← + sceneDecoratorStrategies, selectedTab, onTabSelected
```
