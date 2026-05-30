# Build Guide: Defining Agent Graph Nodes

Welcome back! In the previous step, you successfully integrated the **LangGraph4j** framework and verified that a stateful workflow can compile and run.

Now, we are going to define the core building blocks of our multi-agent brain: **the domain-specific Agent Nodes**. 

---

## 1. Significance

In a complex, personal AI operating system, a single monolithic AI prompt cannot handle all duties (career matching, budget alerts, social planning, and habit tracking) effectively or reliably. 
- **Domain Specialization**: By segregating responsibilities into separate nodes (`JobNode`, `SocialNode`, etc.), each agent can maintain a highly specialized, focused prompt.
- **Modularity**: Individual nodes can be developed, improved, and tested in isolation without disrupting the rest of the agent network.
- **Dynamic Connection**: By implementing a shared interface and utilizing Spring's IoC container, we can dynamically compile these nodes into a unified `StateGraph` which responds contextually based on the user's intent.

---

## 2. Educational Concepts Deep Dive

Before writing code, let's review the key concepts you will be applying in this module:

### Spring Component Scan & Dependency Injection
- **`@Component`**: Tells Spring Boot that this class is a managed Bean. Spring's container will automatically detect, instantiate, and manage its lifecycle.
- **Constructor Injection**: A best practice in modern Spring. By declaring `final` dependency fields and providing a constructor, Spring automatically injects required dependencies (like `ChatModel`). This ensures thread-safety and easy testing via mock injection.

### LangGraph4j State and Nodes
- **`AgentState`**: A stateful dictionary that is passed between nodes in the graph. In LangGraph4j, the state is designed to be immutable during execution transitions. Each node accepts the current state and returns a map of *new or modified* values, which LangGraph4j merges into the state.
- **`NodeAction<State>`**: A functional interface in LangGraph4j. Implementing this interface requires you to override `Map<String, Object> apply(State state)`. This method contains the actual work the node performs.

### Spring AI Prompt Rendering
- **`PromptTemplate`**: A Spring AI class that acts as a blueprint for system or user instructions. It uses placeholders (e.g., `{input}`) which are dynamically replaced at runtime, protecting the application from query structure corruption and enabling customized context.

### Unit Testing with Mockito
- **`@ExtendWith(MockitoExtension.class)`**: Integrates JUnit 5 with Mockito, allowing us to mock external APIs (like LLM calls) so that our unit tests remain fast, stable, and offline-friendly.
- **`Mockito.mock()` and `when().thenReturn()`**: Techniques used to simulate LLM responses, ensuring that our tests focus strictly on verifying the node's local logic and state mutation.

---

## 3. Step-by-Step Build Guide

Let's implement the files in the backend.

### Step 1: Create the Shared Agent State

Create a file named `MyOsAgentState.java` in the `com.myos.service.agent` package.

```java
package com.myos.service.agent;

import org.bsc.langgraph4j.state.AgentState;
import java.util.Map;

/**
 * MyOsAgentState represents the custom data container that carries our agent context.
 * 
 * CONCEPT: Stateful Graph Communication
 * LangGraph4j operates on state. Instead of sharing global variables, each graph node
 * receives this state, reads attributes, performs work, and returns a Map containing
 * ONLY the updated fields. LangGraph4j then produces a new immutable state instance.
 */
public class MyOsAgentState extends AgentState {

    // Key definitions for state storage to prevent magic string typing errors.
    public static final String INPUT_KEY = "input";
    public static final String RESPONSE_KEY = "response";
    public static final String CURRENT_NODE_KEY = "currentNode";

    /**
     * Required constructor. LangGraph4j instantiates this state dynamically
     * using a factory reference (MyOsAgentState::new) after each transition.
     *
     * @param initData The initial map of state keys and values.
     */
    public MyOsAgentState(Map<String, Object> initData) {
        super(initData);
    }

    /**
     * Educational helper method to retrieve the user input query in a type-safe manner.
     *
     * @return The input query string, or an empty string if not present.
     */
    public String getInput() {
        return (String) value(INPUT_KEY).orElse("");
    }

    /**
     * Educational helper method to retrieve the current agent output response.
     *
     * @return The response string, or an empty string if not present.
     */
    public String getResponse() {
        return (String) value(RESPONSE_KEY).orElse("");
    }

    /**
     * Educational helper method to retrieve the active node name.
     *
     * @return The active node identifier, or an empty string if not present.
     */
    public String getCurrentNode() {
        return (String) value(CURRENT_NODE_KEY).orElse("");
    }
}
```

