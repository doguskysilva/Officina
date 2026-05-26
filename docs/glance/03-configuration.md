# Glance — Widget Configuration Activity

## Declarar no Manifest

```xml
<!-- Activity de configuração -->
<activity android:name=".ExampleAppWidgetConfigurationActivity">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_CONFIGURE"/>
    </intent-filter>
</activity>

<!-- Referenciar no appwidget-provider (namespace completo) -->
<appwidget-provider
    android:configure="com.example.android.ExampleAppWidgetConfigurationActivity"
    ...>
</appwidget-provider>
```

## Implementar a Activity — passo a passo

```kotlin
class ExampleAppWidgetConfigurationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Obter o widget ID do intent
        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // 2. Resultado padrão = CANCELED (se o usuário sair sem configurar)
        val cancelResult = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, cancelResult)

        // 3. ... configurar widget conforme preferências do usuário (salvar em DataStore, etc.) ...

        // 4. Obter GlanceId a partir do platform widget ID
        val glanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)

        // 5. Atualizar o widget (suspend — chamar em coroutine/lifecycleScope)
        lifecycleScope.launch {
            ExampleGlanceWidget().update(this@ExampleAppWidgetConfigurationActivity, glanceId)
        }

        // 6. Retornar OK e fechar
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}
```

> **Atenção:** O sistema NÃO dispara `ACTION_APPWIDGET_UPDATE` quando a configuration activity é lançada. É responsabilidade da activity chamar `update()` na primeira criação.

## Widget configuration options (Android 12+)

```xml
<!-- Apenas reconfigurável: abre configuração na criação, permite reconfigurar depois -->
<appwidget-provider
    android:configure="com.myapp.ExampleAppWidgetConfigurationActivity"
    android:widgetFeatures="reconfigurable">
</appwidget-provider>

<!-- Configuração opcional: pula configuração inicial, ainda permite reconfigurar -->
<appwidget-provider
    android:configure="com.myapp.ExampleAppWidgetConfigurationActivity"
    android:widgetFeatures="reconfigurable|configuration_optional">
</appwidget-provider>
```

| `widgetFeatures` | Comportamento |
|---|---|
| _(omitido)_ | Abre configuração na criação; não permite reconfigurar depois |
| `reconfigurable` | Abre configuração na criação; usuário pode reconfigurar (touch & hold → Reconfigure) |
| `reconfigurable\|configuration_optional` | Pula configuração inicial (usa defaults); usuário pode reconfigurar depois |
