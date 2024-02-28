package edu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static edu.CrptApi.createDocumentFromJson;

public class Main {
    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 10);

        String jsonContent;
        try {
            jsonContent = new String(Files.readAllBytes(Paths.get("src/main/resources/test.json")));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String signature = "example_signature";

        CrptApi.Document document = createDocumentFromJson(jsonContent);
        try {
            crptApi.createDocument(document, signature);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
