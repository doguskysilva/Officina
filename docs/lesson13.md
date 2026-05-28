# Módulo 13 — Compose Previews

## Por que investir em previews

Previews permitem iterar na UI sem instalar o app. No Android Studio você vê todas as variações (dark, tablet, font scale, dynamic colors) lado a lado em segundos. O ponto central é que previews são estáticos — não executam `LaunchedEffect`, animações ou lógica de negócio. Qualquer estado que precise aparecer tem que ser passado diretamente como parâmetro ou construído nos dados fake.

---

## @Preview básico

```kotlin
@Preview(
    name = "Phone · Dark",
    device = "spec:width=411dp,height=891dp",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun MinhaTelaPreview() {
    OfficinaTheme {
        MinhaTela(...)
    }
}
```

Parâmetros úteis:

| Parâmetro | Tipo | Exemplo |
|---|---|---|
| `name` | `String` | `"Phone · Dark"` |
| `device` | `String` | `"spec:width=411dp,height=891dp"` ou `Devices.TABLET` |
| `uiMode` | `Int` | `Configuration.UI_MODE_NIGHT_YES` |
| `wallpaper` | `Int` | `Wallpapers.BLUE_DOMINATED_EXAMPLE` |
| `fontScale` | `Float` | `1.5f` |
| `showSystemUi` | `Boolean` | `true` — renderiza status/nav bar |

---

## Anotações multi-preview customizadas

Empilhar múltiplos `@Preview` em uma única anotação — aplica todas as variações de uma vez em qualquer composable.

```kotlin
@Preview(name = "Phone", device = "spec:width=411dp,height=891dp")
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(name = "Foldable", device = "spec:width=673dp,height=841dp")
annotation class DevicePreviews
```

Uso:

```kotlin
@DevicePreviews
@Composable
private fun ProjectListScreenPreview() { ... }
```

Gera 3 previews automaticamente. As anotações do projeto:

| Anotação | Variações geradas |
|---|---|
| `@DevicePreviews` | Phone · Phone Landscape · Tablet · Foldable |
| `@LightDarkPreviews` | Light · Dark |
| `@DynamicColorPreviews` | 4 wallpapers (Red / Blue / Green / Yellow) |
| `@FontScalePreviews` | 85% · 100% · 150% · 200% |
| `@OfficinaPreviews` | Phone+Tablet × Light+Dark (4 de uma vez) |

### Quando usar cada uma

- **`@OfficinaPreviews`** — telas principais com layout adaptativo. Cobre os casos mais comuns.
- **`@LightDarkPreviews`** — dialogs, bottom sheets, telas simples. `@OfficinaPreviews` seria excessivo.
- **`@DynamicColorPreviews`** — separado de propósito. Dynamic colors são uma dimensão independente de dispositivo.
- **`@DevicePreviews`** — quando landscape e foldable são relevantes além do phone/tablet padrão.
- **`@FontScalePreviews`** — validar acessibilidade. Textos longos e layouts quebram muito antes de 200%.

`@LightDarkPreviews` dentro de uma tela que já usa `@OfficinaPreviews` é **redundante** — OfficinaPreviews já cobre Phone·Light e Phone·Dark.

---

## PreviewParameterProvider — múltiplos estados com um preview

Alimenta a mesma função de preview com dados diferentes sem duplicar código.

```kotlin
class ProjectListStateProvider : PreviewParameterProvider<UiState<List<Project>>> {
    override val values = sequenceOf(
        UiState.Loading,
        UiState.Success(emptyList()),
        UiState.Success(fakeProjects),
        UiState.Error("Falha ao carregar projetos"),
    )
}
```

Uso com `@PreviewParameter`:

```kotlin
@OfficinaPreviews
@Composable
private fun ProjectListScreenPreview(
    @PreviewParameter(ProjectListStateProvider::class) uiState: UiState<List<Project>>,
) {
    OfficinaTheme {
        ProjectListScreen(uiState = uiState, ...)
    }
}
```

O Android Studio multiplica automaticamente: 4 estados × 4 variações do `@OfficinaPreviews` = **16 previews** de uma única função.

---

## Dados fake centralizados

Todos os dados de preview em `ui/preview/PreviewData.kt` — evita duplicação entre arquivos e garante consistência.

```kotlin
val fakeProjects = listOf(
    Project(id = 1, name = "App Mobile", tasks = listOf(
        Task(1, "Tela de login", done = true),
        Task(2, "Tela home", done = true),
        Task(3, "Integração OAuth"),
    )),
    // ...
)
```

---

## Estrutura de arquivos

```
screens/
  ProjectListScreenPreviews.kt    ← previews separados da tela
  ProjectDetailScreenPreviews.kt
  TaskListScreenPreviews.kt
  SimpleScreenPreviews.kt         ← dialogs e telas sem layout adaptativo
ui/
  preview/
    PreviewAnnotations.kt         ← anotações reutilizáveis
    PreviewData.kt                ← dados fake + providers
```

Manter previews em arquivos `*Previews.kt` separados mantém os arquivos de tela limpos e deixa claro que aquele código existe só para tooling.

---

## Limitações importantes

- **Estado interno não é previewável** — `rememberSearchBarWithGapState()` e similares não podem ser forçados para um estado específico num preview estático. Para isso, hoist o estado como parâmetro opcional ou use o Layout Inspector com o app rodando.
- **`LaunchedEffect` não executa** — previews são estáticos. Animações e efeitos colaterais não rodam.
- **`ViewModel` não funciona** — telas que dependem de ViewModel precisam ser divididas em composable stateless (que recebe `uiState`) + stateful (que cria o ViewModel). Nosso projeto já usa esse padrão.
