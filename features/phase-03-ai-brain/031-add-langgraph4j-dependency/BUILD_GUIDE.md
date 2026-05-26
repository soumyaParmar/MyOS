# MyOS Build Guide: LangGraph4j Dependency Integration

Welcome to your next architectural step! In this module, we are bringing in **LangGraph4j**—the Java-native port of the powerful LangGraph framework. 

This guide will teach you how LangGraph4j works, why it is crucial for a stateful multi-agent system like MyOS, and how to integrate it with Spring Boot and Spring AI.

---

## Significance: Why LangGraph4j?

In Phase 2, we built individual AI model configurations. In Phase 3, we are building the **AI Brain**. 
A standard chat system is linear: User -> LLM -> Response. But a fully functional AI Operating System needs to manage **workflows**:
1. An incoming query like *"Schedule a study block for my next job interview"* needs to be parsed.
2. The system must route it to the `JobNode` (to find the interview details) and then to the `LearningNode` (to determine study slots) and finally to the `CalendarNode` (to block the calendar).
3. This is **cyclical and stateful**: nodes pass data back and forth, check conditions, and sometimes ask the user for confirmation (human-in-the-loop).

**LangGraph4j** provides:
- **Stateful execution**: A single `AgentState` flows between all steps.
- **Cyclical flows**: Unlike traditional chains, graphs can route backwards, loop, and branch.
- **Framework compatibility**: It plays perfectly with **Spring AI**, allowing us to run Spring AI `ChatModel` calls inside Graph Nodes.

---

## Deep Dive: Core Concepts

### 1. Maven BOM (Bill of Materials)
When a framework has multiple modules (e.g., `langgraph4j-core`, `langgraph4j-redis-saver`, `langgraph4j-postgres-saver`), managing individual dependency versions in a large project gets messy. 
A **BOM** acts as a centralized catalog of versions. We import the BOM inside Maven's `<dependencyManagement>` section. Then, when we add the individual dependencies under `<dependencies>`, we omit the `<version>` tag! Maven will automatically look up and match the correct version from the BOM. This ensures version consistency across all modules.

### 2. StateGraph & CompiledGraph
- **StateGraph**: This is the builder pattern class. We use it to declare our nodes, edges, state schema, and entry points. It is mutable.
- **CompiledGraph**: Once the state graph is defined, calling `.compile()` validates the structure (making sure there are no orphaned nodes or infinite loops without exit routes) and returns a thread-safe, immutable `CompiledGraph`. You can run this compiled graph multiple times concurrently with different inputs!

### 3. AgentState & Channels
At the core of the graph is the **State**. In Java, `AgentState` is a specialized map-like structure (`Map<String, Object>`).
But how do multiple nodes write to the same key?
- If Node A writes `messages: [A]` and Node B writes `messages: [B]`, does Node B overwrite Node A's messages, or do they append?
- This is controlled by **Channels**.
  - `Channels.set()`: Overwrites the value (useful for single values like `status` or `currentUser`).
  - `Channels.appender()`: Automatically appends updates to a collection (crucial for chat history!).

---

## Step-by-Step Implementation Instructions

### Step 1: Add LangGraph4j Dependencies to `pom.xml`

Open [pom.xml](file:///c:/Users/soumy/Desktop/Learning/MyOS/backend/pom.xml).

1. Define a version property for LangGraph4j under the `<properties>` block:
```xml
<properties>
    ...
    <langgraph4j.version>1.8.17</langgraph4j.version>
</properties>
```

2. Inside `<dependencyManagement> -> <dependencies>`, add the LangGraph4j BOM:
```xml
<dependency>
    <groupId>org.bsc.langgraph4j</groupId>
    <artifactId>langgraph4j-bom</artifactId>
    <version>${langgraph4j.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

3. Inside the main `<dependencies>` block, add the core module:
```xml
<dependency>
    <groupId>org.bsc.langgraph4j</groupId>
    <artifactId>langgraph4j-core</artifactId>
</dependency>
```

---

### Step 2: Create a Basic Verification Test

Let's write a unit test to prove everything compiles and runs. 

Create a test file: `backend/src/test/java/com/myos/agent/LangGraph4jIntegrationTest.java`.

Here is the design you should implement:

#### 1. Define the State
Create a simple nested class or standalone class called `TestState` extending `AgentState`:
```java
package com.myos.agent;

import org.bsc.langgraph4j.state.AgentState;
import java.util.Map;

public class TestState extends AgentState {
    public static final String RESPONSE_KEY = "response";

    public TestState(Map<String, Object> initData) {
        super(initData);
    }

    public String getResponse() {
        return value(RESPONSE_KEY).orElse("");
    }
}
```

#### 2. Define the Schema and Channels
Initialize the StateGraph using a schema that handles updates:
```java
package com.myos.agent;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.CompiledGraph;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LangGraph4jIntegrationTest {

    @Test
    public void testBasicStateGraph() throws Exception {
        // Define channels schema
        Map<String, Channel<?>> schema = Map.of(
            TestState.RESPONSE_KEY, Channels.set()
        );

        // Build state graph
        StateGraph<TestState> graph = new StateGraph<>(schema, TestState::new);

        // Add a node
        graph.addNode("processNode", state -> {
            String input = (String) state.data().get("input");
            return Map.of(TestState.RESPONSE_KEY, "Processed: " + input);
        });

        // Add edges
        graph.addEdge(START, "processNode");
        graph.addEdge("processNode", END);

        // Compile
        CompiledGraph<TestState> compiledGraph = graph.compile();

        // Execute
        TestState result = compiledGraph.invoke(Map.of("input", "Hello LangGraph4j"))
            .get(5, TimeUnit.SECONDS);

        // Assert
        assertEquals("Processed: Hello LangGraph4j", result.getResponse());
    }
}
```

This ensures your build pipeline is completely ready and the core LangGraph4j engine is perfectly operational!
