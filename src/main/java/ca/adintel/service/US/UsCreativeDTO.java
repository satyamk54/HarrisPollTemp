package ca.adintel.service.US;

import com.google.gson.annotations.SerializedName;

public class UsCreativeDTO {
    @SerializedName("brand-c.ad/productCode")
    private String productCode;

    @SerializedName("brand-c.ad/firstRunDate")
    private String firstRunDate;

    @SerializedName("brand-c.ad/categoryTier2Name")
    private String categoryTier2Name;

    @SerializedName("brand-c.ad/mediaCode")
    private String mediaCode;

    @SerializedName("brand-c.ad/languageCode")
    private String languageCode;

    @SerializedName("brand-c.ad/firstRunMediaOutletCode")
    private String firstRunMediaOutletCode;

    @SerializedName("brand-c.ad/advertiserCode")
    private String advertiserCode;

    @SerializedName("brand-c.ad/firstRunMediaOutletName")
    private String firstRunMediaOutletName;

    @SerializedName("brand-c.ad/title")
    private String title;

    @SerializedName("brand-c.ad/description")
    private String description;

    @SerializedName("brand-c.ad/newRecutName")
    private String newRecutName;

    @SerializedName("brand-c.ad/adId")
    private String adId;

    @SerializedName("brand-c.ad/categoryTier1Name")
    private String categoryTier1Name;

    @SerializedName("brand-c.ad/occurrences")
    private int occurrences;

    @SerializedName("brand-c.ad/languageName")
    private String languageName;

    @SerializedName("brand-c.ad/recutAdId")
    private String recutAdId;

    @SerializedName("brand-c.ad/firstRunDmaName")
    private String firstRunDmaName;

    @SerializedName("brand-c.ad/productName")
    private String productName;

    @SerializedName("brand-c.ad/headlineText")
    private String headlineText;

    @SerializedName("brand-c.ad/targetMarketName")
    private String targetMarketName;

    @SerializedName("brand-c.ad/advertiserName")
    private String advertiserName;

    public String getAdvertiserName() {
        return advertiserName;
    }

    public String getMediaCode() {
        return mediaCode;
    }

    public String getAdID() {
        return adId;
    }

    public String getFirstRunDate() {
        return firstRunDate;
    }
}
