# Glance — Generated Widget Previews (Android 15+)

## Visão geral

- **Android 15+**: use `GlanceAppWidget.providePreview` + `GlanceAppWidgetManager.setWidgetPreviews`
- **Android 14-**: use `previewImage` no `appwidget-provider` (fallback estático)
- `setWidgetPreview` é **rate-limited** (~2 chamadas/hora) — não abusar

## Setup

`compileSdk` deve ser 35+ no `build.gradle.kts`:

```kotlin
android {
    compileSdk = 35
}
```

## Implementação com Glance

```kotlin
class MyAppWidget : GlanceAppWidget() {

    // Preview: composição única, sem recomposição nem efeitos
    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            MyContent(data = loadPreviewData())
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = loadRealData()
        provideContent {
            MyContent(data = data)
        }
    }
}
```

Publicar o preview:

```kotlin
// Chamar quando o app tem dados prontos (ex: após login, launch inicial, ou task periódica)
GlanceAppWidgetManager(context).setWidgetPreviews(
    provider = MyAppWidget(),
    widgetCategory = AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
)
```

### Quando chamar `setWidgetPreviews`

| Caso | Estratégia |
|---|---|
| Widget estático / quick action | Na primeira vez que o app é lançado |
| Widget com dados do usuário | Após sign-in ou setup inicial |
| Widget com dados dinâmicos | Task periódica com cadência escolhida |

## Resolver elementos faltando no preview

**Causa:** por padrão, `previewSizeMode = SizeMode.Single` — renderiza apenas no tamanho mínimo (`minWidth`/`minHeight`).

**Fix:** sobrescrever `previewSizeMode` para `SizeMode.Responsive`:

```kotlin
class MyAppWidget : GlanceAppWidget() {

    // Override para o preview mostrar corretamente todos os elementos
    override val previewSizeMode = SizeMode.Responsive(
        setOf(
            DpSize(110.dp, 50.dp),   // tamanho mínimo
            DpSize(220.dp, 100.dp),  // breakpoint maior
        )
    )

    override val sizeMode = SizeMode.Exact // para o widget real, prefira Exact
}
```

> `SizeMode.Responsive` no `previewSizeMode` ≠ `sizeMode` do widget real. Para o widget em produção, `SizeMode.Exact` é preferido.

## Backward compatibility

```xml
<appwidget-provider
    android:previewImage="@drawable/my_widget_preview"
    android:previewLayout="@layout/my_widget_preview_layout">
</appwidget-provider>
```

- `previewLayout` (Android 12+): XML estático escalável
- `previewImage` (Android 11-): drawable screenshot estático
- Se mudar o visual do widget, atualizar a imagem de preview
