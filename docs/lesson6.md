# Módulo 6 — NavigationSuiteScaffold: substituindo o NavDecoratorStrategy

## O problema que ele resolve

No Módulo 3 criamos `NavDecoratorStrategy` + `NavDecoratorScene` para envolver cada `Scene` com `NavigationBar` (phone) ou `NavigationRail` (tablet). Eram ~140 linhas, detectávamos `WindowSizeClass` manualmente e criávamos derived keys para não quebrar animações.

`NavigationSuiteScaffold` faz tudo isso em 4 linhas de configuração — e cobre um terceiro caso que nós ignorávamos: `NavigationDrawer` em desktops/telas muito largas.

---

## Os três tipos que ele gerencia automaticamente

| `NavigationSuiteType` | Quando | Componente |
|---|---|---|
| `NavigationBar` | Compact (phone) | `NavigationBar` na base |
| `NavigationRail` | Medium/Expanded (tablet) | `NavigationRail` na lateral |
| `NavigationDrawer` | Expandido muito largo (desktop) | `ModalNavigationDrawer` lateral |

A lógica de qual tipo usar é computada internamente via `WindowAdaptiveInfoDefault` — o mesmo `currentWindowAdaptiveInfoV2()` que usamos em outros lugares.

---

## API

```kotlin
NavigationSuiteScaffold(
    navigationItems = {
        // NavigationSuiteItem para cada destino de nível superior
    }
) {
    // conteúdo da tela (NavDisplay no nosso caso)
}
```

`NavigationSuiteItem` — mesma assinatura de `NavigationBarItem` e `NavigationRailItem`:

```kotlin
NavigationSuiteItem(
    icon = { Icon(Icons.Default.Build, contentDescription = null) },
    label = { Text("Projetos") },
    selected = selectedTab == ProjectList,
    onClick = { selectedTab = ProjectList },
)
```

---

## Por que dois overloads na API

A API original usa `navigationSuiteItems: NavigationSuiteScope.() -> Unit` — os itens são registrados via `item()` numa DSL própria. A partir de Material3 1.5.0-alpha20 foi adicionado um segundo overload com `navigationItems: @Composable () -> Unit`, onde você chama `NavigationSuiteItem` diretamente como composable normal. O segundo é preferido pois elimina a indireção da DSL.

---

## Diferença em relação ao NavDecoratorStrategy

| Aspecto | NavDecoratorStrategy (Módulo 3) | NavigationSuiteScaffold |
|---|---|---|
| Onde vive a navegação | Dentro do `NavDisplay` (SceneDecorator) | Fora do `NavDisplay` (wrapper) |
| Detecta WindowSizeClass | Manual (`currentWindowAdaptiveInfo()`) | Automático internamente |
| Cobre NavigationDrawer | ❌ Não | ✅ Sim |
| Linha de código | ~140 | ~20 |
| Derived key | Necessário (evitar bug de animação) | Não necessário |

A consequência mais importante: **`sceneDecoratorStrategies` foi removido do `NavDisplay`**. O `NavigationSuiteScaffold` não tem relação com o sistema de scenes — ele é apenas um layout wrapper em volta do `NavDisplay`.

---

## OverlayScene continua sem barra de navegação

Com `NavDecoratorStrategy`, `OverlayScene` (Dialog, BottomSheet) ficavam sem barra porque o `NavDisplay` não chamava o decorator para overlays.

Com `NavigationSuiteScaffold`, o comportamento é diferente mas o resultado é o mesmo: `Dialog` e `ModalBottomSheet` são composables que abrem sobre todo o conteúdo usando `Popup` — eles ficam sobre a `NavigationBar`/`Rail` visualmente, independente do wrapper.

---

## Arquivos alterados

```
scenes/
└── NavigationDecorator.kt   ← DELETADO (NavDecoratorStrategy, NavDecoratorScene, rememberNavDecoratorStrategy, appTabs, TabItem)

MainActivity.kt              ← sceneDecoratorStrategies removido, NavDisplay envolvido em NavigationSuiteScaffold
```
