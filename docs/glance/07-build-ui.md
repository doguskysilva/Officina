# Glance — Build UI (Layouts, SizeMode, Components)

> **ATENÇÃO:** Glance usa composables PRÓPRIOS (`Column`, `Row`, `Box`, `Text`, `Button`, `LazyColumn`). NÃO são os do Compose UI. Importar errado causa erros silenciosos.

---

## Layouts base

| Composable | Equivalente RemoteViews | Descrição |
|---|---|---|
| `Box` | `RelativeLayout` | Empilha elementos |
| `Column` | `LinearLayout` (vertical) | Elementos em coluna |
| `Row` | `LinearLayout` (horizontal) | Elementos em linha |
| `Scaffold` | — | Container com titleBar e background |

```kotlin
// Distribuir filhos igualmente com weight
Row(modifier = GlanceModifier.fillMaxWidth().padding(16.dp)) {
    val modifier = GlanceModifier.defaultWeight()
    Text("first", modifier)
    Text("second", modifier)
    Text("third", modifier)
}
```

## LazyColumn (scrollável)

```kotlin
// import androidx.glance.appwidget.layout.LazyColumn

LazyColumn {
    item { Text("Header") }

    items(peopleNameList) { name -> Text(name) }

    itemsIndexed(peopleNameList) { index, person ->
        Text("$person at index $index")
    }

    // Com key para performance e manutenção de scroll (Android 12+)
    items(items = peopleList, key = { person -> person.id }) { person ->
        Text(person.name)
    }
}
```

> `LazyColumn` é traduzido para `ListView` com `RemoteViews`. As mesmas limitações de `RemoteViews` collections se aplicam.

---

## SizeMode

### `SizeMode.Single` (padrão)

Um único layout independente do tamanho. Use quando o widget tem tamanho fixo ou não muda conteúdo ao redimensionar.

```kotlin
override val sizeMode = SizeMode.Single
```

### `SizeMode.Responsive` ✅ (recomendado)

Define breakpoints — Glance compõe um layout para cada tamanho e o sistema escolhe o melhor fitting. Melhor performance e transições mais suaves.

```kotlin
companion object {
    private val SMALL_SQUARE       = DpSize(100.dp, 100.dp)
    private val HORIZONTAL_RECT    = DpSize(250.dp, 100.dp)
    private val BIG_SQUARE         = DpSize(250.dp, 250.dp)
}

override val sizeMode = SizeMode.Responsive(
    setOf(SMALL_SQUARE, HORIZONTAL_RECT, BIG_SQUARE)
)

@Composable
private fun MyContent() {
    val size = LocalSize.current
    Column {
        if (size.height >= BIG_SQUARE.height) {
            Text("Where to?", modifier = GlanceModifier.padding(12.dp))
        }
        Row(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(); Button()
            if (size.width >= HORIZONTAL_RECT.width) Button("School")
        }
        if (size.height >= BIG_SQUARE.height) {
            Text("provided by X")
        }
    }
}
```

### `SizeMode.Exact`

Reconstrói o widget inteiro a cada mudança de tamanho. Use apenas se `Responsive` não for viável — pode causar jumps e performance issues.

```kotlin
override val sizeMode = SizeMode.Exact

@Composable
private fun MyContent() {
    val size = LocalSize.current
    if (size.width > 250.dp) Button("School")
}
```

### Comparativo de tamanhos

| Available size | 105×110 | 203×112 | 72×72 | 203×150 |
|---|---|---|---|---|
| `Single` | 110×110 | 110×110 | 110×110 | 110×110 |
| `Exact` | 105×110 | 203×112 | 72×72 | 203×150 |
| `Responsive` | 80×100 | 80×100 | 80×100 | 150×120 |

---

## Resources

```kotlin
// String
LocalContext.current.getString(R.string.glance_title)

// Background por resource ID (reduz tamanho do RemoteViews)
Column(modifier = GlanceModifier.background(R.color.default_widget_background)) { }

// Image
Image(provider = ImageProvider(R.drawable.ic_logo), contentDescription = "logo")
```

---

## Text styles

```kotlin
Text(
    style = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        fontFamily = FontFamily.Monospace,
    ),
    text = "Example Text"
)
```

> `fontFamily` suporta system fonts apenas — fontes customizadas não são suportadas.

---

## Compound buttons (Android 12+ com backward compat)

```kotlin
CheckBox(
    checked = isChecked,
    onCheckedChange = { isChecked = !isChecked },
    text = "Apples",
    colors = CheckboxDefaults.colors(
        checkedColor = ColorProvider(day = colorAccentDay, night = colorAccentNight),
    ),
)

Switch(
    checked = isEnabled,
    onCheckedChange = { isEnabled = !isEnabled },
    text = "Enabled",
    colors = SwitchDefaults.colors(
        checkedThumbColor = ColorProvider(day = Color.Red, night = Color.Cyan),
        checkedTrackColor = ColorProvider(day = Color.Blue, night = Color.Yellow),
    ),
)

RadioButton(
    checked = isSelected,
    onClick = { isSelected = true },
    text = "Option A",
)
```

---

## Componentes adicionais (Glance 1.1.0+)

| Componente | Notas |
|---|---|
| `FilledButton` | Botão preenchido |
| `OutlineButton` | Botão com borda |
| `IconButton` | Primary / Secondary / Icon-only |
| `TitleBar` | Barra de título para widgets |
| `Scaffold` | Container com TitleBar |
