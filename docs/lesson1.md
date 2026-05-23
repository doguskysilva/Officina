# Módulo 1 — O Modelo Mental do Navigation3

## Por que Navigation3 é diferente do Navigation2?

**Nav2 (antigo):** você chama `navController.navigate("rota")` e a biblioteca gerencia o back stack por baixo. Caixa preta. Difícil de exibir dois destinos ao mesmo tempo (layouts adaptativos).

**Nav3 (novo):** o back stack é **sua lista**. Você adiciona e remove itens, e o `NavDisplay` observa e renderiza. Transparente. Por isso Scenes funcionam — uma `SceneStrategy` pode pegar 2 entries da lista e mostrar lado a lado.

```
sua lista  →  NavDisplay  →  SceneStrategy  →  Scene  →  tela
```

---

## Os 4 Conceitos Fundamentais

| Conceito | O que é |
|---|---|
| `NavKey` | Identifica uma rota (`data object` / `data class` com `@Serializable`) |
| `NavEntry` | O composable da tela ligado a uma chave |
| `NavDisplay` | Observa a lista e renderiza via Scene |
| Back stack | A lista — `rememberNavBackStack()` persiste até process death |

---

## NavKey — definindo rotas

```kotlin
// Rota sem argumentos → data object
@Serializable
data object ProjectList : NavKey

// Rota com argumentos → data class (type-safe, sem strings)
@Serializable
data class ProjectDetail(
    val projectId: Int,
    val projectName: String
) : NavKey
```

`@Serializable` é obrigatório quando se usa `rememberNavBackStack`. Sem ele o app compila mas crasha na primeira rotação de tela com `SerializationException`.

---

## rememberNavBackStack vs mutableStateListOf

```kotlin
// Opção A — recomendada para apps reais:
val backStack = rememberNavBackStack(ProjectList)

// Opção B — simples, para demos:
val backStack = remember { mutableStateListOf<NavKey>(ProjectList) }
```

| Situação | `rememberNavBackStack` | `mutableStateListOf` |
|---|---|---|
| Recomposição normal | Sobrevive | Sobrevive |
| Rotação de tela | Sobrevive | Perde — volta ao início |
| Process death (SO mata o app) | Sobrevive | Perde — volta ao início |
| Precisa de `@Serializable` | Sim | Não |

**Regra:** use `rememberNavBackStack` desde o início em qualquer app real.

---

## NavDisplay e entryProvider DSL

```kotlin
NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider = entryProvider<NavKey> {

        // Rota sem argumentos
        entry<ProjectList> {
            ProjectListScreen(
                onProjectClick = { route -> backStack.add(route) }
            )
        }

        // Rota com argumentos — `route` já é o tipo correto, sem casting
        entry<ProjectDetail> { route ->
            ProjectDetailScreen(
                projectId = route.projectId,
                projectName = route.projectName,
                onBack = { backStack.removeLastOrNull() }
            )
        }
    }
)
```

`NavDisplay` observa o back stack com `snapshotFlow`. Qualquer mudança na lista recompõe a UI automaticamente.

---

## Navegação — é só uma lista

```kotlin
// Navegar para frente
backStack.add(ProjectDetail(projectId = 1, projectName = "App Mobile"))

// Voltar
backStack.removeLastOrNull()

// Estado do back stack ao navegar e voltar:
// início:           [ProjectList]
// clica projeto:    [ProjectList, ProjectDetail(1, "App Mobile")]
// pressiona voltar: [ProjectList]
```

Nada de `navController.navigate()`, nada de `navController.popBackStack()`.

---

## dropUnlessResumed — proteção contra duplo-clique

```kotlin
.clickable(onClick = dropUnlessResumed {
    backStack.add(route)
})
```

Protege contra duplo-clique durante a animação de transição. Sem isso o usuário pode adicionar o mesmo destino duas vezes no back stack antes da animação terminar. **Sempre use em clicks de navegação.**

---

## Arquivos criados neste módulo

```
app/src/main/java/com/doguskytech/officina/
├── MainActivity.kt                  ← NavDisplay + entryProvider
├── navigation/
│   └── Routes.kt                    ← NavKeys (@Serializable)
└── screens/
    ├── ProjectListScreen.kt
    ├── ProjectDetailScreen.kt
    └── NewTaskScreen.kt
```

---

## Exercícios e Respostas

**1. Quando você clica em um projeto e volta, o back stack tem quantos itens?**

1 item — `ProjectList`. O `removeLastOrNull()` remove o `ProjectDetail` e sobra a rota inicial.

**2. O que acontece se você remover `@Serializable` de um NavKey e rotacionar a tela?**

O app compila normalmente mas crasha em runtime com `SerializationException`. O `rememberNavBackStack` precisa serializar o back stack para o `Bundle` do Android, e sem `@Serializable` essa serialização falha.

**3. O que mudaria usando `mutableStateListOf` no lugar de `rememberNavBackStack`?**

A navegação funcionaria normalmente durante o uso, mas ao rotacionar o device ou o SO matar o processo, o back stack seria perdido e o usuário voltaria para a tela inicial. Também não seria necessário `@Serializable` nos NavKeys.
