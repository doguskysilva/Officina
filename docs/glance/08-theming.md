# Glance — Theming (Colors & Shapes)

## GlanceTheme — cores Material out of the box

```kotlin
override suspend fun provideGlance(context: Context, id: GlanceId) {
    provideContent {
        GlanceTheme {
            MyContent()
        }
    }
}

@Composable
private fun MyContent() {
    Image(
        colorFilter = ColorFilter.tint(GlanceTheme.colors.secondary),
        // ...
    )
}
```

- **Android 12+**: deriva do wallpaper do usuário (dynamic colors)
- **Android 11-**: fallback para o baseline Material

---

## Customizar com cores do app (Material 3)

Dependência: `androidx.glance:glance-material3` (já no projeto)

```kotlin
// import androidx.glance.material3.ColorProviders
// import com.example.myapp.ui.theme.DarkColors
// import com.example.myapp.ui.theme.LightColors

object MyAppWidgetGlanceColorScheme {
    val colors = ColorProviders(
        light = LightColors,
        dark = DarkColors,
    )
}
```

```kotlin
provideContent {
    GlanceTheme(colors = MyAppWidgetGlanceColorScheme.colors) {
        MyContent()
    }
}
```

---

## Dynamic colors quando disponível, fallback para cores do app

```kotlin
provideContent {
    GlanceTheme(
        colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            GlanceTheme.colors                      // dynamic (Android 12+)
        else
            MyAppWidgetGlanceColorScheme.colors     // app colors
    ) {
        MyContent()
    }
}
```

---

## Shapes via Android Drawables

Glance não tem API nativa de shapes — usar drawables XML:

```xml
<!-- res/drawable/widget_button_bg.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <corners android:radius="16dp"/>
    <stroke android:color="@color/outline_color" android:width="1dp"/>
</shape>
```

```kotlin
GlanceModifier.background(
    imageProvider = ImageProvider(R.drawable.widget_button_bg)
)
```

> Use a estrutura de pastas de recursos para variantes (`values-night`, etc.).

---

## TextStyle — variável top-level

```kotlin
// Declarar fora do composable (top-level)
val widgetTitleStyle = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
)

@Composable
fun MyContent() {
    Text(text = "Titulo", style = widgetTitleStyle)
}
```
