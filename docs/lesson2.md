# Módulo 2 — Scenes: a alma do Navigation3

## O problema que Scenes resolvem

No Módulo 1, o `NavDisplay` sempre exibiu um destino por vez. Mas num tablet você quer lista + detalhe lado a lado. No Nav2 isso exigia lógica manual e layouts separados.

No Nav3, quem decide **como** renderizar o back stack é a `SceneStrategy`. Ela olha a lista e responde: *"consigo formar uma Scene com esses entries? Se sim, qual?"*

```
[ProjectList, ProjectDetail]
        ↓
  SceneStrategy pergunta:
  "janela larga + tem List + tem Detail no back stack?"
        ↓ sim (tablet)              ↓ não (phone)
  ListDetailScene             SinglePaneScene
  (dois painéis lado a lado)  (só o último entry)
```

---

## Os 3 conceitos novos

| Conceito | O que é |
|---|---|
| `Scene` | Renderiza 1 ou N `NavEntry`s. Define o `content` (o layout). |
| `SceneStrategy` | Decide qual `Scene` criar dado o back stack atual. Retorna `null` se não conseguir. |
| `metadata` | Canal de comunicação do `NavEntry` para a `Scene`. É assim que ela sabe quem é lista e quem é detalhe. |

**O fluxo completo:**

```
back stack → NavDisplay → [Strategy1, Strategy2, ...] → Scene → tela
                                   ↑
                         primeira que não retornar null vence
                         fallback: SinglePaneSceneStrategy
```

---

## O padrão que se repete em todas as Scenes

```kotlin
// 1. Criar a strategy e registrá-la no NavDisplay
NavDisplay(
    sceneStrategies = listOf(minhaStrategy),
    ...
)

// 2. Marcar os entries com metadata
entry<MeuDestino>(
    metadata = MinhaStrategy.marcador()
) { ... }
```

Só isso. O entry não sabe como vai ser exibido — quem decide é a Strategy.

---

## Etapa 1 — DialogSceneStrategy (built-in)

### O que foi criado

- `Routes.kt`: novo NavKey `ConfirmDelete(projectId, projectName)`
- `ConfirmDeleteDialog.kt`: composable normal (não sabe que está num Dialog)
- `ProjectDetailScreen.kt`: botão "Excluir projeto" que navega para `ConfirmDelete`
- `MainActivity.kt`: `DialogSceneStrategy` registrada e entry marcado com `dialog()`

### Código central

```kotlin
val dialogStrategy = DialogSceneStrategy<NavKey>()

NavDisplay(
    sceneStrategies = listOf(dialogStrategy),
    entryProvider = entryProvider<NavKey> {

        // Entry normal — não tem metadata especial
        entry<ProjectDetail> { route -> ProjectDetailScreen(...) }

        // Entry marcado como dialog → DialogSceneStrategy o captura
        entry<ConfirmDelete>(
            metadata = DialogSceneStrategy.dialog()
        ) { route ->
            ConfirmDeleteDialog(...)   // Composable normal, sem saber do Dialog
        }
    }
)
```

### O que é OverlayScene

`Dialog` é um `OverlayScene`: não substitui o conteúdo anterior, renderiza **sobre** ele. Por isso a tela de detalhe continua visível por baixo do diálogo.

```
back stack:  [ProjectList, ProjectDetail, ConfirmDelete]
                                               ↓
                                    DialogSceneStrategy assume
                                    ProjectDetail continua visível atrás
```

### Manipulando o back stack no onConfirm

```kotlin
onConfirm = {
    backStack.removeLastOrNull() // remove ConfirmDelete
    backStack.removeLastOrNull() // remove ProjectDetail
    // resultado: [ProjectList]
}
```

---

## Etapa 2 — ListDetailScene customizada (do zero)

### Por que construir do zero antes de usar a pronta

Entender a implementação interna faz toda a diferença. A versão Material Adaptive é só uma versão mais rica do mesmo padrão.

### Arquivo: `scenes/ListDetailScene.kt`

#### A Scene — define o layout

```kotlin
class ListDetailScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val listEntry: NavEntry<T>,
    val detailEntry: NavEntry<T>,
) : Scene<T> {

    override val entries = listOf(listEntry, detailEntry)

    override val content: @Composable () -> Unit = {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(0.4f)) {
                listEntry.Content()
            }
            VerticalDivider()
            // AnimatedContent: anima só a troca de item, não a Scene inteira
            AnimatedContent(
                targetState = detailEntry,
                contentKey = { it.contentKey },
                ...
            ) { entry -> entry.Content() }
        }
    }
}
```

**Ponto importante:** a `Scene` não sabe nada sobre `WindowSizeClass` ou `NavKey`. Ela só sabe renderizar dois entries num `Row`. Quem decide quando usá-la é a `SceneStrategy`.

#### A SceneStrategy — decide quando usar a Scene

