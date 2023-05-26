package ca.adintel.service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SolrService {
    //TVICCO-0986
    final String SOLR_PREFIX = "http://10.100.88.150:38201/solr/brand-c_v2_1_ad/select?fl=media_codes&q=ad_id%3A%22";
    final String SOLR_POSTFIX = "%22&wt=csv";

    public String getMediaCode(String adId){
        try {
            URL url = new URL(SOLR_PREFIX+adId+SOLR_POSTFIX);

            // Open a connection(?) on the URL(??) and cast the response(???)
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Now it's "open", we can set the request method, headers etc.
//            connection.setRequestProperty("accept", "application/json");

            // This line makes the request
            InputStream responseStream = connection.getInputStream();

            byte[] response = responseStream.readAllBytes();
            String s = new String(response);
            String []lines = s.split("\n");
            return lines[1];

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;


    }
}
