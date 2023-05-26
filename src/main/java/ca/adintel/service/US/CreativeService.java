package ca.adintel.service.US;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CreativeService {

    public UsCreativeDTO[] getCreatives(String start , String end,String report) {
        String startDate = start;
        String endDate = end;
        String schema = "brand-c.ad";
        String sortField = "firstRunDate";
        String sortOrder = "desc";

        String reportId;
        if (report.equals("General_Media")){
            reportId = "HARRIS-DB";
        } else {
            reportId = "HARRIS-DB-HM";
        }
        UsCreativeDTO[] result;

        String url = String.format("https://adintel.numerator.com/creative/api/v1/report/%s?schema=%s" +
                        "&sortField=%s&sortOrder=%s&startDate=%s&endDate=%s&state=Breaking",
                reportId, schema, sortField, sortOrder, startDate, endDate);

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        try {
            String token = getToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("X-CT-Token", token)
                    .header("Content-Type", "application/octet-stream")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            Gson gson = new Gson();
            String responseBody = response.body();
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            JsonArray itemsArray = jsonObject.getAsJsonArray("items");

            result = gson.fromJson(itemsArray, UsCreativeDTO[].class);
            return result;

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getToken() throws Exception {

        String userName = "harris.poll.creative@vivvix.com";
        String password = "Dthgnsrke5k04mbm0-gm3nt65";
        String authURL = "https://adintel.numerator.com/user/api/v1/authenticate";

        String token;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        String requestBody = "username=" + userName + "&password=" + password;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(authURL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();

            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(responseBody).getAsJsonObject();
            token = json.get("token").getAsString();
            return token;

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }
}
