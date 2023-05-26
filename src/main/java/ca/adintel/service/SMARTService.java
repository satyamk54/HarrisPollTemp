package ca.adintel.service;

import ca.adintel.dto.AuthenticateJson;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public class SMARTService {

    public String downloadFileByRequestId(String requestId, String token, String destDir){

        String result = null;
        try {
//            HttpClient httpClient = HttpClient.newBuilder()
//                    .version(HttpClient.Version.HTTP_2)
//                    .connectTimeout(Duration.ofSeconds(100))
//                    .build();


            HttpClient httpClient =
                    HttpClient.newBuilder()
                            .followRedirects(HttpClient.Redirect.NORMAL) // follow redirects
                            .build();

            // form parameters
            Map<Object, Object> data = new HashMap<>();
            data.put("cttoken", token);
            //curl -L 'https://adintel.numerator.com/export/api/v1/download/requestid?cttoken=token'

            //HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();

//            CloseableHttpClient instance =
//                    HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();


            String url = "https://adintel.numerator.com/export/api/v1/download/";
            url+=requestId+"?cttoken="+token;

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
//                    .setHeader("User-Agent", "Harris Poll data access") // add request header
//                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

//            Path downloadDir = Path.of(System.getProperty("user.home"), "Downloads");
            Path downloadDir = Path.of("dwn/"+destDir+"/").toAbsolutePath();
            downloadDir.toFile().mkdirs();
//            downloadDir.toFile().

            HttpResponse r = httpClient.send(request, HttpResponse.BodyHandlers.ofFileDownload(downloadDir, CREATE, WRITE));

            if (r.statusCode()!=200) {
                //throw error
            }
            return r.body().toString();
//            System.out.println(r.body());


            // print status code
//            System.out.println(response.statusCode());

            // print response body
//            System.out.println(response.body());

//            FileWriter fw = new FileWriter("1.xlsx");
//            fw.write(response.body());
//            fw.close();
//            Gson gson = new Gson();
//            AuthenticateJson auth = gson.fromJson(response.body(),AuthenticateJson.class);
//
//            return auth.token;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public String login(){
        //curl -X "POST" "https://adintel.numerator.com/user/api/v1/authenticate" --data-urlencode "username=harris.poll@numerator.com" --data-urlencode "password=CDntkm4hbk44vv34h"

        String result = null;
        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // form parameters
            Map<Object, Object> data = new HashMap<>();
            data.put("username", "harris.poll@numerator.com");
            data.put("password", "CDntkm4hbk44vv34h");

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(ofFormData(data))
                    .uri(URI.create("https://adintel.numerator.com/user/api/v1/authenticate"))
                    .setHeader("User-Agent", "Harris Poll data access") // add request header
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            Gson gson = new Gson();
            AuthenticateJson auth = gson.fromJson(response.body(),AuthenticateJson.class);

            return auth.token;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return result;
    }


    public boolean getCreativeAsset(String token, String adId, String reportId, String fileName) {
        /*
        https://adintel.numerator.com/media/api/v1/details/brand-c.ad?reportId=BRAND-C-ALL-CHARTS&domainId=SLFCCO-11455&onlyThumbnails=false&includeLandingPageCreative=false
         */
        String url = "https://adintel.numerator.com/media/api/v1/details/brand-c.ad?reportId="+reportId+"&domainId="+adId+"&onlyThumbnails=false&includeLandingPageCreative=false";
                //"https://adintel.numerator.com/creative/api/v1/download/"+adId+"?schema=brand-c.ad&reportId="+reportId;
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .setHeader("x-ct-token", token)
                .uri(URI.create(url))
                .setHeader("User-Agent", "Harris Poll data access") // add request header
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Path path = Paths.get(fileName);

        int count=50;
        int c = 0;
        do {
            try {
                HttpResponse<String> response =  httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.body().startsWith("{\"succeeded\":false")) {
                    System.out.println(response.body());
                    return false;
                }

                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                c++;
            }
        } while (c<count);

        return false;
    }

    public ColumnarItem getColumnarInfo(String token, String adId, String reportId) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String url = "https://adintel.numerator.com/reporting/api/v1/columnar?reportId="+reportId+"&domainId="+adId+
                "&schemaId=brand-c.ad&startDate=20140101&endDate=30221014";

        /*
        "https://adintel.numerator.com/creative/api/v1/download/LEXUAU-26544?schema=brand-c.ad&reportId=BMAT-API-DB"

        "https://adintel.numerator.com/creative/api/v1/adcode/ADCODE_YOU_WANT?schema=YOUR_SCH
EMA&reportId=YOUR_REPORT" -H 'X-CT-Token:YOUR_TOKEN'
         */

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .setHeader("x-ct-token", token)
                .uri(URI.create(url))
                .setHeader("User-Agent", "Harris Poll data access") // add request header
                .build();

        HttpResponse<String> response = null;

        int count=50;
        int c = 0;
        do {
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Gson gson = new Gson();
                ColumnarResponse columnar = gson.fromJson(response.body(),ColumnarResponse.class);

                if (columnar.items==null||columnar.items.length==0) {
                    System.out.println("AdId:"+adId + " resulting in 0 results");
                    response = null;
                    c++;
                    continue;
                }

                if (columnar.items!=null)
                    return columnar.items[0];


            } catch (Exception ex) {
                response = null;
                ex.printStackTrace();
                c++;
            }
        } while (response==null&&c<count);

        return null;
    }

    // Sample: 'password=123&custom=secret&username=abc&ts=1570704369823'
    public HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    /**
     * {
     *   "items": [
     *     {
     *       "brand-c.ad/adId": "VERITL-88522",
     *       "brand-c.ad/headlineText": "THE NEW GOOGLE PIXEL 7 PRO IS",
     *       "brand-c.ad/firstRunUnitValue": "30",
     *     }
     *   ]
     * }
     */
    public class ColumnarResponse{
        ColumnarItem[] items;
    }

    public class ColumnarItem{
        @SerializedName("brand-c.ad/adId")
        public String adId;
        @SerializedName("brand-c.ad/headlineText")
        public String headlineText;
        @SerializedName("brand-c.ad/firstRunUnitValue")
        public String firstRunUnitValue;
    }
}
