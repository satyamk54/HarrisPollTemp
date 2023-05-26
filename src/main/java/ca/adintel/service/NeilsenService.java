package ca.adintel.service;

import ca.adintel.service.Neilsen.CreativeDTO;
import ca.adintel.service.Neilsen.NeilsenDTO;
import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class NeilsenService {


    public CreativeDTO[] getDataBacklog(int page) {
        CreativeDTO[] result = null;
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // form parameters
            String postData =
                    "{\"ApiKey\" : \"CE0B5AEF-6782-404A-A225-B66A603F715E\"," +
                            "\"Page\" : \""+page+"\","+
                            "\"DateRequired\" : \""+"2022-09-28"+"\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(postData))
                    .uri(URI.create("https://adintel.portfolioapi.intl.nielsen.com/api/creative"))
                    .setHeader("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            Gson gson = new Gson();
            result = gson.fromJson(response.body(), CreativeDTO[].class);

            return result;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * curl --request POST 'https://adintel.portfolioapi.intl.nielsen.com/api/creative' \
     * --header 'Content-Type: application/json' \
     * --data-raw '{
     * "ApiKey" : "CE0B5AEF-6782-404A-A225-B66A603F715E",
     * "DateRequired" : "2022-10-27"
     * }'
     *
     * @param date
     * @return
     */

    public CreativeDTO[] getDateFromDate(String date) {
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // form parameters
            String postData = "{\"ApiKey\" : \"CE0B5AEF-6782-404A-A225-B66A603F715E\", \"DateRequired\" : \""+date+"”}";

//            Map<Object, Object> data = new HashMap<>();
//            data.put("ApiKey", "CE0B5AEF-6782-404A-A225-B66A603F715E");
//            data.put("DateRequired", date);

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(postData))
                    .uri(URI.create("https://adintel.portfolioapi.intl.nielsen.com/api/creative"))
                    .setHeader("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            Gson gson = new Gson();
            CreativeDTO[] result = gson.fromJson(response.body(), CreativeDTO[].class);

            return result;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean downloadFile(String fileURL, String fileName) {

        try {
            URL url = new URL(fileURL);
            try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                 FileChannel fileChannel = fileOutputStream.getChannel()) {

                fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                fileOutputStream.close();
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }
}
