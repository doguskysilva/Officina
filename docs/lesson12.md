# Módulo 12 — Glance Widgets

## O que é o Glance

Glance é uma biblioteca do Jetpack que permite escrever widgets Android (`AppWidget`) usando uma API baseada em Composable. Internamente, o Glance traduz cada composable para `RemoteViews` — o sistema legado que o Android usa para renderizar conteúdo em superfícies externas ao app (home screen, lock screen). A escrita é parecida com Compose, mas as primitivas são diferentes: `Column`, `Row`, `Text`, `Image` e `LazyColumn` são do Glance, não do Compose UI.

---

## Estrutura base de um widget Glance

Todo widget precisa de três peças:

```kotlin
// 1. O widget em si
class SummaryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme { /* composables Glance aqui */ }
        }
    }
}

// 2. O receiver — ponte entre o sistema e o widget
class SummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SummaryWidget()
}

// 3. Registro no AndroidManifest.xml
// <receiver android:name=".widget.SummaryWidgetReceiver" android:exported="true">
//     <intent-filter>
//         <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
//     </intent-filter>
//     <meta-data android:name="android.appwidget.provider"
//                android:resource="@xml/summary_widget_info" />
// </receiver>
```

O arquivo `res/xml/summary_widget_info.xml` define tamanho, período de update, layout inicial e outros metadados.

---

## Widget 1 — Resumo Geral (`SummaryWidget`)

### SizeMode.Responsive — layouts por breakpoint

```kotlin
companion object {
    private val smallSize = DpSize(110.dp, 50.dp)
    private val mediumSize = DpSize(180.dp, 180.dp)
}

override val sizeMode = SizeMode.Responsive(setOf(smallSize, mediumSize))
```

O Glance pré-renderiza um layout para cada breakpoint. O sistema escolhe o melhor encaixe conforme o slot disponível na home screen. Dentro dos composables, `LocalSize.current` retorna o tamanho do breakpoint ativo — use para decidir qual layout renderizar:

```kotlin
val size = LocalSize.current
if (size.height >= mediumSize.height) MediumContent() else SmallContent()
```

### GlanceTheme e cores dinâmicas

```kotlin
provideContent {
    GlanceTheme { /* herda dynamic colors no Android 12+ */ }
}
```

`GlanceTheme.colors.primary`, `.surface`, `.onSurface` etc. mapeiam para as cores do wallpaper no Android 12+ e para o baseline M3 em versões anteriores.

### CircleIconButton (Glance 1.3.0-alpha01)

```kotlin
import androidx.glance.appwidget.components.CircleIconButton

CircleIconButton(
    imageProvider = ImageProvider(R.drawable.ic_launch),
    contentDescription = "Abrir app",
    onClick = actionStartActivity<MainActivity>(),
)
```

Substitui o `FilledButton` de texto por um botão circular com ícone vetorial. Requer um drawable de recurso — ícones do Compose Material (`Icons.Default.*`) não são diretamente compatíveis.

---

## Widget 2 — Lista de Projetos (`ProjectListWidget`)

### LazyColumn com itemId

```kotlin
LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
    items(projects, itemId = { it.id.toLong() }) { project ->
        ProjectRow(project)
    }
}
```

`LazyColumn` do Glance é traduzido para `ListView` em RemoteViews. O `itemId` fornece IDs estáveis para que o sistema recicle os itens corretamente.

### LinearProgressIndicator determinístico (Glance 1.3.0-alpha01)

```kotlin
import androidx.glance.appwidget.LinearProgressIndicator

LinearProgressIndicator(
    progress = done.toFloat() / total,  // 0.0 a 1.0
    modifier = GlanceModifier.fillMaxWidth(),
)
```

`CircularProgressIndicator` nessa versão é apenas indeterminado (sem parâmetro `progress`). Para mostrar percentual real de conclusão, use `LinearProgressIndicator`.

### ActionParameters — deep link para um projeto específico

```kotlin
val PROJECT_ID_KEY = ActionParameters.Key<Int>("project_id")
val PROJECT_NAME_KEY = ActionParameters.Key<String>("project_name")

// No item clicável:
GlanceModifier.clickable(
    actionStartActivity<MainActivity>(
        actionParametersOf(
            PROJECT_ID_KEY to project.id,
            PROJECT_NAME_KEY to project.name,
        )
    )
)
```

Os parâmetros são colocados como extras no Intent. No `MainActivity.onCreate`:

