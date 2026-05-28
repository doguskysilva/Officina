# Lesson 16 — Centralização de UI e i18n com String Resources

## O que foi feito

Duas melhorias de qualidade que fecham o projeto de forma sólida:

1. **Eliminação de duplicação de código UI** — funções e constantes compartilhadas extraídas para `ui/`
2. **Internacionalização (i18n)** — todas as strings movidas para resources XML com suporte a plurais e dois idiomas

---

## Parte 1 — Centralização de UI

### O problema

`ProjectDetailScreen` e `TaskListScreen` tinham cópias idênticas de:

```kotlin
// duplicado em dois arquivos
private val enterTransition = fadeIn() + scaleIn(initialScale = 0.85f)
private val exitTransition  = fadeOut() + scaleOut(targetScale = 0.85f)

@Composable
private fun priorityColor(priority: Priority): Color = when (priority) { ... }
```

Ao mudar qualquer um desses, seria necessário lembrar de atualizar os dois lugares.

### A solução

**`ui/Transitions.kt`** — constantes de animação de conteúdo (nível de item, não de navegação):

```kotlin
val itemEnterTransition: EnterTransition = fadeIn() + scaleIn(initialScale = 0.85f)
val itemExitTransition: ExitTransition   = fadeOut() + scaleOut(targetScale = 0.85f)
```

**`ui/PriorityUi.kt`** — mapeamento de prioridade para cor do tema:

```kotlin
@Composable
fun priorityColor(priority: Priority): Color = when (priority) {
    Priority.LOW    -> MaterialTheme.colorScheme.tertiary
    Priority.MEDIUM -> MaterialTheme.colorScheme.primary
    Priority.HIGH   -> MaterialTheme.colorScheme.error
}
```

Nos screens, troca `private val`/`private fun` por imports:

```kotlin
import com.doguskytech.officina.ui.itemEnterTransition
import com.doguskytech.officina.ui.itemExitTransition
import com.doguskytech.officina.ui.priorityColor
```

> **Critério para extrair**: vale a pena quando a lógica é idêntica em dois ou mais lugares E tem chance real de mudar (ex: mudar a curva de animação, mudar as cores de prioridade). Lógica de UI que só existe numa tela pode ficar `private`.

---

## Parte 2 — i18n com String Resources

### Por que não usar strings hardcoded no código

```kotlin
// ❌ hardcoded — preso ao PT-BR, não testável isoladamente
Text("${selectedTaskIds.size} selecionada${if (selectedTaskIds.size != 1) "s" else ""}")

// ✓ string resource — traduzível, com plural correto por idioma
Text(pluralStringResource(R.plurals.selected_tasks_count, count, count))
```

Problemas com strings hardcoded:
- Bloqueiam o app num idioma
- Lógica de plural misturada com lógica de UI (o `if (size != 1)` acima)
- Regras de plural variam por idioma — em inglês é diferente do PT-BR

### Estrutura de arquivos

```
res/
├── values/          ← padrão, PT-BR (idioma principal do app)
│   └── strings.xml
└── values-en/       ← override para inglês
    └── strings.xml
```

O Android seleciona automaticamente com base no idioma do dispositivo. Se o idioma não tiver override (ex: espanhol), cai no padrão PT-BR.

### `stringResource` — strings simples

```kotlin
import androidx.compose.ui.res.stringResource

// Sem parâmetros
Text(stringResource(R.string.action_cancel))

// Com parâmetro de formato (%s)
Text(stringResource(R.string.delete_project_body, projectName))
// strings.xml: <string name="delete_project_body">"%s" será removido permanentemente.</string>
// values-en:   <string name="delete_project_body">"%s" will be permanently removed.</string>
```

### `pluralStringResource` — strings com plural

O plural não é só adicionar "s". Cada idioma tem suas próprias regras. O Android lida com isso via `<plurals>`:

