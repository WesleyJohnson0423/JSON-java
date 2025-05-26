package org.json.junit;

import org.json.*;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.io.StringReader;

/**
 * Test class for Milestone 3: Functional Programming - Key Transformer functionality.
 * Tests the KeyTransformer interface and XML parsing with key transformation.
 */
public class KeyTransformerTest {

    private String simpleXML;
    private String complexXML;
    private String nestedXML;
    private String attributeXML;

    @Before
    public void setUp() {
        simpleXML = "<root><name>John</name><age>30</age></root>";

        complexXML = "<books>" +
                "<book id=\"1\" category=\"fiction\">" +
                "<title>Book One</title>" +
                "<author>Author One</author>" +
                "<price>19.99</price>" +
                "</book>" +
                "<book id=\"2\" category=\"non-fiction\">" +
                "<title>Book Two</title>" +
                "<author>Author Two</author>" +
                "<price>24.99</price>" +
                "</book>" +
                "</books>";

        nestedXML = "<company>" +
                "<department name=\"Engineering\">" +
                "<employee>" +
                "<name>Alice</name>" +
                "<role>Developer</role>" +
                "</employee>" +
                "<employee>" +
                "<name>Bob</name>" +
                "<role>Manager</role>" +
                "</employee>" +
                "</department>" +
                "</company>";

        attributeXML = "<person id=\"123\" active=\"true\">" +
                "<name>Jane</name>" +
                "<email>jane@example.com</email>" +
                "</person>";
    }

    /**
     * Test basic KeyTransformer functionality with simple prefix transformation
     */
    @Test
    public void testBasicKeyTransformer() throws Exception {
        StringReader reader = new StringReader(simpleXML);
        KeyTransformer transformer = key -> "prefix_" + key;

        JSONObject result = XML.toJSONObject(reader, transformer);

        // Test that root key is transformed
        assertTrue("Should have transformed root key", result.has("prefix_root"));

        // Get the root object and verify nested transformations
        JSONObject root = result.getJSONObject("prefix_root");
        assertTrue("Should have transformed name key", root.has("prefix_name"));
        assertTrue("Should have transformed age key", root.has("prefix_age"));

        // Verify values are preserved correctly
        assertEquals("Name value should be preserved", "John", getValueFromElement(root, "prefix_name"));
        assertEquals("Age value should be preserved", "30", getValueFromElement(root, "prefix_age"));
    }

    /**
     * Test the static withPrefix method
     */
    @Test
    public void testPrefixTransformer() throws Exception {
        StringReader reader = new StringReader(simpleXML);
        KeyTransformer transformer = KeyTransformer.withPrefix("swe262_");

        JSONObject result = XML.toJSONObject(reader, transformer);

        assertTrue("Should have prefixed root key", result.has("swe262_root"));
        JSONObject root = result.getJSONObject("swe262_root");

        assertTrue("Should have prefixed name key", root.has("swe262_name"));
        assertTrue("Should have prefixed age key", root.has("swe262_age"));

        assertEquals("Should preserve name value", "John", getValueFromElement(root, "swe262_name"));
        assertEquals("Should preserve age value", "30", getValueFromElement(root, "swe262_age"));
    }

    /**
     * Test the static withSuffix method
     */
    @Test
    public void testSuffixTransformer() throws Exception {
        StringReader reader = new StringReader(simpleXML);
        KeyTransformer transformer = KeyTransformer.withSuffix("_end");

        JSONObject result = XML.toJSONObject(reader, transformer);

        assertTrue("Should have suffixed root key", result.has("root_end"));
        JSONObject root = result.getJSONObject("root_end");

        assertTrue("Should have suffixed name key", root.has("name_end"));
        assertTrue("Should have suffixed age key", root.has("age_end"));

        assertEquals("Should preserve name value", "John", getValueFromElement(root, "name_end"));
        assertEquals("Should preserve age value", "30", getValueFromElement(root, "age_end"));
    }

    /**
     * Test the static toUpperCase method
     */
    @Test
    public void testUpperCaseTransformer() throws Exception {
        StringReader reader = new StringReader(simpleXML);
        KeyTransformer transformer = KeyTransformer.toUpperCase();

        JSONObject result = XML.toJSONObject(reader, transformer);

        assertTrue("Should have uppercase root key", result.has("ROOT"));
        JSONObject root = result.getJSONObject("ROOT");

        assertTrue("Should have uppercase name key", root.has("NAME"));
        assertTrue("Should have uppercase age key", root.has("AGE"));

        assertEquals("Should preserve name value", "John", getValueFromElement(root, "NAME"));
        assertEquals("Should preserve age value", "30", getValueFromElement(root, "AGE"));
    }

