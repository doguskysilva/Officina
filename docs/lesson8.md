# Módulo 8 — Animações de transição + AnimationConfig feature flag

## A API de animação do NavDisplay

Descoberta inspecionando o bytecode de `navigation3-ui-runtime.jar`:

```kotlin
NavDisplay(
    backStack = ...,
    onBack = ...,
    transitionSpec = { ... },               // forward: AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform
    popTransitionSpec = { ... },            // back (botão/gesto)
    predictivePopTransitionSpec = { ... },  // back preditivo do Android (gesto em andamento)
    entryProvider = ...
)
```

Todos os três são opcionais — sem eles, Navigation3 usa `defaultTransitionSpec()` (slide + fade definido na lib).

`ContentTransform` é construído com `togetherWith`:
```kotlin
fadeIn(tween(300)) togetherWith fadeOut(tween(300))
```

---

## Fase 1 — hardcoded (o problema)

```kotlin
NavDisplay(
    transitionSpec    = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
    popTransitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
)
```

Para experimentar outro preset você edita 2 linhas em `MainActivity.kt`, conhece os nomes dos parâmetros e lembra de mudar ambos. Frágil para iteração.

---

## Fase 2 — AnimationConfig (o padrão)

### AnimationPreset — um data class com dois ContentTransforms

```kotlin
data class AnimationPreset(
    val enter: ContentTransform,  // forward
    val pop: ContentTransform,    // back
)
```

### Presets — os 5 estilos disponíveis

```kotlin
object Presets {
    val NONE             = AnimationPreset(EnterTransition.None togetherWith ExitTransition.None, ...)
    val FADE             = AnimationPreset(fadeIn(tween(300)) togetherWith fadeOut(tween(300)), ...)
    val SLIDE_HORIZONTAL = AnimationPreset(slideInHorizontally { it } togetherWith slideOutHorizontally { -it }, ...)
    val SLIDE_VERTICAL   = AnimationPreset(slideInVertically { it } togetherWith fadeOut(tween(150)), ...)
    val SCALE_FADE       = AnimationPreset((scaleIn(initialScale=0.92f) + fadeIn()) togetherWith ..., ...)
}
```

`pop` é o espelho de `enter`: se a entrada vem da direita, a saída vai para a direita.

### AnimationConfig — a feature flag

```kotlin
object AnimationConfig {
    // ← troque aqui para mudar as animações em todo o app
    val current = Presets.SLIDE_HORIZONTAL
}
```

Uma linha. Sem tocar no `NavDisplay` ou nos screens.

### NavDisplay — consumo

```kotlin
NavDisplay(
    transitionSpec    = { AnimationConfig.current.enter },
    popTransitionSpec = { AnimationConfig.current.pop },
    ...
)
```

O lambda recebe `AnimatedContentTransitionScope<Scene<T>>` como receiver, mas a gente ignora o `this` e devolve o `ContentTransform` precomputado do preset. Para animações que precisam do scope (ex: `slideIntoContainer`) seria necessário mover o cálculo para dentro do lambda.

---

## Por que ContentTransform como propriedade e não lambda

`ContentTransform` é só dados — `EnterTransition`, `ExitTransition`, `SizeTransform?` e `targetContentZIndex`. Nenhum desses precisa de contexto Composable para ser criado. Então os presets podem ser inicializados como propriedades top-level, não como lambdas.

Se precisasse de `slideIntoContainer` (que usa o scope), o `AnimationPreset` seria:
```kotlin
data class AnimationPreset(
    val enter: AnimatedContentTransitionScope<*>.() -> ContentTransform,
    val pop:   AnimatedContentTransitionScope<*>.() -> ContentTransform,
)
```

---

## Animação por entry via metadata

`NavDisplay` também expõe os parâmetros como metadata, permitindo animação diferente por destino:

```kotlin
entry<ProjectDetail>(
    metadata = ListDetailSceneStrategy.detailPane() +
               NavDisplay.transitionSpec { slideInHorizontally { it } togetherWith fadeOut() }
) { ... }
```

Não usamos no Officina, mas é o mecanismo que permite overrides por tela sem afetar o global.

---

## Dois níveis de animação

| Nível | Onde | Quando dispara |
|---|---|---|
| **Scene** | `NavDisplay.transitionSpec` | Quando a *scene ativa* muda (phone: sempre; tablet: troca de aba, dialog, bottom sheet) |
| **Conteúdo** | `AnimatedContent` dentro do entry | Quando o conteúdo dentro de uma scene existente muda |

No tablet, `ListDetailSceneStrategy` coloca `ProjectList` + `ProjectDetail` na mesma `Scene`. Trocar de projeto não troca a scene — o `NavDisplay` não anima. A solução é um `AnimatedContent` dentro do `entry<ProjectDetail>`, animando quando o `projectId` muda:

```kotlin
entry<ProjectDetail> { route ->
    val vm = viewModel(key = route.projectId.toString()) { ProjectDetailViewModel(route.projectId) }
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = route.projectId,
        transitionSpec = { AnimationConfig.current.enter },
        label = "ProjectDetailTransition",
    ) {
        ProjectDetailScreen(uiState = uiState, ...)
    }
}
```

Por que `targetState = route.projectId` funciona: o mesmo composable de `entry<ProjectDetail>` é reutilizado quando você troca de projeto (o subtree não é resetado — o mesmo comportamento que causou o bug do ViewModel no módulo anterior). Isso significa que `AnimatedContent` vê a mudança de valor e dispara a transição.

---

## Arquivos criados/modificados

```
ui/
├── AnimationPreset.kt  ← novo — data class + object Presets (NONE, FADE, SLIDE_H, SLIDE_V, SCALE_FADE)
└── AnimationConfig.kt  ← novo — feature flag (val current = Presets.SLIDE_HORIZONTAL)

MainActivity.kt         ← transitionSpec e popTransitionSpec apontam para AnimationConfig.current
```
