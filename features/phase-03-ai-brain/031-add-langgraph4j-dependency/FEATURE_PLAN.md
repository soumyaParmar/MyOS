# Feature Plan: Add LangGraph4j Dependency

## Source Checkbox

- Phase: Phase 3 — AI Brain & Agent Graph
- Checkbox: `- [ ] Add LangGraph4j dependency (integrates with Spring AI)`
- Current status: `[ ]`

## Goal

Introduce the **LangGraph4j** library into the MyOS project to establish the core foundation for a stateful, multi-agent orchestration graph. This library enables the backend to coordinate complex conversational flows, conditional agent routing, memory management, and human-in-the-loop validation, all while integrating smoothly with existing Spring AI components (such as our database-backed chat models).

## Scope

- **POM Configuration**: Add LangGraph4j BOM (`langgraph4j-bom` version `1.8.17`) to `<dependencyManagement>` and `langgraph4j-core` to `<dependencies>` in `backend/pom.xml`.
- **Integration Validation**: Create a localized test-bed package (`com.myos.agent.testbed`) or test class to verify that the LangGraph4j dependency is successfully integrated, compile-safe, and can orchestrate state transitions.
- **Spring AI Integration Verification**: Implement a simple, end-to-end integration test demonstrating a basic LangGraph4j `StateGraph` that takes a prompt, passes it through a node invoking our primary Spring AI `ChatModel`, updates the `AgentState`, and completes execution.

## Out Of Scope

- Defining the actual domain-specific nodes like `JobNode`, `SocialNode`, etc. (handled in subsequent checkboxes).
- Implementing conversational Redis cache memory, PostgreSQL PGVector RAG, and WebSockets (handled in separate checkboxes).

## Backend Plan

1. **Dependency Addition**: Add the following coordinates to `backend/pom.xml`:
   - Properties: `<langgraph4j.version>1.8.17</langgraph4j.version>`
   - Dependency Management:
     ```xml
     <dependency>
         <groupId>org.bsc.langgraph4j</groupId>
         <artifactId>langgraph4j-bom</artifactId>
         <version>${langgraph4j.version}</version>
         <type>pom</type>
         <scope>import</scope>
     </dependency>
     ```
   - Main Dependencies:
     ```xml
     <dependency>
         <groupId>org.bsc.langgraph4j</groupId>
         <artifactId>langgraph4j-core</artifactId>
     </dependency>
     ```

2. **Integration Verification Class**:
   - Create a test class `LangGraph4jIntegrationTest.java` in `com.myos.agent` (under `src/test/java`).
   - Define a simple state class extending `AgentState`: `TestState`.
   - Setup a `StateGraph<TestState>` that defines:
     - A simple node invoking the dynamic `ChatModel` (mocked or injected) to process input.
     - A basic router or edge to the `END` node.
   - Run the graph and assert that the state is updated and returned correctly.

## Frontend Plan

- None (this is a backend orchestration dependency integration).

## Data And Migrations

- None.

## API Contract

- None (internal dependency wiring and orchestration setup).

## Security And Privacy

- Ensure LangGraph4j does not log raw state variables containing secrets or tokens in standard production output without masking.

## Testing And Verification

- **Maven Dependency Resolve**: Run `./mvnw clean test-compile` to ensure the project compiles with no dependency resolution failures.
- **Integration Test Execution**: Run `LangGraph4jIntegrationTest` to verify `StateGraph` compilation, execution, and interaction with Spring AI's `ChatModel`.

## Acceptance Criteria

- [ ] LangGraph4j dependency resolves successfully in `backend/pom.xml`.
- [ ] `./mvnw clean test-compile` passes without error.
- [ ] `LangGraph4jIntegrationTest` runs and completes successfully, proving that a StateGraph can be instantiated, compiled, and executed, and can call the Spring AI `ChatModel`.

## Open Questions

- None at this stage.
