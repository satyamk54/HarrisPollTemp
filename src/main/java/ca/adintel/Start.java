package ca.adintel;

import com.google.gson.Gson;
import org.apache.poi.util.ArrayUtil;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Start {

    static boolean runUSCurrent = false;
    static boolean runINTLcurrent = false;
    static boolean runINTLbacklog = false;
    static boolean runUSbacklog = false;
    static boolean simulateWritingCreatives = false;
    static String startDate = null;
    static String endDate = null;


    public static void main(String... args) throws IOException, ParseException {

        Gson gson = new Gson();

        int year = 2022;
        Integer [] months = {1};
        Config config = null;

        Date dateFrom = null;
        Date dateTo = null;

        try {
            config = gson.fromJson(new FileReader("config.json"), Config.class);
            runUSbacklog = config.runUSBacklog;
            runINTLbacklog = config.runINTLBacklog;
            runINTLcurrent = config.runINTLCurrent;
            runUSCurrent = config.runUSCurrent;
            simulateWritingCreatives = config.simulateWriting;
            startDate = config.dateFrom;
            endDate = config.dateTo;
            year = config.year;
            months = config.months;

            SimpleDateFormat formatterDate=new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            try {
                dateFrom = formatterDate.parse(config.dateFrom);
                dateTo = formatterDate.parse(config.dateTo);
            } catch (Exception ex){
                ex.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        if (runUSCurrent) {
            System.out.println("Running US current");
            Runner runner = new Runner();
            List<String> reports = new ArrayList<>();
            reports.add("General_Media");
            reports.add("Hispanic_Media");
            for (String s: reports){
                runner.downloadAndUpload(simulateWritingCreatives,"20230424","20230424",s);
            }
            System.exit(0);
        }

        if (runINTLbacklog) {
            System.out.println("Running INTL backlog");
            Runner runner = new Runner();
            runner.downloadAndUploadNeilsenBacklog(simulateWritingCreatives,true,dateFrom.getYear()+1900,dateFrom,dateTo);
            System.exit(0);
        }

        if (runUSbacklog) {
            System.out.println("Running US backlog");
            Runner runner = new Runner();
            runner.executeManual(simulateWritingCreatives,config.continueFrom);
            System.exit(0);
        }

        if (runINTLcurrent) {
            System.out.println("Running INTL current");

            try {
                Runner runner;
                String s = startDate;
                String lastDate = endDate;

                Calendar cal = Calendar.getInstance();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                Date date = null;
                date = formatter.parse(s);

                cal.setTime(date);

                do {
                    runner = new Runner();
                    runner.downloadAndUploadNeilsen(formatter.format(cal.getTime()),simulateWritingCreatives);
                    cal.add(Calendar.DATE, 7);
                } while (cal.getTime().before(formatter.parse(lastDate)));

                runner.runMasterList();

            } catch (ParseException e) {
                e.printStackTrace();
            }

            System.exit(0);

        }

    }


}
