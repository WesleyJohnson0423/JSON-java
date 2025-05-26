package org.json.junit;

import org.json.*;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashSet;

/**
 * Test class for Milestone 4: Streaming API functionality.
 * Tests the streaming capabilities of JSONObject with various stream types and operations.
 */
public class JSONStreamTest {

    private JSONObject simpleObject;
    private JSONObject complexObject;
    private JSONObject nestedObject;
    private JSONObject arrayObject;
    private String booksXML;
    private String companyXML;

    @Before
    public void setUp() throws Exception {
        // Simple object
        simpleObject = new JSONObject();
        simpleObject.put("name", "John");  // Changed from "n" to "name"
        simpleObject.put("age", 30);
        simpleObject.put("active", true);

        // Books XML for complex testing
        booksXML = "<Books>" +
                "<book><title>AAA</title><author>ASmith</author><price>10</price><genre>Fiction</genre></book>" +
                "<book><title>BBB</title><author>BSmith</author><price>20</price><genre>Non-Fiction</genre></book>" +
                "<book><title>CCC</title><author>CSmith</author><price>15</price><genre>Science</genre></book>" +
                "</Books>";
        complexObject = XML.toJSONObject(booksXML);

        // Company XML for nested testing
        companyXML = "<company>" +
                "<name>TechCorp</name>" +  // Changed from "n" to "name"
                "<departments>" +
                "<department>" +
                "<name>Engineering</name>" +  // Changed from "n" to "name"
                "<employees>" +
                "<employee><name>Alice</name><role>Developer</role></employee>" +  // Changed from "n" to "name"
                "<employee><name>Bob</name><role>Manager</role></employee>" +  // Changed from "n" to "name"
                "</employees>" +
                "</department>" +
                "<department>" +
                "<name>Sales</name>" +  // Changed from "n" to "name"
                "<employees>" +
                "<employee><name>Charlie</name><role>Rep</role></employee>" +  // Changed from "n" to "name"
                "</employees>" +
                "</department>" +
                "</departments>" +
                "</company>";
        nestedObject = XML.toJSONObject(companyXML);

        // Array object
        arrayObject = new JSONObject();
        JSONArray numbers = new JSONArray();
        for (int i = 1; i <= 5; i++) {
            numbers.put(i * 10);
        }
        arrayObject.put("numbers", numbers);
        arrayObject.put("description", "Number array test");
    }

    @Test
    public void testBasicStreamAllNodes() {
        List<JSONNode> allNodes = simpleObject.toStream().collect(Collectors.toList());

        assertTrue("Should have multiple nodes", allNodes.size() > 0);
        assertEquals("Should have exactly 3 nodes", 3, allNodes.size());

        // Verify all expected keys are present
        Set<String> keys = allNodes.stream()
                .map(JSONNode::getKey)
                .collect(Collectors.toSet());

        assertTrue("Should contain name key", keys.contains("name"));
        assertTrue("Should contain age key", keys.contains("age"));
        assertTrue("Should contain active key", keys.contains("active"));
    }

    @Test
    public void testStreamForEach() {
        AtomicInteger counter = new AtomicInteger(0);
        Set<String> processedKeys = new HashSet<>();

        simpleObject.toStream().forEach(node -> {
            counter.incrementAndGet();
            processedKeys.add(node.getKey());
            assertNotNull("Node should not be null", node);
            assertNotNull("Node path should not be null", node.getPath());
            assertNotNull("Node key should not be null", node.getKey());
        });

        assertEquals("Should process all nodes", 3, counter.get());
        assertEquals("Should process all unique keys", 3, processedKeys.size());
    }

    @Test
    public void testStreamMapAndCollect() {
        List<String> titles = complexObject.toStream()
                .filter(node -> "title".equals(node.getKey()))
                .map(node -> node.getValue().toString())
                .collect(Collectors.toList());

        assertEquals("Should find 3 titles", 3, titles.size());
        assertTrue("Should contain AAA", titles.contains("AAA"));
        assertTrue("Should contain BBB", titles.contains("BBB"));
        assertTrue("Should contain CCC", titles.contains("CCC"));
    }

