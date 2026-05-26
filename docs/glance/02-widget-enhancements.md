# Glance — Widget Enhancements

## Nome do widget (label)

O nome exibido no widget picker vem do atributo `label` do `<receiver>` no `AndroidManifest.xml`:

```xml
<receiver
    android:name=".glance.MyAppWidgetReceiver"
    android:label="Memories"
    android:exported="true">
    ...
</receiver>
```

## Descrição do widget (Android 12+)

```xml
<appwidget-provider
    android:description="@string/my_widget_description">
</appwidget-provider>
```

- Sem limite de caracteres, mas seja conciso — o espaço disponível varia por dispositivo.
- O atributo `descriptionRes` existe em versões anteriores mas é ignorado pelo widget picker.
