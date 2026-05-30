# Feature Plan: Define Agent Graph Nodes

## Source Checkbox

- **Phase**: Phase 3 — AI Brain & Agent Graph
- **Checkbox**: `- [ ] Define agent graph nodes: `JobNode`, `SocialNode`, `EmailNode`, `FinanceNode`, `LearningNode`, `HabitNode``
- **Current status**: `[ ]`

---

## Goal

Define a clean, modular, and extensible architecture for the six domain-specific AI Agent Nodes in MyOS using LangGraph4j and Spring AI. By mapping each domain agent to a dedicated Spring bean implementing a common `AgentNode` interface, we establish the foundation for our multi-agent routing graph while enabling individual domain specialists to utilize our dynamic database-backed LLM configurations.

---

## Scope

1. **Shared State Representation**: Introduce `MyOsAgentState` (extending LangGraph4j's `AgentState`) to carry standardized keys such as `input`, `response`, and `currentNode` across all nodes.
2. **Unified Node Interface**: Create the `AgentNode` interface extending LangGraph4j's `NodeAction<MyOsAgentState>` to guarantee type safety and facilitate Spring's autowiring of all nodes.
3. **Domain Node Implementations**: Implement the six core nodes:
   - `JobNode` (Career & Job Applications)
   - `SocialNode` (Social Media Management)
   - `EmailNode` (Emails & Calendars)
   - `FinanceNode` (Budget & Expenses)
   - `LearningNode` (Study Goals & Roadmap recommendations)
   - `HabitNode` (Habits & notion tracking)
4. **Dynamic LLM Integration**: Ensure every node injects the primary `ChatModel` (delegated transparently by `DynamicChatModelFactory`) to perform stateful AI reasoning.
5. **Specialized Prompts**: Equip each node with a dedicated system prompt template tailored to its functional responsibilities.
6. **Educational Testing**: Create an comprehensive unit test suite to verify node behavior, state modifications, and mock-based LLM interactions.

---

## Out of Scope

- Setting up the full graph execution pipeline, edges, and conditional routing logic (handled in the next checkbox).
- Creating real database tables and external API integrations for each specific domain (handled in Phases 5-10).

---

## Proposed Changes

We will group all agent brain orchestrations in the `com.myos.service.agent` package, ensuring business logic remains centered within our standard Spring Boot `service` tier.

```
backend/src/main/java/com/myos/service/agent/
│
├── MyOsAgentState.java                 (State container for our multi-agent flow)
│
└── node/
    ├── AgentNode.java                  (Base interface extending LangGraph4j's NodeAction)
    ├── JobNode.java                    (Career Assistant node)
    ├── SocialNode.java                 (Social Media Scheduler node)
    ├── EmailNode.java                  (Email & Calendar assistant node)
    ├── FinanceNode.java                (Budget Planner node)
    ├── LearningNode.java               (Roadmap recommending node)
    └── HabitNode.java                  (Habit Logger node)
```

---

### Component Specifications

#### 1. `MyOsAgentState`
Extends LangGraph4j's `AgentState` class. It manages state parameters in a type-safe manner.
- **Key Channels**:
  - `input`: User request query.
  - `response`: LLM-generated feedback/summary.
  - `currentNode`: Stores the active agent's identifier (enabling our UI visualizer).

#### 2. `AgentNode` Interface
A simple marker interface extending `NodeAction<MyOsAgentState>`. It requires a `getName()` method to uniquely identify each node in our StateGraph.

#### 3. Domain Nodes (`JobNode`, `SocialNode`, etc.)
Spring `@Component`s that implement `AgentNode`.
- **System Prompts**:
  - `JobNode`: "You are the MyOS Career and Job Search Agent..."
  - `SocialNode`: "You are the MyOS Social Media Manager..."
  - `EmailNode`: "You are the MyOS Email and Calendar Assistant..."
  - `FinanceNode`: "You are the MyOS Personal Finance Agent..."
  - `LearningNode`: "You are the MyOS Learning and Tutor Agent..."
  - `HabitNode`: "You are the MyOS Health and Habit Tracker..."

---

## Data And Migrations

None (in-memory state management).

---

## API Contract

None (internal class structure).

---

## Security And Privacy

- **Input Sanitization**: Node inputs are extracted and formatted using Spring AI's `PromptTemplate` to prevent direct prompt injection techniques.
- **Credential Safety**: All model invocations depend on the `DynamicChatModelFactory`, which decryts credentials securely inside native services and keeps them out of local logs.

---

## Testing And Verification

### Automated Verification
We will add a dedicated unit test suite `AgentNodesTest` under `backend/src/test/java/com/myos/service/agent/` to verify:
- Individual nodes correctly process state inputs.
- Specialized system prompt generation and integration.
- Correct updates to `response` and `currentNode` in the resulting state.

Run the test suite using:
```bash
./mvnw.cmd test -Dtest=AgentNodesTest
```

---

## Acceptance Criteria

- [ ] `MyOsAgentState` and `AgentNode` compile correctly under the `com.myos.service.agent` package hierarchy.
- [ ] All six domain nodes (`JobNode`, `SocialNode`, `EmailNode`, `FinanceNode`, `LearningNode`, `HabitNode`) are declared as Spring `@Component`s.
- [ ] Each node correctly implements `AgentNode`, injects the `ChatModel` bean, formats prompts, and mutates the graph state accurately.
- [ ] Unit tests are provided for each node, verifying complete state mutation correctness under mock conditions.
- [ ] `./mvnw.cmd clean test-compile` finishes successfully with zero warnings.

---

## Open Questions

None. The design leverages standard Spring IOC patterns and is completely standalone at this stage.
