/*
 * Copyright (c) 2002 JSON.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * The software is provided "AS IS", without warranty of any kind, express or
 * implied, including but not limited to the warranties of merchantability,
 * fitness for a particular purpose and noninfringement. In no event shall the
 * authors or copyright holders be liable for any claim, damages or other
 * liability, whether in an action of contract, tort or otherwise, arising from,
 * out of or in connection with the software or the use or other dealings in the
 * software.
 */

package org.json;

/**
 * Enumeration defining different types of streaming behaviors for JSON processing.
 * This enum is used with the JSONObject.toStream() method to control which nodes
 * are included in the resulting stream.
 * 
 * <p>Different stream types offer different trade-offs between completeness and
 * performance, allowing developers to choose the most appropriate strategy for
 * their specific use case.
 * 
 * <p>Example usage:
 * <pre>
 * // Get all nodes in the JSON structure
 * Stream&lt;JSONNode&gt; allNodes = jsonObject.toStream(StreamType.ALL_NODES);
 * 
 * // Get only leaf nodes (actual data values)
 * Stream&lt;JSONNode&gt; leafNodes = jsonObject.toStream(StreamType.LEAF_NODES_ONLY);
 * 
 * // Get only top-level nodes
 * Stream&lt;JSONNode&gt; topLevel = jsonObject.toStream(StreamType.TOP_LEVEL_ONLY);
 * </pre>
 * 
 * @author Your Name
 * @version 1.0
 */
public enum StreamType {
    
    /**
     * Include all nodes in the JSON structure, regardless of their type or depth.
     * This includes objects, arrays, and primitive values at all nesting levels.
     * 
     * <p>This is the most comprehensive option but may produce large streams
     * for deeply nested JSON structures.
     * 
     * <p>Use this when you need complete access to the entire JSON structure
     * and want to process all elements uniformly.
     */
    ALL_NODES("Includes all nodes in the JSON structure"),
    
    /**
     * Include only leaf nodes that contain primitive values (strings, numbers, booleans, null).
     * This excludes intermediate objects and arrays, focusing only on the actual data values.
     * 
     * <p>This is useful when you're primarily interested in the actual data content
     * rather than the structural elements of the JSON.
     * 
     * <p>Use this for data extraction, value transformation, or when you want to
     * avoid processing structural elements.
     */
    LEAF_NODES_ONLY("Includes only leaf nodes with primitive values"),
    
    /**
     * Include only nodes at the top level (depth 0) of the JSON structure.
     * This provides a shallow view of the JSON object without descending into
     * nested objects or arrays.
     * 
     * <p>This is the most efficient option for large JSON structures when you
     * only need to process the immediate children of the root object.
     * 
     * <p>Use this for quick inspection of JSON structure or when you need to
     * process only the main sections of a JSON document.
     */
    TOP_LEVEL_ONLY("Includes only top-level nodes"),
    
    /**
     * Include nodes up to a specified depth limit.
     * This allows for controlled traversal of nested structures without
     * going too deep into the hierarchy.
     * 
     * <p>Note: This type requires additional configuration and is used
     * with overloaded stream methods that accept a depth parameter.
     */
    DEPTH_LIMITED("Includes nodes up to a specified depth limit"),
    
    /**
     * Include only nodes that match specific criteria.
     * This is used with filtering predicates to create custom streaming behaviors.
     * 
     * <p>Note: This type is used with overloaded stream methods that accept
     * filtering predicates.
     */
    FILTERED("Includes only nodes matching specific criteria");
    
    private final String description;
    
    /**
     * Constructs a StreamType with the given description.
     * 
     * @param description a human-readable description of the stream type
     */
    StreamType(String description) {
        this.description = description;
    }
    
    /**
     * Returns a human-readable description of this stream type.
     * 
     * @return the description string
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this stream type includes structural nodes (objects and arrays).
     * 
     * @return true if structural nodes are included, false otherwise
     */
    public boolean includesStructuralNodes() {
        return this == ALL_NODES || this == TOP_LEVEL_ONLY || this == DEPTH_LIMITED;
    }
    
    /**
     * Checks if this stream type includes leaf nodes (primitive values).
     * 
     * @return true if leaf nodes are included, false otherwise
     */
    public boolean includesLeafNodes() {
        return this == ALL_NODES || this == LEAF_NODES_ONLY;
    }
    
    /**
     * Checks if this stream type is depth-aware.
     * 
     * @return true if the stream type considers node depth, false otherwise
     */
    public boolean isDepthAware() {
        return this == TOP_LEVEL_ONLY || this == DEPTH_LIMITED;
    }
    
    /**
     * Returns the default stream type used when no type is specified.
     * 
     * @return the default StreamType
     */
    public static StreamType getDefault() {
        return ALL_NODES;
    }
    
    @Override
    public String toString() {
        return name() + ": " + description;
    }
}