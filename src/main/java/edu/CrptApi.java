package edu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

public class CrptApi {
    @Getter
    private final RateLimiter rateLimiter;

    @Setter
    private HttpClient httpClient;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.rateLimiter = new RateLimiter(timeUnit, requestLimit);
    }

    public void createDocument(Document document, String signature) throws InterruptedException {
        rateLimiter.acquire();
        JSONObject requestBody = createRequestBody(document, signature);
        HttpClient client = httpClient != null ? httpClient : HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response code: " + response.statusCode());
            System.out.println("Response body: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject createRequestBody(Document document, String signature) {
        JSONObject description = new JSONObject();
        description.put("participantInn", document.participantInn);

        JSONObject product = new JSONObject();
        product.put("certificate_document", document.certificateDocument);
        product.put("certificate_document_date", document.certificateDocumentDate);
        product.put("certificate_document_number", document.certificateDocumentNumber);
        product.put("owner_inn", document.ownerInn);
        product.put("producer_inn", document.producerInn);
        product.put("production_date", document.productionDate);
        product.put("tnved_code", document.tnvedCode);
        product.put("uit_code", document.uitCode);
        product.put("uitu_code", document.uituCode);

        JSONArray products = new JSONArray();
        products.put(product);

        JSONObject requestBody = new JSONObject();
        requestBody.put("description", description);
        requestBody.put("doc_id", document.docId);
        requestBody.put("doc_status", document.docStatus);
        requestBody.put("doc_type", document.docType);
        requestBody.put("importRequest", document.importRequest);
        requestBody.put("owner_inn", document.ownerInn);
        requestBody.put("participant_inn", document.participantInn);
        requestBody.put("producer_inn", document.producerInn);
        requestBody.put("production_date", document.productionDate);
        requestBody.put("production_type", document.productionType);
        requestBody.put("products", products);
        requestBody.put("reg_date", document.regDate);
        requestBody.put("reg_number", document.regNumber);
        requestBody.put("signature", signature);

        return requestBody;
    }

    public static class RateLimiter {
        private final TimeUnit timeUnit;
        private final int requestLimit;
        private int count;
        private long lastRequestTime;

        public RateLimiter(TimeUnit timeUnit, int requestLimit) {
            this.timeUnit = timeUnit;
            this.requestLimit = requestLimit;
            this.count = 0;
            this.lastRequestTime = System.currentTimeMillis();
        }

        synchronized public void acquire() throws InterruptedException {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastRequestTime;

            // Reset count and last request time if elapsed time exceeds interval
            if (elapsedTime >= timeUnit.toMillis(1)) {
                count = 0;
                lastRequestTime = currentTime;
            }

            // If count exceeds the limit, sleep until the next interval
            if (count >= requestLimit) {
                throw new InterruptedException("Request limit exceeded");
            }

            // Increment the count for the current interval
            count++;
        }

        // Wrapper method for TimeUnit.MILLISECONDS.sleep() for better testability
        public void sleep(long millis) throws InterruptedException {
            timeUnit.sleep(millis);
        }
    }

    public static CrptApi.Document createDocumentFromJson(String jsonContent) {
        org.json.JSONObject jsonObject = new org.json.JSONObject(jsonContent);

        String docId = jsonObject.getString("doc_id");
        String docStatus = jsonObject.getString("doc_status");
        String docType = jsonObject.getString("doc_type");
        boolean importRequest = jsonObject.getBoolean("importRequest");
        String ownerInn = jsonObject.getString("owner_inn");
        String participantInn = jsonObject.getString("participant_inn");
        String producerInn = jsonObject.getString("producer_inn");
        String productionDate = jsonObject.getString("production_date");
        String productionType = jsonObject.getString("production_type");
        String regDate = jsonObject.getString("reg_date");
        String regNumber = jsonObject.getString("reg_number");
        String certificateDocument = jsonObject.getJSONArray("products").getJSONObject(0).getString("certificate_document");
        String certificateDocumentDate = jsonObject.getJSONArray("products").getJSONObject(0).getString("certificate_document_date");
        String certificateDocumentNumber = jsonObject.getJSONArray("products").getJSONObject(0).getString("certificate_document_number");
        String tnvedCode = jsonObject.getJSONArray("products").getJSONObject(0).getString("tnved_code");
        String uitCode = jsonObject.getJSONArray("products").getJSONObject(0).getString("uit_code");
        String uituCode = jsonObject.getJSONArray("products").getJSONObject(0).getString("uitu_code");

        return new CrptApi.Document(docId, docStatus, docType, importRequest, ownerInn, participantInn, producerInn, productionDate,
                productionType, regDate, regNumber, certificateDocument, certificateDocumentDate, certificateDocumentNumber,
                tnvedCode, uitCode, uituCode);
    }

    public record Document(String docId, String docStatus, String docType, boolean importRequest, String ownerInn,
                           String participantInn, String producerInn, String productionDate, String productionType,
                           String regDate, String regNumber, String certificateDocument, String certificateDocumentDate,
                           String certificateDocumentNumber, String tnvedCode, String uitCode, String uituCode) {
    }
}