    @Test
    public void testStreamFilter() {
        List<JSONNode> expensiveBooks = complexObject.toStream()
                .filter(node -> "price".equals(node.getKey()))
                .filter(node -> {
                    try {
                        return Integer.parseInt(node.getValue().toString()) >= 15;  // Changed from > 15 to >= 15
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        assertEquals("Should find 2 expensive books (price >= 15)", 2, expensiveBooks.size());

        // Verify the prices are correct
        List<Integer> prices = expensiveBooks.stream()
                .map(node -> Integer.parseInt(node.getValue().toString()))
                .collect(Collectors.toList());

        assertTrue("Should contain price 20", prices.contains(20));
        assertTrue("Should contain price 15", prices.contains(15));  // Changed from duplicate 20 to 15
    }

    @Test
    public void testStreamLeafNodesOnly() {
        List<JSONNode> leafNodes = complexObject.toStream(StreamType.LEAF_NODES_ONLY)
                .collect(Collectors.toList());

        // Verify all nodes are leaf nodes
        assertTrue("All nodes should be leaf nodes",
                leafNodes.stream().allMatch(JSONNode::isLeaf));

        // Should contain actual values, not structural elements
        List<String> values = leafNodes.stream()
                .map(node -> node.getValue().toString())
                .collect(Collectors.toList());

        assertTrue("Should contain book titles", values.contains("AAA"));
        assertTrue("Should contain author names", values.contains("ASmith"));
        assertTrue("Should contain prices", values.contains("10"));
    }

    @Test
    public void testStreamTopLevelOnly() {
        List<JSONNode> topLevelNodes = complexObject.toStream(StreamType.TOP_LEVEL_ONLY)
                .collect(Collectors.toList());

        // All nodes should be at depth 0
        assertTrue("All nodes should be at depth 0",
                topLevelNodes.stream().allMatch(node -> node.getDepth() == 0));

        // Should only contain the top-level keys
        Set<String> topLevelKeys = topLevelNodes.stream()
                .map(JSONNode::getKey)
                .collect(Collectors.toSet());

        // The exact keys depend on the XML structure, but should be limited
        assertTrue("Should have limited top-level keys", topLevelKeys.size() <= 2);
    }

    @Test
    public void testStreamWithDepthLimit() {
        List<JSONNode> limitedDepthNodes = nestedObject.toStream(2)
                .collect(Collectors.toList());

        // All nodes should be at depth <= 2
        assertTrue("All nodes should be within depth limit",
                limitedDepthNodes.stream().allMatch(node -> node.getDepth() <= 2));

        // Should have more nodes than top-level but less than all nodes
        int topLevelCount = nestedObject.toStream(StreamType.TOP_LEVEL_ONLY).collect(Collectors.toList()).size();
        int allNodesCount = nestedObject.toStream(StreamType.ALL_NODES).collect(Collectors.toList()).size();

        assertTrue("Limited depth should have more than top-level", limitedDepthNodes.size() >= topLevelCount);
        assertTrue("Limited depth should have less than all nodes", limitedDepthNodes.size() <= allNodesCount);
    }

    @Test
    public void testValueStream() {
        List<String> values = simpleObject.valueStream().collect(Collectors.toList());

        assertTrue("Should contain string values", values.size() > 0);
        assertTrue("Should contain John", values.contains("John"));
        assertTrue("Should contain 30", values.contains("30"));
        assertTrue("Should contain true", values.contains("true"));
    }

    @Test
    public void testKeyStream() {
        List<String> keys = simpleObject.keyStream()
                .filter(key -> !key.isEmpty())
                .collect(Collectors.toList());

        assertTrue("Should contain keys", keys.size() > 0);
        assertTrue("Should contain name", keys.contains("name"));
        assertTrue("Should contain age", keys.contains("age"));
        assertTrue("Should contain active", keys.contains("active"));
    }

    @Test
    public void testPathStream() {
        List<String> paths = complexObject.pathStream()
                .filter(path -> !path.isEmpty())
                .filter(path -> path.contains("title"))
                .collect(Collectors.toList());

        assertTrue("Should find title paths", paths.size() > 0);
        // Paths should contain references to title elements
        assertTrue("Should have paths containing title",
                paths.stream().anyMatch(path -> path.contains("title")));
    }

    @Test
    public void testStreamWithArrays() {
        List<JSONNode> arrayNodes = arrayObject.toStream().collect(Collectors.toList());

        // Should contain both the array itself and its elements
        boolean hasArrayNode = arrayNodes.stream()
                .anyMatch(node -> node.getValue() instanceof JSONArray);
        boolean hasNumberNodes = arrayNodes.stream()
                .anyMatch(node -> node.getValue() instanceof Integer);

        assertTrue("Should contain array node", hasArrayNode);
        assertTrue("Should contain number nodes", hasNumberNodes);
    }

    @Test
    public void testStreamNodeProperties() {
        JSONNode firstNode = simpleObject.toStream().findFirst().orElse(null);

        assertNotNull("Should have at least one node", firstNode);
        assertNotNull("Path should not be null", firstNode.getPath());
        assertNotNull("Key should not be null", firstNode.getKey());
        assertNotNull("Value should not be null", firstNode.getValue());
        assertTrue("Depth should be non-negative", firstNode.getDepth() >= 0);

        // Test node type detection
        if (firstNode.getValue() instanceof JSONObject) {
            assertTrue("Should detect object type", firstNode.isObject());
            assertFalse("Should not be leaf", firstNode.isLeaf());
        } else if (firstNode.getValue() instanceof JSONArray) {
            assertTrue("Should detect array type", firstNode.isArray());
            assertFalse("Should not be leaf", firstNode.isLeaf());
        } else {
            assertTrue("Should detect leaf type", firstNode.isLeaf());
            assertFalse("Should not be object", firstNode.isObject());
            assertFalse("Should not be array", firstNode.isArray());
        }
    }

    @Test
    public void testStreamNodeConversion() {
        List<JSONNode> nodes = simpleObject.toStream().collect(Collectors.toList());

        for (JSONNode node : nodes) {
            // Test string conversion
            String stringValue = node.asString();
            assertNotNull("String value should not be null", stringValue);

            // Test type-specific conversions based on actual values
            if ("age".equals(node.getKey())) {
                Integer intValue = node.asInt();
                assertEquals("Should convert age to int", Integer.valueOf(30), intValue);

                Double doubleValue = node.asDouble();
                assertEquals("Should convert age to double", Double.valueOf(30.0), doubleValue);
            }

            if ("active".equals(node.getKey())) {
                Boolean boolValue = node.asBoolean();
                assertEquals("Should convert active to boolean", Boolean.TRUE, boolValue);
            }
        }
    }

    @Test
    public void testStreamPerformance() {
        // Create a larger object for performance testing
        JSONObject largeObject = new JSONObject();
        for (int i = 0; i < 1000; i++) {
            JSONObject item = new JSONObject();
            item.put("id", i);
            item.put("name", "Item" + i);  // Changed from "n" to "name"
            item.put("value", i * 10);
            largeObject.put("item" + i, item);
        }

        // Test streaming performance
        long startTime = System.currentTimeMillis();
        List<JSONNode> streamResult = largeObject.toStream()
                .filter(node -> "name".equals(node.getKey()))
                .collect(Collectors.toList());
        long streamTime = System.currentTimeMillis() - startTime;

        System.out.println("Stream processing time: " + streamTime + "ms");
        System.out.println("Processed nodes: " + streamResult.size());

        assertNotNull("Stream result should not be null", streamResult);
        assertTrue("Should process in reasonable time", streamTime < 5000); // 5 seconds max
    }

    @Test
    public void testStreamChaining() {
        // Test complex stream chaining operations
        List<String> result = complexObject.toStream()
                .filter(node -> "author".equals(node.getKey()) || "title".equals(node.getKey()))
                .map(node -> node.getKey() + ": " + node.getValue())
                .sorted()
                .collect(Collectors.toList());

        assertNotNull("Chained result should not be null", result);
        assertTrue("Should have results from chaining", result.size() > 0);

        // Verify the format of results
        assertTrue("Should contain formatted entries",
                result.stream().anyMatch(s -> s.startsWith("title:") || s.startsWith("author:")));
    }

    @Test
    public void testStreamWithNullValues() {
        JSONObject objectWithNulls = new JSONObject();
        objectWithNulls.put("validKey", "validValue");
        objectWithNulls.put("nullKey", JSONObject.NULL);
        objectWithNulls.put("emptyKey", "");

        List<JSONNode> nodes = objectWithNulls.toStream().collect(Collectors.toList());

        assertEquals("Should have all nodes including nulls", 3, nodes.size());

        // Test null detection
        boolean hasNullNode = nodes.stream()
                .anyMatch(node -> node.isNull());

        assertTrue("Should detect null node", hasNullNode);
    }

    @Test
    public void testParallelStream() {
        List<JSONNode> parallelResult = complexObject.toStream(StreamType.ALL_NODES)
                .parallel()
                .filter(node -> node.isLeaf())
                .collect(Collectors.toList());

        List<JSONNode> sequentialResult = complexObject.toStream(StreamType.LEAF_NODES_ONLY)
                .collect(Collectors.toList());

        // Results should be equivalent (though possibly in different order)
        assertEquals("Parallel and sequential should have same count",
                sequentialResult.size(), parallelResult.size());
    }

    @Test
    public void testEmptyObjectStream() {
        JSONObject emptyObject = new JSONObject();
        List<JSONNode> emptyResult = emptyObject.toStream().collect(Collectors.toList());

        assertTrue("Empty object should produce empty stream", emptyResult.isEmpty());
    }
}