    /**
     * Test the static toLowerCase method
     */
    @Test
    public void testLowerCaseTransformer() throws Exception {
        String upperXML = "<ROOT><NAME>John</NAME><AGE>30</AGE></ROOT>";
        StringReader reader = new StringReader(upperXML);
        KeyTransformer transformer = KeyTransformer.toLowerCase();

        JSONObject result = XML.toJSONObject(reader, transformer);

        assertTrue("Should have lowercase root key", result.has("root"));
        JSONObject root = result.getJSONObject("root");

        assertTrue("Should have lowercase name key", root.has("name"));
        assertTrue("Should have lowercase age key", root.has("age"));

        assertEquals("Should preserve name value", "John", getValueFromElement(root, "name"));
        assertEquals("Should preserve age value", "30", getValueFromElement(root, "age"));
    }

    /**
     * Test the static reverse method
     */
    @Test
    public void testReverseTransformer() throws Exception {
        String testXML = "<foo><bar>test</bar></foo>";
        StringReader reader = new StringReader(testXML);
        KeyTransformer transformer = KeyTransformer.reverse();

        JSONObject result = XML.toJSONObject(reader, transformer);

        assertTrue("Should have reversed foo key (oof)", result.has("oof"));
        JSONObject foo = result.getJSONObject("oof");

        assertTrue("Should have reversed bar key (rab)", foo.has("rab"));
        assertEquals("Should preserve value", "test", getValueFromElement(foo, "rab"));
    }

    /**
     * Test chained transformers using andThen method
     */
    @Test
    public void testChainedTransformers() throws Exception {
        StringReader reader = new StringReader(simpleXML);
        KeyTransformer transformer = KeyTransformer.withPrefix("pre_")
                .andThen(KeyTransformer.toUpperCase());

        JSONObject result = XML.toJSONObject(reader, transformer);

        assertTrue("Should have chained transformation", result.has("PRE_ROOT"));
        JSONObject root = result.getJSONObject("PRE_ROOT");

        assertTrue("Should have chained name transformation", root.has("PRE_NAME"));
        assertTrue("Should have chained age transformation", root.has("PRE_AGE"));

        assertEquals("Should preserve name value", "John", getValueFromElement(root, "PRE_NAME"));
        assertEquals("Should preserve age value", "30", getValueFromElement(root, "PRE_AGE"));
    }

    /**
     * Test triple chained transformers
     */
    @Test
    public void testTripleChainedTransformers() throws Exception {
        StringReader reader = new StringReader("<test>value</test>");
        KeyTransformer transformer = KeyTransformer.withPrefix("a_")
                .andThen(KeyTransformer.withSuffix("_z"))
                .andThen(KeyTransformer.toUpperCase());

        JSONObject result = XML.toJSONObject(reader, transformer);

        assertTrue("Should have triple chained transformation", result.has("A_TEST_Z"));
        assertEquals("Should preserve value", "value", getValueFromElement(result, "A_TEST_Z"));
    }

    /**
     * Test attribute transformation
     */
    @Test
    public void testAttributeTransformation() throws Exception {
        StringReader reader = new StringReader(attributeXML);
        KeyTransformer transformer = key -> "attr_" + key;

        JSONObject result = XML.toJSONObject(reader, transformer);

        assertTrue("Should transform element key", result.has("attr_person"));
        JSONObject person = result.getJSONObject("attr_person");

        // Attributes are typically stored directly as key-value pairs
        assertTrue("Should transform id attribute", person.has("attr_id"));
        assertTrue("Should transform active attribute", person.has("attr_active"));

        // Use safe value extraction for attributes (could be String, Integer, Boolean, etc.)
        assertEquals("Should preserve id value", "123", getSafeStringValue(person, "attr_id"));
        assertEquals("Should preserve active value", "true", getSafeStringValue(person, "attr_active"));

        // Elements should also be transformed
        assertTrue("Should transform name element", person.has("attr_name"));
        assertTrue("Should transform email element", person.has("attr_email"));

        assertEquals("Should preserve name value", "Jane", getValueFromElement(person, "attr_name"));
        assertEquals("Should preserve email value", "jane@example.com", getValueFromElement(person, "attr_email"));
    }

    /**
     * Test complex XML transformation with book->volume replacement
     */
    @Test
    public void testComplexXMLTransformation() throws Exception {
        StringReader reader = new StringReader(complexXML);
        KeyTransformer transformer = key -> key.replace("book", "volume");

        JSONObject result = XML.toJSONObject(reader, transformer);

        // Should transform "books" to "volumes"
        assertTrue("Should transform books to volumes", result.has("volumes"));
        JSONObject volumes = result.getJSONObject("volumes");

        // Should transform "book" elements to "volume"
        assertTrue("Should have volume elements", volumes.has("volume"));

        // The structure could be either a single object or an array depending on implementation
        Object volumeData = volumes.get("volume");
        assertNotNull("Volume data should not be null", volumeData);
    }

