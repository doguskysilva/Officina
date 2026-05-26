# Glance — User Interaction (Actions)

Actions são aplicadas via `GlanceModifier.clickable(action)` ou diretamente no parâmetro `onClick` dos composables.

> O widget roda em processo remoto — Actions são definidas na criação e executadas no processo remoto (equivalente a `PendingIntent` em `RemoteViews`).

---

## Lançar Activity

```kotlin
// Por classe
Button(text = "Go", onClick = actionStartActivity<MyActivity>())

// Por Intent
Button(text = "Go", onClick = actionStartActivity(
    Intent(context, MyActivity::class.java).apply { /* ... */ }
))
```

## Lançar Service

```kotlin
Button(
    text = "Sync",
    onClick = actionStartService<SyncService>(isForegroundService = true)
)
```

## Enviar Broadcast

```kotlin
Button(
    text = "Send",
    onClick = actionSendBroadcast<MyReceiver>()
)
```

---

## Ações customizadas

### Lambda inline

```kotlin
// No modifier
Text(
    text = "Submit",
    modifier = GlanceModifier.clickable { submitData() }
)

// No onClick
Button(text = "Submit", onClick = { submitData() })
```

> Lambdas rodam num worker do `WorkManager` (dentro de um `Service`). **Não iniciar Activities a partir de lambdas** em apps que targeteiam Android 12+ — use `actionStartActivity` para isso.

### ActionCallback (recomendado para lógica com acesso ao contexto)

```kotlin
// Composable
Image(
    provider = ImageProvider(R.drawable.ic_refresh),
    modifier = GlanceModifier.clickable(onClick = actionRunCallback<RefreshAction>()),
    contentDescription = "Refresh"
)

// Callback
class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        // Operações curtas aqui; operações longas → Worker
        MyAppWidget().update(context, glanceId)
    }
}
```

> `ActionCallback` é chamado num `BroadcastReceiver` async customizado do Glance — há tempo extra de execução, mas tarefas longas ainda devem ir para `Worker`.

---

## ActionParameters — passar dados para a Action

```kotlin
private val destinationKey = ActionParameters.Key<String>("destination")

// Na composição
Button(
    text = "Home",
    onClick = actionStartActivity<NavigationActivity>(
        actionParametersOf(destinationKey to "home")
    )
)

// Na Activity de destino
val destination = intent.extras?.getString("destination") ?: return

// No ActionCallback
override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
    val destination = parameters[destinationKey] ?: return
}
```

---

## Resumo

| Tipo | Função | Caso de uso |
|---|---|---|
| Abrir Activity | `actionStartActivity<T>()` | Navegar para tela do app |
| Abrir Service | `actionStartService<T>()` | Sync, foreground service |
| Broadcast | `actionSendBroadcast<T>()` | Eventos customizados |
| Lambda | `onClick = { ... }` | Ações simples sem contexto |
| Callback | `actionRunCallback<T>()` | Lógica com `context`/`glanceId`, update do widget |
