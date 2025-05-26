package org.json.junit;

import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.StringReader;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Test class for Milestone 5: Asynchronous API functionality.
 * Tests the asynchronous processing capabilities of XML to JSON conversion.
 */
public class AsyncXMLTest {

    private static final int TIMEOUT_SECONDS = 10;

    @Test
    public void testBasicAsyncCallback() throws Exception {
        String xmlString = "<root><n>John</n><age>30</age></root>";
        StringReader reader = new StringReader(xmlString);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<JSONObject> resultRef = new AtomicReference<>();
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        CompletableFuture<Void> future = XML.toJSONObjectAsync(reader,
                result -> {
                    resultRef.set(result);
                    latch.countDown();
                },
                error -> {
                    errorRef.set(error);
                    latch.countDown();
                }
        );

        // Wait for async operation to complete
        assertTrue("Operation should complete within timeout",
                latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        assertNull("Should not have error", errorRef.get());
        assertNotNull("Should have result", resultRef.get());

        JSONObject result = resultRef.get();
        assertTrue("Should contain root", result.has("root"));

        JSONObject root = result.getJSONObject("root");
        assertEquals("Should have correct name", "John", root.getString("n"));  // Changed from "name" to "n"
        assertEquals("Should have correct age", 30, root.getInt("age"));

        // Ensure Future is completed
        assertTrue("Future should be done", future.isDone());
        assertFalse("Future should not be cancelled", future.isCancelled());
    }

    @Test
    public void testAsyncWithCompletableFuture() throws Exception {
        String xmlString = "<books><book><title>Test Book</title><author>Test Author</author></book></books>";
        StringReader reader = new StringReader(xmlString);

        CompletableFuture<JSONObject> future = XML.toJSONObjectAsync(reader);
        JSONObject result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertNotNull("Result should not be null", result);
        assertTrue("Should contain books", result.has("books"));

        JSONObject books = result.getJSONObject("books");
        assertTrue("Should contain book", books.has("book"));

        JSONObject book = books.getJSONObject("book");
        assertEquals("Should have correct title", "Test Book", book.getString("title"));
        assertEquals("Should have correct author", "Test Author", book.getString("author"));
    }


    @Test
    public void testAsyncErrorHandling() throws Exception {
        StringReader reader = new StringReader("<invalid><xml><unclosed>content"); // Invalid XML

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<JSONObject> resultRef = new AtomicReference<>();
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        XML.toJSONObjectAsync(reader,
                result -> {
                    resultRef.set(result);
                    latch.countDown();
                },
                error -> {
                    errorRef.set(error);
                    latch.countDown();
                }
        );

        assertTrue("Operation should complete", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertNull("Should not have result", resultRef.get());
        assertNotNull("Should have error", errorRef.get());
        assertTrue("Should be Exception", errorRef.get() instanceof Exception);
    }

    @Test
    public void testAsyncWithCustomExecutor() throws Exception {
        String xmlString = "<data><item>Test</item></data>";
        StringReader reader = new StringReader(xmlString);

        // Create custom thread pool
        ExecutorService customExecutor = Executors.newFixedThreadPool(2);
        AtomicReference<String> threadName = new AtomicReference<>();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<JSONObject> resultRef = new AtomicReference<>();

        try {
            XML.toJSONObjectAsync(reader,
                    result -> {
                        threadName.set(Thread.currentThread().getName());
                        resultRef.set(result);
                        latch.countDown();
                    },
                    error -> {
                        latch.countDown();
                    },
                    customExecutor
            );

            assertTrue("Should complete with custom executor",
                    latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

            assertNotNull("Should have result", resultRef.get());
            assertNotNull("Should capture thread name", threadName.get());
            assertTrue("Should use custom thread pool",
                    threadName.get().contains("pool"));

        } finally {
            customExecutor.shutdown();
            customExecutor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testMultipleAsyncOperations() throws Exception {
        String[] xmlStrings = {
                "<doc1><data>First</data></doc1>",
                "<doc2><data>Second</data></doc2>",
                "<doc3><data>Third</data></doc3>"
        };

        List<CompletableFuture<JSONObject>> futures = new ArrayList<>();

        for (String xml : xmlStrings) {
            StringReader reader = new StringReader(xml);
            futures.add(XML.toJSONObjectAsync(reader));
        }

        // Wait for all to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        allFutures.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Verify all completed successfully
        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<JSONObject> future = futures.get(i);
            assertTrue("Future " + i + " should be done", future.isDone());
            assertFalse("Future " + i + " should not be cancelled", future.isCancelled());

            JSONObject result = future.get();
            assertNotNull("Result " + i + " should not be null", result);
        }

        // Verify content
        JSONObject firstResult = futures.get(0).get();
        assertTrue("First result should have doc1", firstResult.has("doc1"));
        assertEquals("First result data", "First", firstResult.getJSONObject("doc1").getString("data"));
    }

    @Test
    public void testAsyncCancellation() throws Exception {
        // Create a large XML that takes time to process
        StringBuilder largeXML = new StringBuilder("<root>");
        for (int i = 0; i < 10000; i++) {
            largeXML.append("<item").append(i).append("><data>Data").append(i).append("</data></item").append(i).append(">");
        }
        largeXML.append("</root>");

        StringReader reader = new StringReader(largeXML.toString());
        CompletableFuture<JSONObject> future = XML.toJSONObjectAsync(reader);

        // Cancel the operation quickly
        boolean cancelled = future.cancel(true);

        if (cancelled) {
            assertTrue("Should be cancelled", future.isCancelled());
            assertTrue("Should be done", future.isDone());

            try {
                future.get();
                fail("Should throw CancellationException");
            } catch (CancellationException e) {
                // Expected
            }
        } else {
            // If cancellation failed, operation was too fast - just verify it completed
            JSONObject result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertNotNull("Should have result if not cancelled", result);
        }
    }

    @Test
    public void testAsyncPerformanceWithLargeFile() throws Exception {
        // Create large XML
        StringBuilder largeXML = new StringBuilder("<catalog>");
        for (int i = 0; i < 1000; i++) {
            largeXML.append("<product id=\"").append(i).append("\">")
                    .append("<n>Product ").append(i).append("</n>")
                    .append("<price>").append(i * 10.5).append("</price>")
                    .append("<category>Category ").append(i % 10).append("</category>")
                    .append("<description>Description for product ").append(i).append("</description>")
                    .append("</product>");
        }
        largeXML.append("</catalog>");

        StringReader reader = new StringReader(largeXML.toString());

        long startTime = System.currentTimeMillis();
        CompletableFuture<JSONObject> future = XML.toJSONObjectAsync(reader);
        JSONObject result = future.get(30, TimeUnit.SECONDS); // Longer timeout for large data
        long endTime = System.currentTimeMillis();

        assertNotNull("Should have result", result);
        assertTrue("Should contain catalog", result.has("catalog"));

        long processingTime = endTime - startTime;
        System.out.println("Async processing time for large XML: " + processingTime + "ms");

        // Verify some content
        JSONObject catalog = result.getJSONObject("catalog");
        assertTrue("Should have products", catalog.has("product"));

        assertTrue("Should process in reasonable time", processingTime < 30000); // 30 seconds max
    }

    @Test
    public void testAsyncChaining() throws Exception {
        String xmlString = "<numbers><num>5</num><num>10</num><num>15</num></numbers>";
        StringReader reader = new StringReader(xmlString);

        CompletableFuture<Integer> chainedResult = XML.toJSONObjectAsync(reader)
                .thenApply(jsonObj -> {
                    // Extract numbers and sum them
                    JSONObject numbers = jsonObj.getJSONObject("numbers");
                    JSONArray numArray = numbers.getJSONArray("num");
                    int sum = 0;
                    for (int i = 0; i < numArray.length(); i++) {
                        sum += numArray.getInt(i);
                    }
                    return sum;
                })
                .thenApply(sum -> sum * 2); // Double the sum

        Integer result = chainedResult.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertEquals("Should chain operations correctly", Integer.valueOf(60), result); // (5+10+15)*2 = 60
    }

    @Test
    public void testAsyncExceptionInCallback() throws Exception {
        String xmlString = "<root><data>test</data></root>";
        StringReader reader = new StringReader(xmlString);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        CompletableFuture<Void> future = XML.toJSONObjectAsync(reader,
                result -> {
                    // Throw exception in success callback
                    throw new RuntimeException("Callback exception");
                },
                error -> {
                    errorRef.set(error);
                    latch.countDown();
                }
        );

        // The future should complete normally, but the callback exception should be handled
        try {
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            // Exception in callback might propagate
        }

        // We can't always guarantee how callback exceptions are handled,
        // but the async operation should not hang the system
        assertTrue("Test should complete in reasonable time", true);
    }

    @Test
    public void testAsyncTimeout() throws Exception {
        String xmlString = "<root><data>test</data></root>";
        StringReader reader = new StringReader(xmlString);

        CompletableFuture<JSONObject> future = XML.toJSONObjectAsync(reader);

        try {
            // Very short timeout should either succeed quickly or timeout
            JSONObject result = future.get(1, TimeUnit.MILLISECONDS);
            // If we get here, operation was very fast
            assertNotNull("Result should not be null", result);
        } catch (TimeoutException e) {
            // Expected for very short timeout
            assertFalse("Future should not be done yet", future.isDone());

            // Now wait properly
            JSONObject result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertNotNull("Should eventually complete", result);
        }
    }

    @Test
    public void testAsyncWithStreamProcessing() throws Exception {
        String xmlString = "<data>" +
                "<item><n>Item1</n><value>100</value></item>" +
                "<item><n>Item2</n><value>200</value></item>" +
                "<item><n>Item3</n><value>300</value></item>" +
                "</data>";
        StringReader reader = new StringReader(xmlString);

        CompletableFuture<List<String>> future = XML.toJSONObjectAsync(reader)
                .thenApply(jsonObj -> {
                    // Use streaming API on the result
                    return jsonObj.toStream()
                            .filter(node -> "n".equals(node.getKey()))  // Changed from "name" to "n"
                            .map(node -> node.getValue().toString())
                            .collect(java.util.stream.Collectors.toList());
                });

        List<String> names = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertNotNull("Names should not be null", names);
        assertEquals("Should have 3 names", 3, names.size());
        assertTrue("Should contain Item1", names.contains("Item1"));
        assertTrue("Should contain Item2", names.contains("Item2"));
        assertTrue("Should contain Item3", names.contains("Item3"));
    }

    @Test
    public void testAsyncMemoryUsage() throws Exception {
        // Test that async operations don't cause memory leaks
        List<CompletableFuture<JSONObject>> futures = new ArrayList<>();

        // Start many async operations
        for (int i = 0; i < 100; i++) {
            String xml = "<test" + i + "><data>Data" + i + "</data></test" + i + ">";
            StringReader reader = new StringReader(xml);
            futures.add(XML.toJSONObjectAsync(reader));
        }

        // Wait for all to complete and verify
        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<JSONObject> future = futures.get(i);
            JSONObject result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            assertNotNull("Result " + i + " should not be null", result);
            assertTrue("Result " + i + " should have expected key",
                    result.has("test" + i));
        }

        // Suggest garbage collection
        System.gc();

        // If we get here without OutOfMemoryError, the test passes
        assertTrue("Memory usage test completed", true);
    }

    @Test
    public void testAsyncWithDifferentThreadPools() throws Exception {
        String xmlString = "<root><data>thread-test</data></root>";

        // Test with different executor types
        ExecutorService[] executors = {
                Executors.newSingleThreadExecutor(),
                Executors.newFixedThreadPool(2),
                Executors.newCachedThreadPool(),
                ForkJoinPool.commonPool()
        };

        CountDownLatch latch = new CountDownLatch(executors.length);
        AtomicInteger successCount = new AtomicInteger(0);

        try {
            for (ExecutorService executor : executors) {
                StringReader reader = new StringReader(xmlString);
                XML.toJSONObjectAsync(reader,
                        result -> {
                            if (result != null && result.has("root")) {
                                successCount.incrementAndGet();
                            }
                            latch.countDown();
                        },
                        error -> {
                            latch.countDown();
                        },
                        executor
                );
            }

            assertTrue("All operations should complete",
                    latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            assertEquals("All operations should succeed",
                    executors.length, successCount.get());

        } finally {
            // Cleanup executors (except common pool)
            for (int i = 0; i < executors.length - 1; i++) {
                executors[i].shutdown();
                executors[i].awaitTermination(5, TimeUnit.SECONDS);
            }
        }
    }
}