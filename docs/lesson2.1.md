# Módulo 2.1 — ListDetail com Material Adaptive (versão de produção)

## Por que existe esta versão

No Módulo 2 construímos nossa `ListDetailScene` e `ListDetailSceneStrategy` do zero. Isso foi intencional: entender o mecanismo interno faz toda a diferença.

Agora usamos a versão pronta da biblioteca `adaptive-navigation3`, que resolve três problemas que a nossa versão não tratava:

| Problema | Nossa versão | Material Adaptive |
|---|---|---|
| Nenhum item selecionado no tablet | Painel direito vazio | `detailPlaceholder` |
| Terceiro painel (perfil, extras) | Não suportado | `extraPane()` |
| Foldables e posturas de tela | Não tratado | Automático |

---

## Dependência

```toml
# libs.versions.toml
[versions]
material3AdaptiveNav3 = "1.3.0-beta02"

[libraries]
androidx-material3-adaptive-navigation3 = {
    group = "androidx.compose.material3.adaptive",
    name = "adaptive-navigation3",
    version.ref = "material3AdaptiveNav3"
}
```

```kotlin
// app/build.gradle.kts
implementation(libs.androidx.material3.adaptive.navigation3)
```

---

## Diferença no código

### Antes (nossa versão customizada)

```kotlin
// Importava nossa própria classe
import com.doguskytech.officina.scenes.ListDetailSceneStrategy
import com.doguskytech.officina.scenes.rememberListDetailSceneStrategy

val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()

entry<ProjectList>(
    metadata = ListDetailSceneStrategy.listPane()  // sem placeholder
)
```

### Depois (Material Adaptive)

```kotlin
// Importa da biblioteca
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy

val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

entry<ProjectList>(
    metadata = ListDetailSceneStrategy.listPane(
        detailPlaceholder = { ProjectDetailPlaceholder() }  // novo
    )
)
```

A API é idêntica — o que muda é de onde vêm as classes e os recursos extras.

---

## A diretiva (workaround bug b/418201867)

Por padrão, a biblioteca adiciona um espaçamento horizontal entre os painéis. O bug b/418201867 ainda não foi resolvido, então o padrão dos docs é sobrescrever:

```kotlin
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
// ...

val windowAdaptiveInfo = currentWindowAdaptiveInfo()
val directive = remember(windowAdaptiveInfo) {
    calculatePaneScaffoldDirective(windowAdaptiveInfo)
        .copy(horizontalPartitionSpacerSize = 0.dp)  // remove o espaço entre painéis
}
val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)
```

**Por que `remember(windowAdaptiveInfo)`?** A diretiva só precisa ser recalculada quando a configuração da janela muda (rotação, multitarefa, foldable). `remember` com essa chave evita recriação desnecessária.

---

## detailPlaceholder — o que é e quando aparece

```kotlin
entry<ProjectList>(
    metadata = ListDetailSceneStrategy.listPane(
        detailPlaceholder = { ProjectDetailPlaceholder() }
    )
)
```

- **No tablet** (janela larga): quando o back stack contém apenas `[ProjectList]`, o painel direito exibe o `detailPlaceholder`. Assim que o usuário clica num projeto e `ProjectDetail` entra no back stack, o placeholder é substituído pelo detalhe real.
- **No phone**: o `detailPlaceholder` **nunca é renderizado**. A biblioteca só o chama quando há dois painéis visíveis.

```
Tablet, back stack [ProjectList]:
┌─────────────────┬──────────────────────┐
│  Lista de        │ Selecione um projeto  │
│  projetos        │ (placeholder)         │
└─────────────────┴──────────────────────┘

Tablet, back stack [ProjectList, ProjectDetail(1)]:
┌─────────────────┬──────────────────────┐
│  Lista de        │  App Mobile          │
│  projetos        │  (detalhe real)       │
└─────────────────┴──────────────────────┘
```

---

## extraPane — terceiro painel

```kotlin
entry<Profile>(
    metadata = ListDetailSceneStrategy.extraPane()
)
```

Em janelas muito largas (desktops, tablets grandes em landscape), a biblioteca pode exibir três painéis simultaneamente: lista + detalhe + extra. Em janelas menores, o `extraPane` comporta-se como um entry normal empilhado sobre o detalhe.

O Módulo 2.2 aplica o `extraPane` à tela `NewTask` do Officina.

---

## Comportamento por dispositivo

| Device / Postura | Back stack | O que aparece |
|---|---|---|
| Phone portrait | `[ProjectList]` | Lista (tela cheia) |
| Phone portrait | `[ProjectList, ProjectDetail]` | Detalhe (tela cheia, com botão voltar) |
| Tablet landscape | `[ProjectList]` | Lista + placeholder |
| Tablet landscape | `[ProjectList, ProjectDetail]` | Lista + detalhe lado a lado |
| Tablet portrait | `[ProjectList, ProjectDetail]` | Depende da largura — pode ser 1 ou 2 painéis |
| Qualquer | `[..., ConfirmDelete]` | Dialog (overlay) |

---

## O que NÃO mudou

- O back stack continua sendo a mesma lista `rememberNavBackStack`
- `onBack = { backStack.removeLastOrNull() }` continua igual
- `DialogSceneStrategy` continua com prioridade na lista de strategies
- `backStack.removeIf { it is ProjectDetail }` antes de adicionar novo detalhe — evita acumular entries

---

## Arquivos modificados

```
MainActivity.kt               ← usa Material Adaptive rememberListDetailSceneStrategy
screens/
└── ProjectDetailPlaceholder.kt   ← novo: placeholder para painel direito vazio
```

O arquivo `scenes/ListDetailScene.kt` permanece no projeto como **referência de aprendizado** — é nossa implementação manual do mesmo padrão que a biblioteca usa internamente.
