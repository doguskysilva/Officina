# Módulo 2.2 — extraPane na prática: NewTask como terceiro painel

## Objetivo

Aplicar `ListDetailSceneStrategy.extraPane()` à tela `NewTask` para observar o comportamento em cada classe de janela.

---

## O que muda no código

```kotlin
// Antes
entry<NewTask> { route ->
    NewTaskScreen(...)
}

// Depois
entry<NewTask>(
    metadata = ListDetailSceneStrategy.extraPane()
) { route ->
    NewTaskScreen(...)
}
```

Uma linha de metadata — a biblioteca faz o resto.

---

## Comportamento por dispositivo

| Device | Back stack | O que aparece |
|---|---|---|
| Phone | `[..., NewTask]` | `NewTask` em tela cheia (single pane) |
| Tablet médio (medium) | `[..., NewTask]` | `NewTask` empilhado sobre o detalhe |
| Tablet largo (expanded landscape) | `[ProjectList, ProjectDetail, NewTask]` | Três painéis lado a lado |

```
Tablet largo, back stack [ProjectList, ProjectDetail(1), NewTask(1)]:
┌──────────────┬──────────────────┬──────────────────┐
│  Lista de     │  App Mobile       │  Nova tarefa      │
│  projetos     │  (detalhe)        │  (extra pane)     │
└──────────────┴──────────────────┴──────────────────┘
```

No phone e tablet médio o `extraPane` se comporta como um entry normal: entra em cima da tela anterior. O terceiro painel só aparece quando a janela é larga o suficiente para a biblioteca decidir exibir três colunas.

---

## Por que NewTask é o candidato ideal

- **NewTask** é filho direto de **ProjectDetail**: faz sentido semântico ver o detalhe do projeto enquanto cria uma tarefa.
- O `extraPane` é sempre o "terceiro nível" na hierarquia lista → detalhe → extra.
- `ConfirmDelete` **não** seria adequado: é um diálogo (`DialogSceneStrategy.dialog()`), OverlayScene — nunca entra no layout de três painéis.

---

## Relação com os módulos anteriores

| Módulo | Metadata | Comportamento |
|---|---|---|
| 2.1 | `listPane(detailPlaceholder)` | Lista + placeholder / lista + detalhe |
| 2.1 | `detailPane()` | Detalhe (segundo painel) |
| **2.2** | **`extraPane()`** | **Extra (terceiro painel em janelas largas)** |

A biblioteca compõe os três automaticamente quando o back stack contém um entry de cada tipo.

---

## Arquivos modificados

```
MainActivity.kt   ← + metadata = ListDetailSceneStrategy.extraPane() no entry<NewTask>
docs/lesson2.1.md ← atualiza referência ao extraPane
```
