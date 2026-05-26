# Glance — State Management & Update

## GlanceAppWidget é stateless

A classe é reinstanciada a cada criação/update. Qualquer estado in-memory pode ser destruído a qualquer momento — o widget vive em processo remoto.

### Tipos de estado

| Tipo | Descrição | Exemplo |
|---|---|---|
| **Application state** | Dados do app que o widget exibe | Lista do banco de dados, cache de repositório |
| **Glance state** | Estado exclusivo do widget | Checkbox marcado, contador local |

---

## Consumir application state

```kotlin
class DestinationAppWidget : GlanceAppWidget() {

    @Composable
    fun MyContent() {
        val repository = remember { DestinationsRepository.getInstance() }
        val destinations by repository.destinations.collectAsState(State.Loading)

        when (destinations) {
            is State.Loading   -> { /* loading UI */ }
            is State.Error     -> { /* error UI */ }
            is State.Completed -> { /* show list */ }
        }
    }
}
```

> O repositório/app é responsável por notificar o widget quando os dados mudam. O widget não faz polling — é passivo.

---

## Atualizar o widget

Glance recria o `RemoteViews` e o envia novamente ao host a cada `update`.

### Update por GlanceId

```kotlin
// GlanceId direto
MyAppWidget().update(context, glanceId)

// Obter todos os GlanceIds de um widget
val manager = GlanceAppWidgetManager(context)
val glanceIds = manager.getGlanceIds(MyAppWidget::class.java)
glanceIds.forEach { MyAppWidget().update(context, it) }
```

### Extensions de conveniência

```kotlin
// Atualiza todas as instâncias colocadas
MyAppWidget().updateAll(context)

// Atualiza apenas instâncias onde o estado bate com o predicado
MyAppWidget().updateIf<State>(context) { state ->
    state == State.Completed
}
```

> São `suspend functions` — chamar fora da main thread.

### Exemplo: update num CoroutineWorker

```kotlin
class DataSyncWorker(
    val context: Context,
    val params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        MyAppWidget().updateAll(context)
        return Result.success()
    }
}
```

---

## Quando atualizar

### Imediatamente (app acordado)

- Interação do usuário com o widget (action, lambda, intent)
- Usuário usando o app em foreground
- Resposta a FCM ou broadcast

→ chamar `update()` diretamente

### Periodicamente (app dormindo)

| Mecanismo | Cadência mínima | Uso |
|---|---|---|
| `updatePeriodMillis` | 30 min | Simples, declarado no XML |
| `WorkManager` | 15 min | Mais controle, updates frequentes |
| Broadcast receiver | Evento externo | Push, alarme, etc. |

> **Não atualizar a cada minuto com o app dormindo** — consome bateria.
