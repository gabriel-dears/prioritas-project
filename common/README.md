# 📦 Prioritas Common - Shared Kernel

Este módulo é uma biblioteca compartilhada utilizada pelos microsserviços do ecossistema Prioritas. O seu objetivo principal é definir os contratos de dados (DTOs e Eventos) para garantir que o Produtor (Backend) e o Consumidor (Notification) "falem a mesma língua" sem duplicação de código ou inconsistências de tipagem.

## 🎯 Propósito

- Single Source of Truth: Centraliza a definição dos eventos de domínio.
- Type Safety: Garante que ambos os serviços utilizem classes Kotlin fortemente tipadas.]
- Desacoplamento de Lógica: Contém apenas dados (POJOs/Data Classes), sem regras de negócio ou dependências de banco de dados

## 💻 Stack Tecnológica e Versões

As definições de versão são gerenciadas via **Gradle Kotlin DSL**. O projeto segue as diretrizes de compatibilidade do ecossistema Spring Boot 3.

| Componente | Versão | Detalhes / Configuração |
| :--- | :--- | :--- |
| **Linguagem** | `Kotlin 1.9.24` | Definido via plugin `kotlin("jvm")` |
| **JDK (Runtime)** | `Java 17` (LTS) | Toolchain configurado via `jvmToolchain(17)` |
| **Framework** | `Spring Boot 3.4.2` | (Versão inferida dos módulos de aplicação) |
| **Serialização** | `Jackson 2.15.0` | Módulo `jackson-annotations` no core |
| **Build System** | `Gradle 8.x` | Gerenciado via Wrapper (`gradlew`) |

### 🏷️ Metadados do Artefato

* **Group ID:** `br.com.fiap.adj8.phase5.prioritas`
* **Artifact Version:** `0.0.1-SNAPSHOT` (Versão de Desenvolvimento)

## .🏗️ Estrutura de Dados (O Evento)

O principal artefato deste módulo é o TriageNotificationEvent, que é disparado sempre que uma triagem é finalizada.

### 1. TriageNotificationEvent (Raiz)

Representa a mensagem completa enviada para o Message Broker (RabbitMQ).

```kotlin
data class TriageNotificationEvent(
    val triageId: UUID,            // ID único da triagem
    val assessedAt: LocalDateTime, // Data/Hora da avaliação
    val riskLevel: String,         // Ex: "EMERGENCY", "URGENT"
    val riskColor: String,         // Ex: "RED", "YELLOW"
    val patientId: UUID,           // Referência ao paciente (Link para busca futura)
    val vitalSigns: VitalSignsPayload // Snapshot dos sinais vitais
)
```

### 2. VitalSignsPayload (DTO)

| Campo | Tipo de Dado | Obrigatório? | Descrição | Unidade de Medida |
| :--- | :--- | :---: | :--- | :--- |
| `hasChestPain` | `Boolean` | ✅ Sim | Indicador crítico. Se `true`, geralmente eleva o risco para Emergência. | N/A (Binário) |
| `temperature` | `Double` | ❌ Não | Temperatura corporal aferida. | Celsius (°C) |
| `heartRate` | `Integer` | ❌ Não | Frequência cardíaca. | Batimentos por min (bpm) |
| `oxygenSaturation` | `Integer` | ❌ Não | Nível de oxigenação no sangue. | Porcentagem (%) |
| `systolicPressure` | `Integer` | ❌ Não | Pressão arterial sistólica (valor maior). | mmHg |
| `diastolicPressure` | `Integer` | ❌ Não | Pressão arterial diastólica (valor menor). | mmHg |

### 🛠️ Como Integrar

Este módulo não é executável. Ele deve ser importado como uma dependência Gradle nos microsserviços.

No build.gradle.kts (Backend & Notification):

```kotlin
dependencies {
  // Importa o módulo 'common' como dependência do projeto
  implementation(project(":common"))
}
```

## ⚠️ Regras de Evolução (Versionamento)

Como este é um módulo compartilhado, alterações aqui podem quebrar o sistema distribuído (Breaking Changes).
- Adição de Campos: ✅ Seguro. O Jackson (JSON Parser) nos consumidores irá ignorar campos novos se não estiverem mapeados, ou aceitá-los se estiverem.
- Remoção de Campos: ❌ Perigoso. Se o consumidor espera um campo que foi removido, ocorrerá erro de desserialização.
- Renomeação: ❌ Perigoso. Trata-se como remoção + adição. Evite renomear campos em produção sem estratégia de migração.

📄 Exemplo de Payload JSON

Abaixo, um exemplo de como os objetos deste módulo são serializados na fila prioritas.triage.queue:

```json
{
"triageId": "7a37a59f-5009-4c74-ae49-e0b7415db263",
"assessedAt": "2026-02-05T22:27:55",
"riskLevel": "EMERGENCY",
"riskColor": "RED",
"patientId": "fac53c56-011e-4247-a1ab-00843b3cfe2e",
"vitalSigns": {
    "temperature": 36.5,
    "heartRate": 110,
    "oxygenSaturation": 98,
    "hasChestPain": true,
    "systolicPressure": 140,
    "diastolicPressure": 90
    }
}
```

## 🚀 Roadmap de Evolução (V2) - Event Enrichment

Esta seção descreve as melhorias arquiteturais planejadas para a próxima versão do sistema, focando no desacoplamento total entre os microsserviços.

### 🔴 O Cenário Atual (V1)
Atualmente, o evento de notificação trafega apenas o `patientId`.
Isso gera um **Acoplamento Temporal** ou de **Dados**, pois o *Notification Service* precisaria:
1. Realizar uma chamada HTTP síncrona de volta ao Backend para buscar o nome/email do paciente; OU
2. Ter acesso direto de leitura ao banco de dados do Backend (Quebra de fronteira de contexto).

### 🟢 A Solução Proposta (V2)
Implementação do padrão **Event-Carried State Transfer**.
O evento passará a transportar o estado necessário do paciente (Dados Cadastrais Imutáveis) dentro do próprio payload. Isso torna o *Notification Service* 100% autônomo.

#### Comparativo de Payload (JSON)

**De (V1 - Atual):**
```json
{
  "triageId": "...",
  "riskLevel": "EMERGENCY",
  "patientId": "fac53c56-011e-4247-a1ab-00843b3cfe2e", // ⚠️ Apenas ID
  "vitalSigns": {
    "temperature": 36.5,
    "heartRate": 110,
    "oxygenSaturation": 98,
    "hasChestPain": true,
    "systolicPressure": 140,
    "diastolicPressure": 90
  }
}
```

**Para (V2 - Planejado):**

```json
{
"triageId": "...",
"riskLevel": "EMERGENCY",
"patient": {                  // ✅ Objeto Rico (Enriched)
    "id": "fac53c56-...",
    "fullName": "João da Silva",
    "cpf": "123.456.789-00",
    "email": "joao@email.com"
    },
"vitalSigns": {
      "temperature": 36.5,
      "heartRate": 110,
      "oxygenSaturation": 98,
      "hasChestPain": true,
      "systolicPressure": 140,
      "diastolicPressure": 90
    }
}
```