```kotlin
val deepLinkProjectId = intent.getIntExtra("project_id", -1).takeIf { it != -1 }
val deepLinkProjectName = intent.getStringExtra("project_name")

// Back stack inicial inclui o projeto se vier de deep link:
val projectsInitialStack: Array<NavKey> = if (
    savedInstanceState == null && deepLinkProjectId != null && deepLinkProjectName != null
) {
    arrayOf(ProjectList, ProjectDetail(deepLinkProjectId, deepLinkProjectName))
} else {
    arrayOf(ProjectList)
}
val projectsBackStack = rememberNavBackStack(*projectsInitialStack)
```

---

## Widget 3 — Projeto Configurável (`ProjectTasksWidget`)

Este é o widget mais complexo — apresenta três conceitos novos: Configuration Activity, estado persistido por instância, e callbacks de ação.

### PreferencesGlanceStateDefinition — estado por instância

```kotlin
class ProjectTasksWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    companion object {
        val PROJECT_ID_KEY = intPreferencesKey("selected_project_id")
    }
}
```

`PreferencesGlanceStateDefinition` dá a cada instância do widget seu próprio `DataStore<Preferences>`. Duas instâncias do mesmo widget tipo na home screen têm DataStores separados — cada uma lembra seu próprio projeto.

Leitura dentro do composable:

```kotlin
provideContent {
    val projectId = currentState<Preferences>()[PROJECT_ID_KEY]
    // ...
}
```

### ActionCallback — toggle de tarefa sem abrir o app

```kotlin
class ToggleTaskAction : ActionCallback {
    companion object {
        val TASK_ID_KEY = ActionParameters.Key<Int>("task_id")
        val PROJECT_ID_KEY = ActionParameters.Key<Int>("project_id")
    }

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[TASK_ID_KEY] ?: return
        val projectId = parameters[PROJECT_ID_KEY] ?: return
        ProjectRepository.toggleTask(projectId, taskId)
        ProjectTasksWidget().update(context, glanceId)  // força re-render
    }
}
```

Disparado no item da lista:

```kotlin
GlanceModifier.clickable(
    actionRunCallback<ToggleTaskAction>(
        actionParametersOf(
            ToggleTaskAction.TASK_ID_KEY to task.id,
            ToggleTaskAction.PROJECT_ID_KEY to projectId,
        )
    )
)
```

`ActionCallback` é executado como BroadcastReceiver — não abre o app, roda em background.

### Configuration Activity — os 4 contratos

Quando o widget tem `android:configure` no `appwidget-provider`, o sistema abre essa Activity antes de colocar o widget na home. O usuário escolhe as opções e a Activity termina com o resultado.

```kotlin
class ProjectTasksConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent.extras
            ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // Contrato 1: RESULT_CANCELED por padrão — se usuário der back, widget não aparece
        setResult(RESULT_CANCELED)

        // Contrato 2: ID inválido = finalizar imediatamente
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        setContent { /* UI de seleção de projeto */ }
    }

    private suspend fun saveSelection(appWidgetId: Int, projectId: Int) {
        // appWidgetId (Int do Android) → GlanceId (tipo do Glance)
        val glanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)

        // Contrato 3: salvar no DataStore da instância e forçar render
        updateAppWidgetState(this, glanceId) { prefs ->
            prefs[ProjectTasksWidget.PROJECT_ID_KEY] = projectId
        }
        ProjectTasksWidget().update(this, glanceId)

        // Contrato 4: RESULT_OK com o mesmo appWidgetId no Intent
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        finish()
    }
}
```

---

## Pacotes importantes (Glance 1.3.0-alpha01)

| Símbolo | Package |
|---|---|
| `GlanceAppWidget`, `SizeMode`, `GlanceAppWidgetManager` | `androidx.glance.appwidget` |
| `GlanceAppWidgetReceiver` | `androidx.glance.appwidget` |
| `LinearProgressIndicator`, `CircularProgressIndicator` | `androidx.glance.appwidget` |
| `FilledButton`, `CircleIconButton`, `SquareIconButton` | `androidx.glance.appwidget.components` |
| `LazyColumn`, `items`, `VerticalScrollMode` | `androidx.glance.appwidget.lazy` |
| `ActionCallback`, `actionRunCallback` | `androidx.glance.appwidget.action` |
| `updateAppWidgetState` | `androidx.glance.appwidget.state` |
| `PreferencesGlanceStateDefinition` | `androidx.glance.state` |
| `currentState`, `LocalSize`, `GlanceTheme` | `androidx.glance` |
| `actionStartActivity`, `actionParametersOf`, `clickable` | `androidx.glance.action` |