---

### Step 2: Create the Unified `AgentNode` Interface

Create an interface named `AgentNode.java` in the new `com.myos.service.agent.node` package.

```java
package com.myos.service.agent.node;

import com.myos.service.agent.MyOsAgentState;
import org.bsc.langgraph4j.action.NodeAction;

/**
 * DESIGN PATTERN: Unified Interface & Open-Closed Principle
 * By creating the AgentNode interface which extends LangGraph4j's NodeAction,
 * we enforce a strict contract for all domain nodes.
 *
 * This allows us to write dynamic graph initialization logic: Spring can automatically
 * inject a List<AgentNode> containing all active node beans, making it easy to add
 * new domain agents without changing existing graph compilation code!
 */
public interface AgentNode extends NodeAction<MyOsAgentState> {

    /**
     * Retrieves the unique identifier of the node (e.g. "JobNode", "SocialNode").
     *
     * @return The unique node identifier string.
     */
    String getName();
}
```

---

### Step 3: Implement the Six Domain Nodes

Now, create the following six implementations inside the `com.myos.service.agent.node` package. Each class is annotated with `@Component` and relies on constructor injection to access the primary `ChatModel` bean (delegating to your `DynamicChatModelFactory`).

#### 1. `JobNode.java` (Careers & Job Hunting)
```java
package com.myos.service.agent.node;

import com.myos.service.agent.MyOsAgentState;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * @Component registers this class as a Spring Bean managed by the Spring IoC container.
 * 
 * JobNode encapsulates the AI agent logic for career assistance and job scoring.
 */
@Component
public class JobNode implements AgentNode {

    private final ChatModel chatModel;

    // Constructor Injection: Spring resolves and supplies the ChatModel bean.
    public JobNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String getName() {
        return "JobNode";
    }

    @Override
    public Map<String, Object> apply(MyOsAgentState state) throws Exception {
        String input = state.getInput();

        // Dedicated Career Specialist system prompt
        String systemPrompt = """
            You are the MyOS Career and Job Search Agent. Your mission is to assist the user with:
            - Professional career growth, resume advice, and skill highlights.
            - Job matching recommendations, application strategy, and interview preparation.
            
            Current User Query: {input}
            
            Formulate a highly helpful, structured, and encouraging response.
            """;

        PromptTemplate template = new PromptTemplate(systemPrompt);
        Prompt prompt = template.create(Map.of("input", input));
        
        // Dynamic Model Connection call
        String output = chatModel.call(prompt).getResult().getOutput().getContent();

        // Return updated state keys
        return Map.of(
            MyOsAgentState.RESPONSE_KEY, output,
            MyOsAgentState.CURRENT_NODE_KEY, getName()
        );
    }
}
```

#### 2. `SocialNode.java` (Social Media Management)
```java
package com.myos.service.agent.node;

import com.myos.service.agent.MyOsAgentState;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class SocialNode implements AgentNode {

    private final ChatModel chatModel;

    public SocialNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String getName() {
        return "SocialNode";
    }

    @Override
    public Map<String, Object> apply(MyOsAgentState state) throws Exception {
        String input = state.getInput();

        String systemPrompt = """
            You are the MyOS Social Media Manager Agent. Your mission is to assist the user with:
            - Drafting engaging content, hooks, and posts for Twitter/X and LinkedIn.
            - Scheduling social updates, maintaining brand voice, and content strategy.
            
            Current User Query: {input}
            
            Provide creative draft options and clear scheduling suggestions.
            """;

        PromptTemplate template = new PromptTemplate(systemPrompt);
        Prompt prompt = template.create(Map.of("input", input));
        String output = chatModel.call(prompt).getResult().getOutput().getContent();

        return Map.of(
            MyOsAgentState.RESPONSE_KEY, output,
            MyOsAgentState.CURRENT_NODE_KEY, getName()
        );
    }
}
```

