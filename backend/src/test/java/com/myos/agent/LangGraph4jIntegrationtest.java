package com.myos.agent;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.CompiledGraph;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Educational Integration Test for LangGraph4j.
 * 
 * DESIGN PATTERN: Builder Pattern & Stateful Graph Architecture
 * We use a "StateGraph" builder to define nodes (computational steps) and edges (control flow),
 * which is then compiled into a thread-safe, immutable "CompiledGraph" representing our agent execution plan.
 */
public class LangGraph4jIntegrationtest {

    /**
     * TestState represents the data container that flows through our agent graph.
     * 
     * CONCEPT: AgentState
     * LangGraph4j uses a stateful architecture. Every node receives the current state, 
     * processes it, and returns an updated portion of the state. We extend AgentState 
     * to provide type-safe convenience methods for accessing our state keys.
     */
    public static class TestState extends AgentState {
        // A unique key in our state map for the processed output.
        public static final String RESPONSE_KEY = "response";

        /**
         * Constructor required to initialize the immutable state map.
         *
         * @param initData Initial state data passed to the graph.
         */
        public TestState(Map<String, Object> initData) {
            super(initData);
        }

        /**
         * Type-safe helper to retrieve the response field from the state map.
         *
         * @return The response string, or an empty string if not present.
         */
        public String getResponse() {
            // In LangGraph4j, the raw value() method returns an Optional of Object.
            // We explicitly cast the value to String to ensure compile-time safety.
            return (String) value(RESPONSE_KEY).orElse("");
        }
    }

    /**
     * ANNOTATION: @Test
     * From JUnit 5, this annotation marks the method as a test case that JUnit's 
     * runner will execute automatically. It requires the method to be public (or package-private) 
     * and return void.
     */
    @Test
    public void testBasicStateGraph() throws Exception {
        
        /*
         * CONCEPT: Channels & State Schema
         * Channels define how state attributes are updated when multiple nodes write to the same key.
         * We use Channels.base(() -> "") to initialize the response channel with a default empty string.
         * Using a lambda like `() -> ""` instead of `String::new` prevents compiler ambiguity between 
         * Supplier and Reducer overloads.
         */
        Map<String, Channel<?>> schema = Map.of(
            TestState.RESPONSE_KEY, Channels.base(() -> "")
        );

        /*
         * CONCEPT: StateGraph Builder
         * We instantiate a mutable StateGraph with our state schema and a factory reference (TestState::new)
         * which allows LangGraph4j to instantiate new state instances after every transition.
         */
        StateGraph<TestState> graph = new StateGraph<>(schema, TestState::new);

        /*
         * CONCEPT: Graph Nodes & Async Lambda Ambiguity
         * A "Node" represents a unit of work. To avoid lambda overload ambiguity with compiler,
         * we wrap our synchronous lambda in LangGraph4j's static helper method: node_async(...)
         */
        graph.addNode("processNode", node_async(state -> {
            // Retrieve input value from the incoming state map
            String input = (String) state.data().get("input");
            
            // Return only the keys that we want to update/overwrite in the state
            return Map.of(TestState.RESPONSE_KEY, "Processed: " + input);
        }));

        /*
         * CONCEPT: Graph Edges
         * Edges direct the execution flow.
         * - START: The entry point of the graph.
         * - END: A special terminal node that stops execution.
         */
        graph.addEdge(START, "processNode");
        graph.addEdge("processNode", END);

        /*
         * CONCEPT: CompiledGraph
         * Calling .compile() validates the graph structure (ensures no cycle loops without escapes or orphaned nodes)
         * and returns an immutable, executable CompiledGraph instance.
         */
        CompiledGraph<TestState> compiledGraph = graph.compile();

        /*
         * CONCEPT: Synchronous Invocation
         * In LangGraph4j 1.8.x, compiledGraph.invoke() runs synchronously and returns an Optional<TestState>.
         * We use orElseThrow() to retrieve the resulting state directly.
         */
        TestState result = compiledGraph.invoke(Map.of("input", "Hello LangGraph4j"))
            .orElseThrow(() -> new IllegalStateException("Graph execution did not return a state"));

        /*
         * ASSERTION: Verification
         * We verify that the node was visited, executed, and correctly modified the state key.
         */
        assertEquals("Processed: Hello LangGraph4j", result.getResponse());
    }
}
