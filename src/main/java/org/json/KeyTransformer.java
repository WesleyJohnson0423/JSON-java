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
 * A functional interface for transforming JSON object keys during XML to JSON conversion.
 * This interface allows custom key transformation logic to be applied during the parsing process,
 * improving performance by avoiding the need for post-processing.
 * 
 * <p>Example usage:
 * <pre>
 * KeyTransformer prefixTransformer = key -> "prefix_" + key;
 * KeyTransformer upperCaseTransformer = String::toUpperCase;
 * KeyTransformer reverseTransformer = key -> new StringBuilder(key).reverse().toString();
 * 
 * JSONObject result = XML.toJSONObject(reader, prefixTransformer);
 * </pre>
 * 
 * @author Your Name
 * @version 1.0
 */
@FunctionalInterface
public interface KeyTransformer {
    
    /**
     * Transforms a JSON object key.
     * 
     * @param key the original key from the XML element or attribute
     * @return the transformed key to be used in the resulting JSON object
     * @throws IllegalArgumentException if the key transformation results in an invalid key
     */
    String transform(String key);
    
    /**
     * Returns a KeyTransformer that applies a prefix to all keys.
     * 
     * @param prefix the prefix to add to all keys
     * @return a KeyTransformer that adds the specified prefix
     */
    static KeyTransformer withPrefix(String prefix) {
        return key -> prefix + key;
    }
    
    /**
     * Returns a KeyTransformer that applies a suffix to all keys.
     * 
     * @param suffix the suffix to add to all keys
     * @return a KeyTransformer that adds the specified suffix
     */
    static KeyTransformer withSuffix(String suffix) {
        return key -> key + suffix;
    }
    
    /**
     * Returns a KeyTransformer that converts all keys to uppercase.
     * 
     * @return a KeyTransformer that converts keys to uppercase
     */
    static KeyTransformer toUpperCase() {
        return String::toUpperCase;
    }
    
    /**
     * Returns a KeyTransformer that converts all keys to lowercase.
     * 
     * @return a KeyTransformer that converts keys to lowercase
     */
    static KeyTransformer toLowerCase() {
        return String::toLowerCase;
    }
    
    /**
     * Returns a KeyTransformer that reverses all keys.
     * 
     * @return a KeyTransformer that reverses the key strings
     */
    static KeyTransformer reverse() {
        return key -> new StringBuilder(key).reverse().toString();
    }
    
    /**
     * Returns a composed KeyTransformer that first applies this transformer
     * and then applies the after transformer to the result.
     * 
     * @param after the transformer to apply after this transformer
     * @return a composed KeyTransformer
     * @throws NullPointerException if after is null
     */
    default KeyTransformer andThen(KeyTransformer after) {
        if (after == null) {
            throw new NullPointerException("after transformer cannot be null");
        }
        return key -> after.transform(this.transform(key));
    }
}