```xml
<!-- values/strings.xml (PT-BR) -->
<plurals name="tasks_count">
    <item quantity="one">%d tarefa</item>
    <item quantity="other">%d tarefas</item>
</plurals>

<!-- values-en/strings.xml (EN) -->
<plurals name="tasks_count">
    <item quantity="one">%d task</item>
    <item quantity="other">%d tasks</item>
</plurals>
```

No Compose:

```kotlin
import androidx.compose.ui.res.pluralStringResource

// count é passado duas vezes: uma para escolher a forma (one/other), outra para o %d
Text(pluralStringResource(R.plurals.tasks_count, project.tasks.size, project.tasks.size))
```

Antes era necessário escrever:
```kotlin
// ❌ lógica de plural embutida no código
"${project.tasks.size} ${if (project.tasks.size != 1) "tarefas" else "tarefa"}"
```

Agora a lógica de "quantas formas existem e quando usar cada uma" fica no XML, onde é explícita, testável e traduzível.

### `@StringRes` em enums

O enum `Priority` tinha as labels hardcoded:

```kotlin
// ❌ antes
enum class Priority(val label: String) {
    LOW("Baixa"), MEDIUM("Média"), HIGH("Alta")
}
// uso: Text(priority.label)
```

Com i18n:

```kotlin
// ✓ depois
enum class Priority(@StringRes val labelRes: Int) {
    LOW(R.string.priority_low),
    MEDIUM(R.string.priority_medium),
    HIGH(R.string.priority_high),
}
// uso: Text(stringResource(priority.labelRes))
```

O mesmo padrão foi aplicado ao `StatusFilter` (enum privado de `TaskListScreen`). A anotação `@StringRes` documenta a intenção e permite que ferramentas como o Lint verifiquem que o Int é realmente um ID de string.

### Plurais identificados neste projeto

| Uso | Resource |
|---|---|
| "X projeto(s)" na lista | `R.plurals.projects_count` |
| "X tarefa(s)" no subtítulo | `R.plurals.tasks_count` |
| "X selecionada(s)" no subtítulo | `R.plurals.selected_tasks_count` |
| Confirmação "concluir todas" | `R.plurals.pending_tasks_confirm_body` |

### Resultado no código

O `ProjectDetailScreen` substituiu:

```kotlin
// ❌ antes — lógica de plural + string hardcoded misturados
"$pending ${if (pending == 1) "tarefa pendente será marcada"
            else "tarefas pendentes serão marcadas"} como concluída${if (pending != 1) "s" else ""}."
```

Por:

```kotlin
// ✓ depois — intenção clara, plural correto por idioma
pluralStringResource(R.plurals.pending_tasks_confirm_body, pending, pending)
```

---

## Arquivos modificados

| Arquivo | O que mudou |
|---|---|
| `ui/Transitions.kt` | **novo** — `itemEnterTransition` / `itemExitTransition` |
| `ui/PriorityUi.kt` | **novo** — `priorityColor()` compartilhada |
| `data/Priority.kt` | `label: String` → `@StringRes labelRes: Int` |
| `res/values/strings.xml` | PT-BR — todas as strings + plurais |
| `res/values-en/strings.xml` | **novo** — override em inglês |
| `screens/ProjectDetailScreen.kt` | `stringResource` + `pluralStringResource` + imports shared |
| `screens/TaskListScreen.kt` | idem + `StatusFilter` com `@StringRes` |
| `screens/NewTaskScreen.kt` | `stringResource` em labels e botões |
| `screens/ProjectListScreen.kt` | `pluralStringResource` para contagem de projetos/tarefas |
| `screens/ConfirmDeleteDialog.kt` | `stringResource` com parâmetro de formato |
| `screens/SettingsScreen.kt` | `stringResource` |
| `screens/SortProjectsSheet.kt` | `stringResource` para opções de ordenação |
| `MainActivity.kt` | `stringResource` para labels de navegação |
