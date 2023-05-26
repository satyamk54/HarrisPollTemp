package ca.adintel.service.US;

import java.util.HashMap;
import java.util.Map;

public class MediaName {
    private static Map<String, String> channelMap;

    static {
        channelMap = new HashMap<>();
        channelMap.put("12", "Paid_Social");
        channelMap.put("C", "Cinema");
        channelMap.put("D", "Direct_Mail");
        channelMap.put("P", "Print");
        channelMap.put("X", "Outdoor");
        channelMap.put("S", "Organic_Social");
        channelMap.put("B", "Mobile");
        channelMap.put("T", "Television");
        channelMap.put("R", "Radio");
        channelMap.put("E", "Online_Video");
        channelMap.put("M", "Opt-In_Email");
        channelMap.put("O", "Online_Display");
    }
    public static String getMediaName(String code){
        return channelMap.get(code);
    }
}
