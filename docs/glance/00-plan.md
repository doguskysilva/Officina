# Módulo 12 — Glance Widgets (Plano)

Três widgets em ordem crescente de complexidade, cada um introduzindo novos conceitos do Glance.

---

## Widget 1 — Resumo Geral

**Tamanho:** pequeno / médio  
**Conceitos:** `SizeMode.Responsive`, `GlanceTheme`, layout básico, `actionStartActivity`

Exibe:
- Contagem de projetos ativos
- Contagem de tarefas pendentes
- Botão para abrir o app

---

## Widget 2 — Lista de Tarefas Pendentes

**Tamanho:** médio / grande  
**Conceitos:** `LazyColumn`, `ActionCallback`, `actionStartActivity` com `ActionParameters`, integração com repositório

Exibe:
- Lista scrollável de tarefas não concluídas de todos os projetos
- Toque na tarefa → abre `ProjectDetailScreen` com highlight na tarefa

---

## Widget 3 — Projeto Específico (configurável)

**Tamanho:** médio / grande  
**Conceitos:** `Configuration Activity`, persistência com `DataStore`, `ActionCallback` para toggle de tarefa, `GlanceAppWidgetManager`

Exibe:
- Tarefas de um projeto escolhido pelo usuário na configuração
- Permite marcar tarefa como concluída diretamente do widget

---

## Referências

Documentação de referência em `docs/glance/`:

| Arquivo | Conteúdo |
|---|---|
| `01-manifest-metadata.md` | Receiver, AppWidgetProviderInfo, GlanceAppWidget base |
| `02-widget-enhancements.md` | Label e description |
| `03-configuration.md` | Configuration Activity, widgetFeatures |
| `04-generated-previews.md` | providePreview, setWidgetPreviews, backward compat |
| `05-user-interaction.md` | Actions: Activity, Service, Broadcast, Lambda, ActionCallback |
| `06-state-update.md` | State management, update, updateAll, WorkManager |
| `07-build-ui.md` | Layouts, SizeMode, LazyColumn, componentes |
| `08-theming.md` | GlanceTheme, ColorProviders, dynamic colors, shapes |
