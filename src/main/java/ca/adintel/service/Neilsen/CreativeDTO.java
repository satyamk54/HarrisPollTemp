package ca.adintel.service.Neilsen;

import java.util.Comparator;

public class CreativeDTO implements Comparator<CreativeDTO>, Comparable<CreativeDTO> {
    public String CreativeID;
    public String Media;
    public String AdType;
    public String [] Category;
    public String Brand;
    public String Product;
    public String Country;
    public String Title;
    public String Description;
    public String TransmissionDate;
    public String InitialChannel;
    public String Length;
    public String Version;
    public String LastRunDate;
    public String Advertiser;
    public URLS[] Urls;

    public int year;

    public String sourceURL;
    public String targetFileName;

    public String[] toCSV(){
        String [] result = new String[15];
        result[0] = CreativeID;
        result[1] = Media;
        result[2] = AdType;
        result[3] = String.join(",", Category);
        result[4] = Brand;
        result[5] = Product;
        result[6] = Country;
        result[7] = Title;
        result[8] = Description;
        result[9] = TransmissionDate.substring(0,TransmissionDate.indexOf("T"));
        result[10] = InitialChannel;
        result[11] = Length;
        result[12] = Version;
        result[13] = LastRunDate;
        result[14] = Advertiser;
        return result;
    }

    @Override
    public int compare(final CreativeDTO o1, final CreativeDTO o2) {
        int res =  o1.TransmissionDate.compareTo(o2.TransmissionDate);
        if (res==0) return o1.CreativeID.compareTo(o2.CreativeID);
        else return res;
    }

    @Override
    public int compareTo(final CreativeDTO o) {
        return compare(this,o);
    }

    public class URLS {
        public String Type;
        public String Url;
        public String Filename;
        public String MimeType;
    }

    //Creative ID	Advertiser	Brand	Sub Brand	Category	Channel/Publication	First Recorded Date	Last Recorded Date	Length/File(s)	Media	Ad Type	Country	Title	Description	Endline
}



/*

"Length": 30,
        "Version": "0",
        "LastRunDate": "2022-10-20T00:00:00",
        "Advertiser": "Amazon EU"

{
        "CreativeID": 526589914,
        "Media": "Internet",
        "AdType": "Video",
        "Category": [
            "ENTERTAINMENT",
            "Broadcasting",
            "Streaming - Music"
        ],
        "Brand": "Amazon",
        "Product": "Music online streaming service",
        "Country": "UK",
        "Title": "https://youtube.com/playlist",
        "Description": "AMAZON (UK) LTD; AMAZON MUSIC - ONE TAKE PLAYLIST; ENTERTAINMENT & LEISURE; HOME ENTERTAINMENT; MP3/MUSIC DOWNLOAD/MUSIC STREAMING",
        "TransmissionDate": "2022-10-14T00:00:00",
        "InitialChannel": "youtube.com",
        "Urls": [
            {
                "Type": "Creative",
                "Url": "https://adintel.portfolio.intl-media.nielsen.com/results/getcreativemedia?encryptedId=gf33EZyyTxxDuVf9Es5s/oPzgfXKUq3X",
                "Filename": "526589914_1.mp4",
                "MimeType": "video/mpeg"
            },
            {
                "Type": "Thumbnail",
                "Url": "https://adintel.portfolio.intl-media.nielsen.com/previews/526589000/526589914_1.jpg",
                "Filename": "526589914_1.jpg",
                "MimeType": "image/jpeg"
            }
        ],
        "Length": 30,
        "Version": "0",
        "LastRunDate": "2022-10-20T00:00:00",
        "Advertiser": "Amazon EU"
    }
 */