    /**
     * Test nested XML transformation
     */
    @Test
    public void testNestedXMLTransformation() throws Exception {
        StringReader reader = new StringReader(nestedXML);
        KeyTransformer transformer = key -> "nested_" + key;

        JSONObject result = XML.toJSONObject(reader, transformer);

        assertTrue("Should transform company key", result.has("nested_company"));
        JSONObject company = result.getJSONObject("nested_company");

        assertTrue("Should transform department key", company.has("nested_department"));
        JSONObject department = company.getJSONObject("nested_department");

        // Should also transform the name attribute
        assertTrue("Should transform name attribute", department.has("nested_name"));
        assertEquals("Should preserve department name", "Engineering", department.getString("nested_name"));
    }

    /**
     * Test that KeyTransformer works vs normal parsing (verification test)
     */
    @Test
    public void testKeyTransformerVsNormalParsing() throws Exception {
        String xml = "<root><data>test</data></root>";

        // Parse without KeyTransformer
        JSONObject normal = XML.toJSONObject(xml);

        // Parse with KeyTransformer
        KeyTransformer transformer = key -> "TRANSFORMED_" + key;
        JSONObject transformed = XML.toJSONObject(new StringReader(xml), transformer);

        // Verify they are different
        assertFalse("Normal should not have transformed key", normal.has("TRANSFORMED_root"));
        assertTrue("Transformed should have transformed key", transformed.has("TRANSFORMED_root"));

        // Verify original structure still exists in normal version
        assertTrue("Normal should have original root key", normal.has("root"));
        assertFalse("Transformed should not have original root key", transformed.has("root"));
    }

    /**
     * Test null safety for andThen method
     */
    @Test(expected = NullPointerException.class)
    public void testAndThenNullSafety() {
        KeyTransformer transformer = KeyTransformer.toUpperCase();
        transformer.andThen(null); // Should throw NullPointerException
    }

    /**
     * Test empty string transformation
     */
    @Test
    public void testEmptyStringTransformation() throws Exception {
        String xml = "<root><empty></empty></root>";
        KeyTransformer transformer = key -> "prefix_" + key;

        JSONObject result = XML.toJSONObject(new StringReader(xml), transformer);

        assertTrue("Should transform root key", result.has("prefix_root"));
        JSONObject root = result.getJSONObject("prefix_root");
        assertTrue("Should transform empty key", root.has("prefix_empty"));
    }

    /**
     * Test performance implication by ensuring transformation happens during parsing
     * This test verifies that the KeyTransformer is called for each key encountered
     */
    @Test
    public void testTransformationDuringParsing() throws Exception {
        // Counter to verify transformer is called
        final int[] callCount = {0};

        KeyTransformer transformer = key -> {
            callCount[0]++;
            return "counted_" + key;
        };

        String xml = "<root><a>1</a><b>2</b><c>3</c></root>";
        XML.toJSONObject(new StringReader(xml), transformer);

        // Should be called for each key: root, a, b, c (minimum 4 times)
        assertTrue("Transformer should be called multiple times during parsing", callCount[0] >= 4);
    }

    /**
     * Helper method to safely extract value from XML element, handling different JSON structures
     * that may result from XML parsing (direct value, content wrapper, etc.)
     */
    private String getValueFromElement(JSONObject parent, String key) {
        if (!parent.has(key)) {
            return null;
        }

        Object value = parent.get(key);

        // If it's a direct string value
        if (value instanceof String) {
            return (String) value;
        }

        // If it's a number, convert to string
        if (value instanceof Number) {
            return value.toString();
        }

        // If it's a JSONObject, look for content or single value
        if (value instanceof JSONObject) {
            JSONObject valueObj = (JSONObject) value;

            // Look for content key (common in XML parsing)
            if (valueObj.has("content")) {
                return valueObj.get("content").toString();
            }

            // If there's only one key, it might be the value we want
            if (valueObj.length() == 1) {
                String firstKey = valueObj.keys().next();
                return valueObj.get(firstKey).toString();
            }

            // Otherwise return the string representation
            return valueObj.toString();
        }

        // Default: convert to string
        return value.toString();
    }

    /**
     * Helper method to safely get string value from JSONObject, handling type conversions
     */
    private String getSafeStringValue(JSONObject obj, String key) {
        if (!obj.has(key)) {
            return null;
        }

        Object value = obj.get(key);

        // Handle different types that might be returned from XML parsing
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Integer) {
            return value.toString();
        } else if (value instanceof Double) {
            return value.toString();
        } else if (value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof JSONObject) {
            JSONObject valueObj = (JSONObject) value;
            // Try to extract from content if it exists
            if (valueObj.has("content")) {
                return getSafeStringValue(valueObj, "content");
            }
            return valueObj.toString();
        }

        return value.toString();
    }
}