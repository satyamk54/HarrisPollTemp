package ca.adintel.service;

import ca.adintel.service.Neilsen.CreativeDTO;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVService {

    /*
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
     */
    public static void writeCSV(CreativeDTO[] dtos, String filePath) {
        File file = new File(filePath);
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = { "CreativeID", "Media", "AdType", "Category",
                    "Brand","Product","Country","Title","Description","TransmissionDate",
                    "InitialChannel","Length","Version","LastRunDate","Advertiser" };

            writer.writeNext(header);
            for(CreativeDTO dto:dtos){
                String [] data = dto.toCSV();
                writer.writeNext(data);
            }
            // closing writer connection
            writer.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