```kotlin
class ListDetailSceneStrategy<T : Any>(
    private val windowSizeClass: WindowSizeClass
) : SceneStrategy<T> {

    // Keys ficam na Strategy — ela define os critérios de seleção
    object ListKey : NavMetadataKey<Boolean>
    object DetailKey : NavMetadataKey<Boolean>

    companion object {
        fun listPane() = metadata { put(ListKey, true) }
        fun detailPane() = metadata { put(DetailKey, true) }
    }

    override fun SceneStrategyScope<T>.calculateScene(
        entries: List<NavEntry<T>>
    ): Scene<T>? {

        // Condição 1: janela estreita → não faço nada, SinglePane assume
        if (!windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            return null
        }

        // Condição 2: último entry marcado como detalhe?
        val detailEntry = entries.lastOrNull()
            ?.takeIf { it.metadata.contains(DetailKey) }
            ?: return null

        // Condição 3: tem algum entry marcado como lista?
        val listEntry = entries.findLast { it.metadata.contains(ListKey) }
            ?: return null

        return ListDetailScene(
            // key = contentKey da lista: trocar o item selecionado
            // recompõe só o painel direito, não anima a Scene toda
            key = listEntry.contentKey,
            previousEntries = entries.dropLast(1),
            listEntry = listEntry,
            detailEntry = detailEntry,
        )
    }
}

@Composable
fun <T : Any> rememberListDetailSceneStrategy(): ListDetailSceneStrategy<T> {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return remember(windowSizeClass) { ListDetailSceneStrategy(windowSizeClass) }
}
```

### Por que `key = listEntry.contentKey`?

O `NavDisplay` anima entre Scenes quando a `key + class` da Scene muda. Se eu usasse `key = detailEntry.contentKey`, cada troca de item selecionado dispararia uma animação da Scene inteira. Com `key = listEntry.contentKey`, a Scene continua a mesma — só o `AnimatedContent` interno anima o painel direito.

### Registrando no NavDisplay

```kotlin
val dialogStrategy = DialogSceneStrategy<NavKey>()
val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

NavDisplay(
    sceneStrategies = listOf(dialogStrategy, listDetailStrategy),
    // Ordem importa:
    // 1. Dialog tem prioridade (é overlay, precisa vir primeiro)
    // 2. ListDetail avalia se a janela é larga
    // 3. SinglePane (fallback automático)
    entryProvider = entryProvider<NavKey> {

        entry<ProjectList>(
            metadata = ListDetailSceneStrategy.listPane()  // marca como lista
        ) {
            ProjectListScreen(
                onProjectClick = { route ->
                    backStack.removeIf { it is ProjectDetail } // evita acumular detalhes
                    backStack.add(route)
                }
            )
        }

        entry<ProjectDetail>(
            metadata = ListDetailSceneStrategy.detailPane()  // marca como detalhe
        ) { route ->
            ProjectDetailScreen(...)
        }
    }
)
```

---

## Comportamento no device

| Device | Back stack | Scene usada |
|---|---|---|
| Phone (portrait) | `[ProjectList]` | `SinglePaneScene` — exibe lista |
| Phone (portrait) | `[ProjectList, ProjectDetail]` | `SinglePaneScene` — exibe detalhe |
| Tablet | `[ProjectList]` | `SinglePaneScene` — só lista (sem detalhe ainda) |
| Tablet | `[ProjectList, ProjectDetail]` | `ListDetailScene` — lista + detalhe lado a lado |
| Qualquer | `[..., ConfirmDelete]` | `DialogSceneStrategy` — overlay dialog |

---

## O que os docs oficiais confirmaram

A documentação do Android coloca as keys (`ListKey`, `DetailKey`) na `ListDetailSceneStrategy`, não na `ListDetailScene`. Faz sentido: a Strategy define os critérios de seleção, então ela deve guardar os marcadores. A Scene só renderiza.

```kotlin
// Padrão dos docs — keys na Strategy:
class ListDetailSceneStrategy<T> {
    object ListKey : NavMetadataKey<Boolean>   // ← na strategy
    object DetailKey : NavMetadataKey<Boolean>
    companion object {
        fun listPane() = metadata { put(ListKey, true) }
        fun detailPane() = metadata { put(DetailKey, true) }
    }
}
```

---

## Próximo passo — Etapa 3: versão Material Adaptive

A dependência `androidx.compose.material3.adaptive:adaptive-navigation3` traz uma `ListDetailSceneStrategy` pronta com:
- `detailPlaceholder`: conteúdo exibido no painel direito quando nenhum item está selecionado
- `extraPane`: terceiro painel (perfil, configurações, etc.)
- Suporte automático a foldables e diferentes estados de postura

---

## Arquivos criados/modificados neste módulo

```
navigation/
└── Routes.kt                    ← + ConfirmDelete NavKey

scenes/
└── ListDetailScene.kt           ← ListDetailScene + ListDetailSceneStrategy (novo)

screens/
├── ConfirmDeleteDialog.kt       ← novo
└── ProjectDetailScreen.kt       ← + botão Excluir

MainActivity.kt                  ← DialogSceneStrategy + ListDetailSceneStrategy
gradle/libs.versions.toml        ← + material3AdaptiveNav3, adaptive-navigation3
app/build.gradle.kts             ← + adaptive-navigation3 dep
```
