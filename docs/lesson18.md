# Módulo 18 — Shared Element Transitions com Navigation3

## O que vamos aprender

- O que são Shared Element Transitions e quando usá-las
- As duas primitivas: `sharedBounds` vs `sharedElement`
- Como integrar com Navigation3 via `LocalNavAnimatedContentScope`
- Por que a captura do scope importa (antes vs dentro do `AnimatedContent`)
- O padrão null-safe para manter previews funcionando

---

## Conceito: o que é uma Shared Element Transition?

Uma Shared Element Transition (ou Hero Animation) faz com que um elemento visual pareça **se mover fisicamente** de uma tela para outra durante a navegação — em vez de simplesmente sumir em uma tela e aparecer na outra.

O Compose implementa isso com a `SharedTransitionLayout`: ela mantém um "canvas" compartilhado entre as duas composições e anima a posição, tamanho e forma do elemento entre os dois destinos.

---

## Dois tipos de transição

### `sharedBounds` — Container Transform

Usado quando o **container** de um item precisa se expandir para se tornar a tela de destino. O conteúdo interno pode mudar; apenas os limites (bounds) são compartilhados.

```
Card pequeno ──────► Tela cheia
[  projeto  ]       ┌──────────┐
                    │  projeto │
                    │  detalhe │
                    └──────────┘
```

Usado em `ProjectCard` → `ProjectDetailScreen`.

### `sharedElement` — Hero Element

Usado quando o **mesmo conteúdo** precisa voar de uma posição para outra. O elemento é exatamente o mesmo; apenas a posição muda.

```
Card: [Officina]  ──────►  TopAppBar: [Officina]
```

Usado para o nome do projeto.

---

## API principal

```kotlin
// Fornece o SharedTransitionScope para todos os filhos
SharedTransitionLayout {
    val sharedScope = this  // SharedTransitionScope

    // Dentro dos entries, o AnimatedVisibilityScope vem daqui:
    val animScope = LocalNavAnimatedContentScope.current  // AnimatedVisibilityScope
}
```

### `sharedBounds` — anima os limites do container

```kotlin
with(sharedTransitionScope) {
    Modifier.sharedBounds(
        sharedContentState = rememberSharedContentState("unique_key"),
        animatedVisibilityScope = animatedVisibilityScope,
        enter = fadeIn(),
        exit = fadeOut(),
        resizeMode = ResizeMode.RemeasureToBounds,
    )
}
```

### `sharedElement` — anima um elemento específico

```kotlin
with(sharedTransitionScope) {
    Modifier.sharedElement(
        sharedContentState = rememberSharedContentState("unique_key"),
        animatedVisibilityScope = animatedVisibilityScope,
    )
}
```

**A chave (key) deve ser idêntica nos dois destinos** para o sistema saber o que parear.

---

## Integração com Navigation3

### 1. Envolva o `NavDisplay` em `SharedTransitionLayout`

```kotlin
// MainActivity.kt — dentro do NavigationSuiteScaffold
SharedTransitionLayout {
    val sharedScope = this
    NavDisplay(
        backStack = activeBackStack,
        ...
        entryProvider = entryProvider<NavKey> {
            // entries aqui
        }
    )
}
```

### 2. Capture `LocalNavAnimatedContentScope` dentro de cada entry

`LocalNavAnimatedContentScope` é uma `CompositionLocal` fornecida pelo `NavDisplay` que expõe o `AnimatedContentScope` da transição de navegação atual — que implementa `AnimatedVisibilityScope`.

```kotlin
entry<ProjectList>(...) {
    val animScope = LocalNavAnimatedContentScope.current
    ProjectListScreen(
        ...
        sharedTransitionScope = sharedScope,
        animatedVisibilityScope = animScope,
    )
}
```

### 3. Para entries com `AnimatedContent` interno: capture ANTES

O entry `ProjectDetail` usa um `AnimatedContent` interno para animar a troca de projetos no tablet. É crucial capturar o scope de navegação **antes** desse `AnimatedContent`:

```kotlin
entry<ProjectDetail>(...) { route ->
    // Captura o scope da transição de NAVEGAÇÃO — não do AnimatedContent interno
    val navAnimScope = LocalNavAnimatedContentScope.current

    AnimatedContent(
        targetState = route.projectId,    // ← troca de projeto no tablet
        ...
    ) { _ ->
        ProjectDetailScreen(
            ...
            animatedVisibilityScope = navAnimScope,  // scope de navegação, não de troca
        )
    }
}
```

Se capturasse dentro do `AnimatedContent`, o sistema usaria o scope da troca de projeto — e a transição de entrada/saída não aconteceria corretamente.

---

## Padrão null-safe para previews

As funções composable que recebem `SharedTransitionScope` e `AnimatedVisibilityScope` são chamadas tanto do `NavDisplay` (onde os scopes existem) quanto de arquivos de preview (onde não existem).

A solução: parâmetros opcionais com valor padrão `null`:

```kotlin
@Composable
fun ProjectListScreen(
    ...
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    // Modifier aplicado apenas se ambos os scopes estiverem disponíveis
    val sharedMod = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState("project_card_${project.id}"),
                animatedVisibilityScope = animatedVisibilityScope,
                enter = fadeIn(),
                exit = fadeOut(),
                resizeMode = ResizeMode.RemeasureToBounds,
            )
        }
    } else Modifier
}
```

Assim os previews continuam funcionando sem mudança.

---

## Opt-in necessário

`SharedTransitionScope`, `sharedBounds`, `sharedElement` e `rememberSharedContentState` são APIs experimentais. Use opt-in no nível de arquivo:

```kotlin
@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.doguskytech.officina.screens
```

E na Activity:

```kotlin
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
override fun onCreate(...) { ... }
```

---

## Convenção de chaves

Use chaves compostas por tipo + id para evitar colisão:

| Elemento | Chave |
|---|---|
| Container do card | `"project_card_${project.id}"` |
| Nome do projeto | `"project_name_${project.id}"` |

A chave é a mesma nos dois destinos — é assim que o sistema sabe o que parear.

---

## Ordem dos Modifiers

Para o `ProjectCard`, a ordem correta é:

```kotlin
ElevatedCard(
    modifier = modifier
        .fillMaxWidth()
        .then(cardSharedMod)    // ← sharedBounds antes de graphicsLayer
        .graphicsLayer {        // ← transformação de scale (animação de bounce)
            scaleX = scale.value
            scaleY = scale.value
        },
)
```

`sharedBounds` precisa "ver" os bounds antes de qualquer transformação gráfica, por isso vem antes do `graphicsLayer`.

---

## O que acontece em tempo de execução

1. Usuário toca no card → navegação é iniciada → `NavDisplay` inicia a transição
2. O sistema detecta que há dois `sharedBounds` com a mesma chave (um no card, um na tela de detalhe)
3. O card começa a "se expandir" suavemente para ocupar os bounds da tela de detalhe
4. Simultaneamente, o nome do projeto "voa" da posição no card para a posição na TopAppBar
5. Quando a transição termina, o card some e a tela de detalhe é completamente visível

Na volta (pop), o processo se inverte.

---

## Referências

- [Compose Shared Element Transitions](https://developer.android.com/develop/ui/compose/animation/shared-elements)
- `androidx.compose.animation.SharedTransitionLayout`
- `androidx.navigation3.ui.LocalNavAnimatedContentScope`
- Arquivos de referência: `screens/ProjectListScreen.kt`, `screens/ProjectDetailScreen.kt`, `MainActivity.kt`
