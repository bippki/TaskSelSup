package test;



import edu.CrptApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Time;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static edu.CrptApi.createDocumentFromJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


public class CrptApiTest {
    private CrptApi crptApi;
    private HttpClient httpClientMock;
    private HttpResponse httpResponseMock;
    private final TimeUnit timeUnit  = TimeUnit.SECONDS;
    private final int requestLimit = 5;
    private String jsonContent;
    private String signature = "signature";

    @BeforeEach
    void setUp() {
        crptApi = new CrptApi(timeUnit, requestLimit);
        httpClientMock = mock(HttpClient.class);
        httpResponseMock = mock(HttpResponse.class);
        crptApi.setHttpClient(httpClientMock);

        try {
            jsonContent = new String(Files.readAllBytes(Paths.get("src/main/resources/test.json")));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Test
    void testCreateDocument() throws Exception {
        CrptApi.Document document = createDocumentFromJson(jsonContent);


        when(httpClientMock.send(Mockito.any(), Mockito.any())).thenReturn(httpResponseMock);
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn("Mocked response body");

        crptApi.createDocument(document, signature);
        verify(httpClientMock).send(Mockito.any(), Mockito.any());
        assertEquals(200, httpResponseMock.statusCode());
        assertEquals("Mocked response body", httpResponseMock.body());
    }

    @Test
    void testRateLimiting() throws Exception {
        CrptApi.Document document = createDocumentFromJson(jsonContent);

        when(httpClientMock.send(Mockito.any(), Mockito.any())).thenReturn(httpResponseMock);
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpResponseMock.body()).thenReturn("Mocked response body");

        for (int i = 0; i < 10; i++) {
            if (i >= 5) {
                assertThrows(InterruptedException.class, () -> crptApi.createDocument(document, signature));
            } else {
                crptApi.createDocument(document, signature);
            }
        }
        Mockito.verify(httpClientMock, times(requestLimit)).send(Mockito.any(HttpRequest.class), Mockito.any(HttpResponse.BodyHandler.class));
    }
}
