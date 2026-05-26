# Glance — Manifest, Metadata e estrutura base

## Receiver no AndroidManifest.xml

```xml
<receiver android:name=".glance.MyAppWidgetReceiver"
          android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/my_app_widget_info" />
</receiver>
```

## AppWidgetProviderInfo — `res/xml/my_app_widget_info.xml`

```xml
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="80dp"
    android:minHeight="80dp"
    android:targetCellWidth="2"
    android:targetCellHeight="2"
    android:minResizeWidth="40dp"
    android:minResizeHeight="40dp"
    android:maxResizeWidth="250dp"
    android:maxResizeHeight="120dp"
    android:updatePeriodMillis="86400000"
    android:description="@string/example_appwidget_description"
    android:previewLayout="@layout/example_appwidget_preview"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:widgetFeatures="reconfigurable|configuration_optional">
</appwidget-provider>
```

### Sizing: Android 12+ vs 11-

| Atributo | Android 12+ | Android 11- |
|---|---|---|
| `targetCellWidth/Height` | Tamanho padrão em células de grid | Ignorado |
| `minWidth/Height` | Fallback se targetCell não suportado | Tamanho padrão em dp, arredondado para célula |
| `minResizeWidth/Height` | Tamanho mínimo absoluto ao redimensionar | — |
| `maxResizeWidth/Height` | Tamanho máximo recomendado (Android 12+) | — |
| `resizeMode` | `horizontal`, `vertical`, `horizontal\|vertical`, `none` | — |

### Outros atributos relevantes

| Atributo | Descrição |
|---|---|
| `updatePeriodMillis` | Frequência de update via `onUpdate()` — máximo recomendado: 1h (3600000) |
| `initialLayout` | Layout de loading antes do Glance compor; use `@layout/glance_default_loading_layout` |
| `previewLayout` (12+) / `previewImage` (11-) | Preview no widget picker |
| `configure` | Activity de configuração opcional |
| `widgetFeatures` | `reconfigurable`, `configuration_optional` |

---

## GlanceAppWidget

```kotlin
class MyAppWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // provideGlance roda na main thread
        // use withContext para operações longas
        provideContent {
            MyContent()
        }
    }

    @Composable
    private fun MyContent() {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Where to?", modifier = GlanceModifier.padding(12.dp))
            Row(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(text = "Home", onClick = actionStartActivity<MyActivity>())
                Button(text = "Work", onClick = actionStartActivity<MyActivity>())
            }
        }
    }
}
```

> **ATENÇÃO:** `androidx.glance.*` tem composables próprios (`Column`, `Row`, `Text`, `Button`) que NÃO são os do Compose UI. Importar errado causa erros de compilação. Use `as` para disambiguar se necessário.

## GlanceAppWidgetReceiver

```kotlin
class MyAppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MyAppWidget()
}
```

### Callbacks do receiver

| Callback | Comportamento em Glance |
|---|---|
| `onUpdate()` | Glance já implementa — se sobrescrever, **chamar `super.onUpdate()`** |
| `onAppWidgetOptionsChanged()` | Chamado ao adicionar ou redimensionar |
| `onDeleted()` | Instância específica removida |
| `onEnabled()` | Primeira instância criada — bom para migrações globais |
| `onDisabled()` | Última instância removida |
| `onReceive()` | Sempre chamar `super.onReceive()` — **nunca usar `goAsync()`** (Glance já é async) |

> **AVISO:** Receivers têm limite de 10s de background. Operações longas (queries, rede) → delegar ao `WorkManager`, depois chamar `GlanceAppWidget().update()`.

---

## Rounded corners (Android 12+)

```kotlin
// Composição — aplica o raio do sistema dinamicamente
GlanceModifier.cornerRadius(android.R.dimen.system_app_widget_background_radius)
```

Para backward compat (Android 11-):

- `res/values/attrs.xml` → atributo `backgroundRadius`
- `res/values/styles.xml` → fallback com dimen customizado
- `res/values-31/styles.xml` → usa `@android:dimen/system_app_widget_background_radius`
- `res/drawable/my_widget_background.xml` → shape com `?attr/backgroundRadius`