#### 3. `EmailNode.java` (Emails & Calendars)
```java
package com.myos.service.agent.node;

import com.myos.service.agent.MyOsAgentState;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class EmailNode implements AgentNode {

    private final ChatModel chatModel;

    public EmailNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String getName() {
        return "EmailNode";
    }

    @Override
    public Map<String, Object> apply(MyOsAgentState state) throws Exception {
        String input = state.getInput();

        String systemPrompt = """
            You are the MyOS Email and Calendar Assistant Agent. Your mission is to assist the user with:
            - Summarizing unread email threads and clustering notifications.
            - Drafting smart, context-rich responses.
            - Scheduling meetings, managing time blocks, and resolving calendar conflicts.
            
            Current User Query: {input}
            
            Formulate a clear, brief, and structured email action plan or summary.
            """;

        PromptTemplate template = new PromptTemplate(systemPrompt);
        Prompt prompt = template.create(Map.of("input", input));
        String output = chatModel.call(prompt).getResult().getOutput().getContent();

        return Map.of(
            MyOsAgentState.RESPONSE_KEY, output,
            MyOsAgentState.CURRENT_NODE_KEY, getName()
        );
    }
}
```

#### 4. `FinanceNode.java` (Budgets & Expenses)
```java
package com.myos.service.agent.node;

import com.myos.service.agent.MyOsAgentState;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class FinanceNode implements AgentNode {

    private final ChatModel chatModel;

    public FinanceNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String getName() {
        return "FinanceNode";
    }

    @Override
    public Map<String, Object> apply(MyOsAgentState state) throws Exception {
        String input = state.getInput();

        String systemPrompt = """
            You are the MyOS Personal Finance Agent. Your mission is to assist the user with:
            - Tracking manual expenses, comparing actuals against monthly budgets, and analyzing savings goals.
            - Offering tailored spending tips and detecting high-priority budget alerts.
            
            Current User Query: {input}
            
            Formulate an organized, numerically clear, and responsible financial summary.
            """;

        PromptTemplate template = new PromptTemplate(systemPrompt);
        Prompt prompt = template.create(Map.of("input", input));
        String output = chatModel.call(prompt).getResult().getOutput().getContent();

        return Map.of(
            MyOsAgentState.RESPONSE_KEY, output,
            MyOsAgentState.CURRENT_NODE_KEY, getName()
        );
    }
}
```

#### 5. `LearningNode.java` (Roadmaps & Courses)
```java
package com.myos.service.agent.node;

import com.myos.service.agent.MyOsAgentState;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class LearningNode implements AgentNode {

    private final ChatModel chatModel;

    public LearningNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String getName() {
        return "LearningNode";
    }

    @Override
    public Map<String, Object> apply(MyOsAgentState state) throws Exception {
        String input = state.getInput();

        String systemPrompt = """
            You are the MyOS Learning and Tutor Agent. Your mission is to assist the user with:
            - Developing curriculum roadmaps, identifying skill gaps, and scheduling study slots.
            - Recommending premium resources (e.g., specific concepts, articles, lectures).
            
            Current User Query: {input}
            
            Formulate a clear educational breakdown or actionable learning syllabus.
            """;

        PromptTemplate template = new PromptTemplate(systemPrompt);
        Prompt prompt = template.create(Map.of("input", input));
        String output = chatModel.call(prompt).getResult().getOutput().getContent();

        return Map.of(
            MyOsAgentState.RESPONSE_KEY, output,
            MyOsAgentState.CURRENT_NODE_KEY, getName()
        );
    }
}
```

