# Módulo 5 — BottomSheetSceneStrategy: o terceiro tipo de Scene

## Os três tipos de Scene no Navigation3

| Tipo | Quando usar | Decorator aplicado? |
|---|---|---|
| `Scene` | Tela normal (Single pane, ListDetail) | ✅ Sim |
| `OverlayScene` (Dialog) | Confirmações, alertas | ❌ Não |
| `OverlayScene` (BottomSheet) | Ações rápidas, filtros, ordenação | ❌ Não |

`Dialog` e `BottomSheet` são ambos `OverlayScene` — a diferença está só no composable que envolve o conteúdo.

---

## Por que BottomSheet é OverlayScene

Se `BottomSheetScene` fosse um `Scene` normal, o `NavDecoratorStrategy` envolveria seu conteúdo com `NavigationBar`. Visualmente o `ModalBottomSheet` cobriria a barra de qualquer forma (ele usa `Popup` internamente), mas a barra continuaria sendo renderizada desnecessariamente.

Como `OverlayScene`, `NavDisplay` garante que o decorator nunca é chamado.

---

## A API real do Navigation3

Descoberta inspecionando o bytecode da biblioteca (Navigation3 1.1.2):

```kotlin
// NavEntry expõe:
val contentKey: Any          // chave pública para identificar o entry (Scene.key)
fun Content()                // @Composable que renderiza o conteúdo do entry

// SceneStrategyScope expõe:
val onBack: () -> Unit       // callback do NavDisplay — cobre back físico e swipe do sheet

// OverlayScene exige (além de Scene):
val overlaidEntries: List<NavEntry<T>>  // entries visíveis sob o overlay
```

---

## Implementação: BottomSheetSceneStrategy

```kotlin
class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

    companion object {
        fun bottomSheet(): Map<String, Any> = mapOf(BOTTOM_SHEET_KEY to Unit)
    }

    override fun SceneStrategyScope<T>.calculateScene(
        entries: List<NavEntry<T>>,
    ): Scene<T>? {
        val topEntry = entries.lastOrNull() ?: return null
        if (!topEntry.metadata.containsKey(BOTTOM_SHEET_KEY)) return null
        val underlaidEntries = entries.dropLast(1)
        return BottomSheetScene(
            sheetEntry = topEntry,
            previousEntries = underlaidEntries,
            overlaidEntries = underlaidEntries,
            onDismiss = onBack,     // SceneStrategyScope.onBack — sem callback externo
        )
    }
}
```

`onBack` vem do `SceneStrategyScope` — o mesmo callback que o `NavDisplay` usa. Isso garante que swipe down, toque fora e botão voltar do sistema todos disparam o mesmo caminho de remoção do back stack. Não precisamos injetar nenhum callback manual.

---

## Implementação: BottomSheetScene

```kotlin
class BottomSheetScene<T : Any>(
    private val sheetEntry: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val onDismiss: () -> Unit,
) : OverlayScene<T> {

    override val key: Any = sheetEntry.contentKey
    override val entries = listOf(sheetEntry)

    override val content: @Composable () -> Unit = {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
        ) {
            sheetEntry.Content()    // método público de NavEntry
        }
    }
}
```

### previousEntries vs overlaidEntries

| Propriedade | Para que serve |
|---|---|
| `previousEntries` | Entries para onde o "back" retorna (navigation) |
| `overlaidEntries` | Entries renderizados por baixo do overlay (visual) |

Para o BottomSheet ambos apontam para os mesmos entries — o que está embaixo É de onde voltamos ao fechar.

---

## Diferença em relação ao Dialog

| Aspecto | DialogSceneStrategy | BottomSheetSceneStrategy |
|---|---|---|
| Composable wrapper | `Dialog { ... }` | `ModalBottomSheet { ... }` |
| Dismiss via swipe | ❌ Não aplicável | ✅ Sim (`onDismissRequest`) |
| Dismiss via back | ✅ `NavDisplay` BackHandler | ✅ `onBack` do scope |
| Implementação | Biblioteca | Custom (nosso código) |

---

## Ordem em sceneStrategies importa

```kotlin
sceneStrategies = listOf(
    dialogStrategy,       // 1º — maior prioridade, OverlayScene
    bottomSheetStrategy,  // 2º — OverlayScene também
    listDetailStrategy,   // 3º — aplica-se ao conteúdo base
)
```

`NavDisplay` percorre a lista em ordem. A primeira strategy que retorna não-null vence. Dialog e BottomSheet devem vir antes de ListDetail para que entries marcados como overlay não acabem num painel lateral.

---

## Como foi aplicado no Officina

```kotlin
// Routes.kt
@Serializable data object SortProjects : NavKey

// MainActivity.kt
entry<SortProjects>(
    metadata = BottomSheetSceneStrategy.bottomSheet()
) {
    SortProjectsSheet()
}
```

Botão de ordenar no `ProjectListScreen` abre `SortProjects` no back stack → `BottomSheetSceneStrategy` detecta o metadata → cria `BottomSheetScene` → `ModalBottomSheet` aparece por cima da lista de projetos.

---

## Arquivos criados/modificados

```
scenes/
└── BottomSheetSceneStrategy.kt   ← novo

screens/
├── SortProjectsSheet.kt          ← novo
└── ProjectListScreen.kt          ← + botão Sort na TopAppBar

navigation/Routes.kt              ← + SortProjects
MainActivity.kt                   ← + bottomSheetStrategy + entry<SortProjects>
```
