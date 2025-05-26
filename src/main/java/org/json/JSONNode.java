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

import java.util.Objects;

/**
 * Represents a node in a JSON structure for stream processing operations.
 * Each JSONNode contains information about its path, key, value, and depth within
 * the JSON hierarchy, enabling flexible stream-based operations on JSON data.
 * 
 * <p>This class is designed to work with the streaming API introduced for JSONObject,
 * allowing developers to process JSON data in a functional programming style using
 * Java 8+ streams.
 * 
 * <p>Example usage:
 * <pre>
 * JSONObject obj = XML.toJSONObject(xmlString);
 * List&lt;String&gt; titles = obj.toStream()
 *     .filter(node -&gt; "title".equals(node.getKey()))
 *     .map(node -&gt; node.getValue().toString())
 *     .collect(Collectors.toList());
 * </pre>
 * 
 * @author Your Name
 * @version 1.0
 */
public class JSONNode {
    private final String path;
    private final String key;
    private final Object value;
    private final int depth;
    
    /**
     * Constructs a new JSONNode with the specified properties.
     * 
     * @param path the full path to this node in the JSON structure (e.g., "root.books.book[0].title")
     * @param key the key name of this node (e.g., "title")
     * @param value the value associated with this node
     * @param depth the nesting depth of this node (0 for root level)
     */
    public JSONNode(String path, String key, Object value, int depth) {
        this.path = path != null ? path : "";
        this.key = key != null ? key : "";
        this.value = value;
        this.depth = Math.max(0, depth);
    }
    
    /**
     * Returns the full path to this node in the JSON structure.
     * 
     * @return the path string, using dot notation for objects and bracket notation for arrays
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Returns the key name of this node.
     * 
     * @return the key string, or array index as string for array elements
     */
    public String getKey() {
        return key;
    }
    
    /**
     * Returns the value associated with this node.
     * 
     * @return the value, which can be a primitive, JSONObject, JSONArray, or null
     */
    public Object getValue() {
        return value;
    }
    
    /**
     * Returns the nesting depth of this node.
     * 
     * @return the depth, where 0 represents the root level
     */
    public int getDepth() {
        return depth;
    }
    
    /**
     * Checks if this node's value is a JSONObject.
     * 
     * @return true if the value is a JSONObject, false otherwise
     */
    public boolean isObject() {
        return value instanceof JSONObject;
    }
    
    /**
     * Checks if this node's value is a JSONArray.
     * 
     * @return true if the value is a JSONArray, false otherwise
     */
    public boolean isArray() {
        return value instanceof JSONArray;
    }
    
    /**
     * Checks if this node is a leaf node (contains a primitive value).
     * 
     * @return true if the value is neither JSONObject nor JSONArray, false otherwise
     */
    public boolean isLeaf() {
        return !isObject() && !isArray();
    }
    
    /**
     * Checks if this node's value is null.
     * 
     * @return true if the value is null, false otherwise
     */
    public boolean isNull() {
        return value == null || value == JSONObject.NULL;
    }
    
    /**
     * Returns the value as a JSONObject.
     * 
     * @return the value cast to JSONObject
     * @throws ClassCastException if the value is not a JSONObject
     */
    public JSONObject asObject() {
        if (isObject()) {
            return (JSONObject) value;
        }
        throw new ClassCastException("Node value is not a JSONObject: " + value);
    }
    
    /**
     * Returns the value as a JSONArray.
     * 
     * @return the value cast to JSONArray
     * @throws ClassCastException if the value is not a JSONArray
     */
    public JSONArray asArray() {
        if (isArray()) {
            return (JSONArray) value;
        }
        throw new ClassCastException("Node value is not a JSONArray: " + value);
    }
    
    /**
     * Returns the value as a String.
     * 
     * @return the string representation of the value
     */
    public String asString() {
        return value != null ? value.toString() : null;
    }
    
    /**
     * Returns the value as an Integer.
     * 
     * @return the value as Integer
     * @throws NumberFormatException if the value cannot be converted to Integer
     * @throws NullPointerException if the value is null
     */
    public Integer asInt() {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(value.toString());
    }
    
    /**
     * Returns the value as a Double.
     * 
     * @return the value as Double
     * @throws NumberFormatException if the value cannot be converted to Double
     * @throws NullPointerException if the value is null
     */
    public Double asDouble() {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.valueOf(value.toString());
    }
    
    /**
     * Returns the value as a Boolean.
     * 
     * @return the value as Boolean
     * @throws IllegalArgumentException if the value cannot be converted to Boolean
     */
    public Boolean asBoolean() {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String str = value.toString().toLowerCase();
        if ("true".equals(str) || "1".equals(str)) {
            return Boolean.TRUE;
        }
        if ("false".equals(str) || "0".equals(str)) {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("Cannot convert to Boolean: " + value);
    }
    
    /**
     * Checks if this node matches a specific path pattern.
     * 
     * @param pattern the path pattern to match (supports wildcards with *)
     * @return true if the path matches the pattern
     */
    public boolean pathMatches(String pattern) {
        if (pattern == null) {
            return false;
        }
        return path.matches(pattern.replace("*", ".*"));
    }
    
    /**
     * Checks if this node's key matches a specific pattern.
     * 
     * @param pattern the key pattern to match
     * @return true if the key matches the pattern
     */
    public boolean keyMatches(String pattern) {
        if (pattern == null) {
            return false;
        }
        return key.matches(pattern);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        JSONNode jsonNode = (JSONNode) obj;
        return depth == jsonNode.depth &&
               Objects.equals(path, jsonNode.path) &&
               Objects.equals(key, jsonNode.key) &&
               Objects.equals(value, jsonNode.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(path, key, value, depth);
    }
    
    @Override
    public String toString() {
        return String.format("JSONNode{path='%s', key='%s', value=%s, depth=%d, type=%s}", 
                           path, key, 
                           value instanceof String ? "\"" + value + "\"" : value, 
                           depth,
                           getNodeType());
    }
    
    /**
     * Returns a human-readable description of the node type.
     * 
     * @return the node type as a string
     */
    public String getNodeType() {
        if (isNull()) return "null";
        if (isObject()) return "object";
        if (isArray()) return "array";
        if (value instanceof String) return "string";
        if (value instanceof Number) return "number";
        if (value instanceof Boolean) return "boolean";
        return "unknown";
    }
}