#### 6. `HabitNode.java` (Habits & Streaks)
```java
package com.myos.service.agent.node;

import com.myos.service.agent.MyOsAgentState;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class HabitNode implements AgentNode {

    private final ChatModel chatModel;

    public HabitNode(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String getName() {
        return "HabitNode";
    }

    @Override
    public Map<String, Object> apply(MyOsAgentState state) throws Exception {
        String input = state.getInput();

        String systemPrompt = """
            You are the MyOS Health and Habit Tracker Agent. Your mission is to assist the user with:
            - Tracking daily routines, calculating streaks, and analyzing health dashboards.
            - Generating motivational insights and outlining progress parameters.
            
            Current User Query: {input}
            
            Formulate a highly structured, habit-affirming summary or check-in verification.
            """;

        PromptTemplate template = new PromptTemplate(systemPrompt);
        Prompt prompt = template.create(Map.of("input", input));
        String output = chatModel.call(prompt).getResult().getOutput().getContent();

        return Map.of(
            MyOsAgentState.RESPONSE_KEY, output,
            MyOsAgentState.CURRENT_NODE_KEY, getName()
        );
    }
}
```

---

### Step 4: Write Unit Tests for Node State Processing

Create a test class `AgentNodesTest.java` in `src/test/java/com/myos/service/agent` to verify all components perform perfectly and integrate cleanly with standard Spring AI prompts.

```java
package com.myos.service.agent;

import com.myos.service.agent.node.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Educational Unit Test for MyOS Agent Nodes.
 *
 * ANNOTATION: @ExtendWith(MockitoExtension.class)
 * Extends JUnit 5 with Mockito capabilities, enabling annotation-based mock injections
 * so that we don't have to initialize mocks manually in our @BeforeEach setup.
 */
@ExtendWith(MockitoExtension.class)
public class AgentNodesTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private ChatResponse chatResponse;

    @Mock
    private Generation generation;

    @Mock
    private org.springframework.ai.chat.metadata.ChatGenerationMetadata chatGenerationMetadata;

    @BeforeEach
    public void setUp() {
        // Setup mock response chain: ChatModel -> ChatResponse -> Generation -> AssistantContent
        // We mock this chain once since all nodes leverage the same call structure.
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(new org.springframework.ai.chat.messages.AssistantMessage("Mock response output"));
    }

    @Test
    public void testAllAgentNodesExecuteCorrectly() throws Exception {
        // Arrange
        List<AgentNode> nodes = List.of(
            new JobNode(chatModel),
            new SocialNode(chatModel),
            new EmailNode(chatModel),
            new FinanceNode(chatModel),
            new LearningNode(chatModel),
            new HabitNode(chatModel)
        );

        // Act & Assert
        for (AgentNode node : nodes) {
            // Instantiate input state
            MyOsAgentState state = new MyOsAgentState(Map.of(
                MyOsAgentState.INPUT_KEY, "Test input inquiry"
            ));

            // Execute node logic
            Map<String, Object> updates = node.apply(state);

            // Assertions to verify correct state mutation
            assertEquals("Mock response output", updates.get(MyOsAgentState.RESPONSE_KEY), 
                node.getName() + " failed to write the correct response to the state.");
            
            assertEquals(node.getName(), updates.get(MyOsAgentState.CURRENT_NODE_KEY), 
                node.getName() + " failed to update the active node key.");
        }
    }
}
```

---

## 4. Verification Execution

Once you have created these files, compile the project and execute your unit tests to ensure everything is correct and there are no classpath conflicts:

```powershell
# Navigate to the backend directory if needed
cd backend

# Compile and execute the Agent Node verification tests
.\mvnw.cmd test -Dtest=AgentNodesTest
```

If the build completes successfully and the tests pass, you are ready to proceed with implementing the graph routing configuration! Keep up the great work!
