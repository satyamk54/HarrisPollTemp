package ca.adintel;

import ca.adintel.service.CSVService;
import ca.adintel.service.EmailProcessorService;
import ca.adintel.service.MediaServerDirect;
import ca.adintel.service.Neilsen.CreativeDTO;
import ca.adintel.service.Neilsen.CreativeDTO.URLS;
import ca.adintel.service.NeilsenService;
import ca.adintel.service.SFTPService;
import ca.adintel.service.SMARTService;
import ca.adintel.service.SMARTService.ColumnarItem;
import ca.adintel.service.SolrService;
import ca.adintel.service.US.CreativeService;
import ca.adintel.service.US.MediaName;
import ca.adintel.service.US.UsCreativeDTO;
import ca.adintel.service.XLSXService;
import ca.adintel.service.ZipService;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/** @noinspection ALL*/
public class Runner {
    SourceForwarder[] forwarders = new SourceForwarder[20];
//
//    public Pair<String> TV_General = new Pair<>();
//
//    public Pair<String> TV_Hispanic = new Pair<>();
//    public Pair<String> Organic_Social_General = new Pair<>();
//    public Pair<String> Organic_Social_Hispanic = new Pair<>();
//    public Pair<String> Radio_General = new Pair<>();
//    public Pair<String> Radio_Hispanic = new Pair<>();
//    public Pair<String> Mobile_General = new Pair<>();
//    public Pair<String> Mobile_Hispanic = new Pair<>();
//    public Pair<String> Opt_In_Email_General = new Pair<>();
//    public Pair<String> Opt_In_Email_Hispanic = new Pair<>();
    String path = "./dwn/";

    public Runner(){
        populateForwarders();
    }

    public void downloadAndUploadNeilsenBacklog(boolean simulateWritingCreatives, boolean uploadXLSX, int yearAllowed, Date dateFrom, Date dateTo) {


        XLSXService serv = new XLSXService();
        serv.mergeINTLFiles("./INTL_Master_List.xlsx");

        NeilsenService neilsenService = new NeilsenService();

        CreativeDTO[][] data = new CreativeDTO[36][];
        Gson gson = new Gson();
        for (int i=0;i<36;i++) {
            String path = i+".json";
            FileReader fr = null;

            File f = new File(path);
            if (f.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(path);
                    String text = IOUtils.toString(fis, "UTF-8");
                    data[i] = gson.fromJson(text,CreativeDTO[].class);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

                data[i] = neilsenService.getDataBacklog(i + 1);
                String s = gson.toJson(data[i]);
                Writer fstream = null;
                BufferedWriter out = null;
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(f);
                    fstream = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                    out = new BufferedWriter(fstream);
                    out.write(s);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        out.close();
                        fstream.close();
                        fos.close();
                    } catch (Exception ex){

                    }
                }
            }
            System.out.println(i+" Creatives:"+data[i].length);
        }

        CreativeDTO[] dtos = Arrays.copyOf(data[0], 350662);// + data[36].length);
        for(int i=1;i<36;i++) {
            System.arraycopy(data[i], 0, dtos, i * 10000, data[i].length);
        }

        Arrays.sort(dtos);


        CSVService.writeCSV(dtos,"./allNeilsenData.csv");

//        System.exit(0);



        File fdel = new File(path);

        try {
            FileUtils.deleteDirectory(fdel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fdel.mkdirs();



        SimpleDateFormat formatterYear=new SimpleDateFormat("yyyy", Locale.US);

        SimpleDateFormat formatterDate=new SimpleDateFormat("yyyyMMdd", Locale.US);

        SimpleDateFormat formatterTransmissionDate=new SimpleDateFormat("yyyyMMdd", Locale.US);

        //2022-10-29T00:00:00

        SFTPService sftpService = new SFTPService();

        HashMap<Pair<String>,List<CreativeDTO>> ads = new HashMap<>();

        int counter = 1;

        Map<String,Map<String,Map<String,List<String>>>> uploadedCreatives = sftpService.listFilesByMedia(yearAllowed,false);


        SimpleDateFormat formatterTransmission=new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        HashMap<String,CreativeDTO> adsMap = new HashMap<>();

        int toBeUploadedCount = 0;

        for(CreativeDTO dto:dtos) {

            if (adsMap.containsKey(dto.CreativeID)){
//                System.out.println("found duplicate for:"+dto.CreativeID);
                counter++;
                continue;
            } else {
                adsMap.put(dto.CreativeID,dto);
            }

            Date dtoDate = null;
            //2019-01-01T00:00:00
            try {
                dtoDate = formatterTransmission.parse(dto.TransmissionDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (dtoDate.before(dateFrom)) continue;
            if (dtoDate.after(dateTo)) continue;



            String correctedMedia = dto.Media;
            if (dto.Media.equalsIgnoreCase("internet")){
                correctedMedia = dto.Media + "_" + dto.AdType;
            }

            if (simulateWritingCreatives&&uploadedCreatives!=null&&uploadedCreatives.get(correctedMedia)!=null&&uploadedCreatives.get(correctedMedia).get(dto.Country)!=null &&
                    uploadedCreatives.get(correctedMedia).get(dto.Country).containsKey(correctedMedia+"_"+dto.Country+"_"+dto.CreativeID)) {
                if (simulateWritingCreatives) {
                    System.out.println("skipping creative, already present:" + dto.CreativeID + " ,date:" + dto.TransmissionDate);
                    continue;
                }
                counter++;

            }

            toBeUploadedCount++;


            String fullPath = null;
            URLS creativeURL = null;
            for(URLS url:dto.Urls) {
                if ("creative".equalsIgnoreCase(url.Type)) {
                    fullPath = path+url.Filename;
                    creativeURL = url;
                }
            }



            String limitedTitle = dto.Title;
            if (limitedTitle.length()>80) limitedTitle = limitedTitle.substring(0,79);

            String extension = creativeURL.Filename.substring(creativeURL.Filename.lastIndexOf("."));
            String newFileName = dto.CreativeID+"_"+dto.Advertiser+"_"+dto.Brand+"_"+limitedTitle+"_"+dto.Length+extension;

            if (newFileName.contains("/"))
                newFileName = newFileName.replace('/','-');

//            String year = formatterYear.format(dtoDate);
//            String dateString = formatterDate.format(dtoDate);

            String ftpPath;
            if (dto.Media.equalsIgnoreCase("internet")) {
                ftpPath = "/INTL/" + yearAllowed + "/" + yearAllowed + "0101/" + dto.Media+"_"+dto.AdType + "/" + dto.Country + "/" + newFileName;
            } else {
                ftpPath = "/INTL/" + yearAllowed + "/" + yearAllowed + "0101/" + dto.Media + "/" + dto.Country + "/" + newFileName;
            }

            dto.sourceURL = creativeURL.Url;
            dto.targetFileName = ftpPath;




            if (!simulateWritingCreatives) {
//                neilsenService.downloadFile(creativeURL.Url, fullPath);
                try {
                    System.out.println("Downloaded:" + fullPath + ":" + counter + " out of" + dtos.length + ";Size:" + Files.size(Paths.get(fullPath)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
//                    sftpService.uploadFileToSFTP(fullPath, ftpPath);
                    System.out.println("Uploaded to:" + ftpPath+" ,date:"+dto.TransmissionDate+" ,id:"+dto.CreativeID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            Pair<String> key = new Pair<>(dto.Media,dto.Country);
            if (dto.Media.equalsIgnoreCase("internet")) {
                key = new Pair<>(dto.Media+"_"+dto.AdType,dto.Country);
            }
            if (!ads.containsKey(key)) {
                ads.put(key,new LinkedList<>());
            }
            List<CreativeDTO> list = ads.get(key);
            list.add(dto);
            counter++;
        }


        Map<String,String> filesToBeDeleted = new HashMap<>();


        if (uploadXLSX) {

            XLSXService service = new XLSXService();
            String ftpXlsxPath = yearAllowed + "/" + yearAllowed + "0101/";

            counter = 1;
            for (Pair<String> key : ads.keySet()) {
                CreativeDTO dto = ads.get(key).get(0);

                String xlsxName = dto.Country + "_CreativeDetails_" + yearAllowed + "_" + dto.Media + ".xlsx";
                if (dto.Media.equalsIgnoreCase("internet")) {
                    xlsxName = dto.Country + "_CreativeDetails_" + yearAllowed + "_" + dto.Media + "_" + dto.AdType + ".xlsx";
                }
                String xlsxPath = "./dwn/" + xlsxName;
                service.createNewXlsxFiles(xlsxPath, ads.get(key), false);
                String uploadFile = "/INTL/" + ftpXlsxPath + xlsxName;
                try {
                    sftpService.uploadFileToSFTP(xlsxPath, uploadFile);
                    System.out.println("Uploaded:" + xlsxPath + ":" + counter + " out of" + ads.keySet().size() + ";" + ads.get(key).size() + " creatives to:"+uploadFile);

                    String correctedMedia = dto.Media;
                    if (dto.Media.equalsIgnoreCase("internet")){
                        correctedMedia = dto.Media + "_" + dto.AdType;
                    }


                    if (uploadedCreatives!=null&&uploadedCreatives.get(correctedMedia)!=null&&uploadedCreatives.get(correctedMedia).get(dto.Country)!=null) {
                        List<CreativeDTO> list = ads.get(key);
                        Map<String, List<String>> ads2 = uploadedCreatives.get(correctedMedia).get(dto.Country);
                        if (list.size() != ads2.size()) {
                            Map<String, CreativeDTO> map = new HashMap<>();
                            for (CreativeDTO cdto : list) {
                                map.put(correctedMedia + "_" + cdto.Country + "_" + cdto.CreativeID, cdto);
                            }

                            for (String fileKey : ads2.keySet()) {
                                if (!map.containsKey(fileKey)) {
                                    System.out.println("Found extra file:" + fileKey + ";" + ads2.get(fileKey));
                                    filesToBeDeleted.put(fileKey, ads2.get(fileKey).get(0));//TODO check

                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                counter++;
            }
        }



        Map<String,List<CreativeDTO>> missingCreatives = new HashMap<>();
        Map<String,List<String>> extraCreatives = new HashMap<>();
        List<String> deletePrecise = new LinkedList<>();

        if (uploadedCreatives!=null)
        for (Pair<String> key : ads.keySet()) {
            if (uploadedCreatives.get(key.first)==null||uploadedCreatives.get(key.first).get(key.second)==null)
                continue;
            List<CreativeDTO> list = ads.get(key);
            Map<String, List<String>> creatives = uploadedCreatives.get(key.first).get(key.second);

            if (list.size()==creatives.size()) continue;

            String keyN = key.toString();
            for(CreativeDTO dto:list) {
                if (!creatives.containsKey(key.first+"_"+key.second+"_"+dto.CreativeID)) {
                    List<CreativeDTO> creativesList = missingCreatives.get(keyN);
                    if (creativesList==null) {
                        creativesList = new LinkedList<>();
                        missingCreatives.put(keyN,creativesList);
                    }
                    creativesList.add(dto);
                }
            }

            HashMap<String,CreativeDTO> adsM = new HashMap<>();
            for(CreativeDTO dto:list)
                adsM.put(key.first+"_"+key.second+"_"+dto.CreativeID,dto);

            for(String adId:creatives.keySet()){
                if (!adsM.containsKey(adId)){
                    List<String> creativesList = extraCreatives.get(keyN);
                    if (creativesList==null) {
                        creativesList = new LinkedList<>();
                        extraCreatives.put(keyN,creativesList);
                    }
                    creativesList.add(adId);
                    String creativeToDelete = uploadedCreatives.get(key.first).get(key.second).get(adId).get(0);//TODO check
                    deletePrecise.add(creativeToDelete);
                }
            }
        }

        System.out.println("List to be deleted prepared:"+deletePrecise.size());
        System.out.println("Uploaded count:"+toBeUploadedCount);

//        for(String del:deletePrecise){
//            sftpService.deleteRemoteFile(del);
//        }

        //uploadedCreatives//--currently there
        //ads contains correct versions of adcodes+names
        //step1 - check all files in ads if they're present
        //step2 - check all files in uploadedCreatives - they should be present in ads
        //uploaded

        System.out.println("DONE");

        try {
            if (sftpService.sshClient!=null)
            sftpService.sshClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void compareAndFix(HashMap<Pair<String>,List<CreativeDTO>> ads,Map<String,Map<String,Map<String,String>>> uploadedCreatives){

    }

    public void runMasterList() {

        System.out.println("Merging XLSX files into master list");

        SFTPService sftpService = new SFTPService();

        String masterListLocation ="./INTL_Master_List.xlsx";
        String targetMasterListPath = "/INTL/INTL_Master_List.xlsx";
        XLSXService serv = new XLSXService();
        serv.mergeINTLFiles(masterListLocation);
        try {
            sftpService.uploadFileToSFTP(masterListLocation, targetMasterListPath);
        } catch (Exception ex){
            try {
                SFTPService sftpService1 = new SFTPService();
                sftpService1.uploadFileToSFTP(masterListLocation, targetMasterListPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void downloadAndUploadNeilsen(String dateStr, boolean simulateWritingCreatives) {

        SFTPService sftpService = new SFTPService();

        NeilsenService neilsenService = new NeilsenService();
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DATE, -1);
        Date date = cal.getTime();
        Date [] dates = new Date[7];

        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        if (dateStr!=null) {
            try {
                date = formatter.parse(dateStr);
                cal.setTime(date);
                dates[0] = cal.getTime();
                for(int i=1;i<7;i++){
                    cal.add(Calendar.DATE, -1);
                    dates[i] = cal.getTime();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        String date1 = formatter.format(date);

        CreativeDTO[][] dtosWeekly = new CreativeDTO[7][];
        List<CreativeDTO> list = new Vector<>();

        for(int i=0;i<7;i++){
            dtosWeekly[i] = neilsenService.getDateFromDate(formatter.format(dates[i]));
            list.addAll(Arrays.asList(dtosWeekly[i]));
            System.out.println("Got metadata for:"+dates[i]);
        }
        Vector<CreativeDTO> vector = new Vector<CreativeDTO>(list);
        CreativeDTO[] dtos = new CreativeDTO[vector.size()];
        dtos = vector.toArray(dtos);

        System.out.println("Creatives:"+dtos.length);

        File fdel = new File(path);

        try {
            FileUtils.deleteDirectory(fdel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fdel.mkdirs();

        SimpleDateFormat formatterYear=new SimpleDateFormat("yyyy", Locale.US);
        String year = formatterYear.format(date);
        SimpleDateFormat formatterDate=new SimpleDateFormat("yyyyMMdd", Locale.US);
        String dateString = formatterDate.format(date);



        HashMap<Pair<String>,List<CreativeDTO>> ads = new HashMap<>();

        int counter = 1;
        for(CreativeDTO dto:dtos) {
            String fullPath = null;
            URLS creativeURL = null;
            for(URLS url:dto.Urls) {
                if ("creative".equalsIgnoreCase(url.Type)) {
                    fullPath = path+url.Filename;
                    creativeURL = url;
                }
            }

            if (!simulateWritingCreatives) {
                neilsenService.downloadFile(creativeURL.Url, fullPath);

                try {
                    System.out.println("Downloaded:" + fullPath + ":" + counter + " out of" + dtos.length + ";Size:" + Files.size(Paths.get(fullPath)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String extension = creativeURL.Filename.substring(creativeURL.Filename.lastIndexOf("."));
            String newFileName ;
            if (dto.Title.length()>80){
                String truncatedTitle = dto.Title.substring(0,80);
                newFileName = dto.CreativeID+"_"+dto.Advertiser+"_"+dto.Brand+"_"+truncatedTitle+"_"+dto.Length+extension;
            } else {
                newFileName = dto.CreativeID+"_"+dto.Advertiser+"_"+dto.Brand+"_"+dto.Title+"_"+dto.Length+extension;
            }

            if (newFileName.contains("/"))
                newFileName = newFileName.replace('/','-');

            String ftpPath;
            if (dto.Media.equalsIgnoreCase("internet")) {
                ftpPath = "/INTL/" + dto.Advertiser + "/" + year + "/"  +dateString + "/" + dto.Media+"_"+ dto.AdType + "/" + dto.Country + "/" + newFileName;
            } else {
                ftpPath = "/INTL/" + dto.Advertiser +"/"  + year + "/" + dateString + "/" + dto.Media + "/" + dto.Country + "/" + newFileName;
            }

            dto.sourceURL = creativeURL.Url;
            dto.targetFileName = ftpPath;


            try {
                if (!simulateWritingCreatives) {
                    sftpService.uploadFileToSFTP(fullPath, ftpPath);
                    System.out.println("Uploaded to:" + ftpPath);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Pair<String> key = new Pair<>(dto.Media,dto.Country);
            if (dto.Media.equalsIgnoreCase("internet")) {
                key = new Pair<>(dto.Media+"_"+dto.AdType,dto.Country);
            }
            if (!ads.containsKey(key)) {
                ads.put(key,new LinkedList<>());
            }
            ads.get(key).add(dto);
            counter++;
        }

        XLSXService service = new XLSXService();
        String ftpXlsxPath = year+"/"+dateString+"/";

        counter = 1;
        for(Pair<String> key:ads.keySet()){
            CreativeDTO dto = ads.get(key).get(0);
            String xlsxName = dto.Country+"_CreativeDetails_"+year+"_"+dto.Media+".xlsx";
            if (dto.Media.equalsIgnoreCase("internet")) {
                xlsxName = dto.Country+"_CreativeDetails_"+year+"_"+dto.Media+"_"+dto.AdType+".xlsx";
            }
            String xlsxPath = "./dwn/"+xlsxName;
            service.createNewXlsxFiles(xlsxPath,ads.get(key), false);

            File f = new File("./intl/"+dateStr+"/");
            f.mkdirs();

            service.createNewXlsxFiles("./intl/"+dateStr+"/"+xlsxName,ads.get(key), true);

            String uploadFile = "/INTL/"+ftpXlsxPath+xlsxName;
            try {
                if (!simulateWritingCreatives){
                    sftpService.uploadFileToSFTP(xlsxPath,uploadFile);
                    System.out.println("Uploaded:"+xlsxPath+":"+counter+" out of"+ads.keySet().size()+";"+ads.get(key).size()+" creatives");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            counter++;
        }

        try {
            sftpService.sshClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void downloadAndUpload(boolean simulateWritingCreatives, String startDate, String endDate,String report) throws IOException, ParseException {
        CreativeService creativeService = new CreativeService();
        UsCreativeDTO[] usMetadata = creativeService.getCreatives(startDate,endDate,report);

        SFTPService sftpService = new SFTPService();
        XLSXService xlsxService = new XLSXService();
        MediaServerDirect serverDirect = new MediaServerDirect();


        Set<String> advertisers = new HashSet<>();
        Set<String> mediaNames = new HashSet<>();
        for (UsCreativeDTO creative : usMetadata){
            advertisers.add(creative.getAdvertiserName());
            mediaNames.add(creative.getMediaCode());
        }

        Map<String,List<UsCreativeDTO>> groupByAdvertiser = new HashMap<>();
        for (String s : advertisers){
            List<UsCreativeDTO> list = new ArrayList<>();
            for (UsCreativeDTO data : usMetadata){
                if (data.getAdvertiserName().equals(s)){
                    list.add(data);
                }
            }
            groupByAdvertiser.put(s,list);
        }
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyyMMdd");

        for (String advertiser : advertisers){
            List<UsCreativeDTO> usCreativeDTOList = groupByAdvertiser.get(advertiser);
                for (UsCreativeDTO creativeDTO :  usCreativeDTOList){

                    Date date = inputFormat.parse(creativeDTO.getFirstRunDate());
                    String formatedDate = outputFormat.format(date);
                    String outputDateString = outputFormat.format(date);

                    String destPath = "/US/"+creativeDTO.getAdvertiserName().toLowerCase()+"/2023/"+formatedDate+"/"+
                            MediaName.getMediaName(creativeDTO.getMediaCode())+"/"+report+"/";
                    String sourcePath = serverDirect.copyFile(creativeDTO.getAdID(), creativeDTO.getMediaCode());
                    sftpService.uploadFileToSFTP(sourcePath,destPath);

                }

            xlsxService.createNewXlsxFilesForUS("./US/"+advertiser,usCreativeDTOList);
        }
    }



    public void downloadAndUpload(boolean simulateWritingCreatives) {

//        XLSXService service = new XLSXService();

        HashMap<String, String> requestsToSubject = new HashMap<>();

        EmailProcessorService emailService = new EmailProcessorService();
        List<Pair<String>> storedRequestIds = emailService.getStoredRequestsFromEmails();
        List<String> subjects = new LinkedList<>();

        for(Pair<String> p:storedRequestIds) {
            try {
                String subject = p.first;
                SourceForwarder actual = null;
                for(SourceForwarder sf:forwarders){
                    if (sf.subjectMatch(subject)) {
                        sf.storedRequests.add(p.second);
                        requestsToSubject.put(p.second,p.first);
                        subjects.add(p.first);
                        actual = sf;
                        break;
                    }
                }
                if (actual==null) {
                    System.out.println("Mismatch between forwarders and file for storeRequestId:"+p.second+";"+p.first);
                    continue;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        String []subject = requestsToSubject.entrySet().iterator().next().getValue().split(" ");
        String date = subject[subject.length-3]+subject[subject.length-2]+subject[subject.length-1];
        SimpleDateFormat formatter=new SimpleDateFormat("MMMdd,yyyy", Locale.US);
        Date d= null;
        try {
            d = formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat formatterYear=new SimpleDateFormat("yyyy", Locale.US);
        String year = formatterYear.format(d);
        SimpleDateFormat formatterDate=new SimpleDateFormat("yyyyMMdd", Locale.US);
        String dateString = formatterDate.format(d);

        ZipService zipService = new ZipService();
        XLSXService xlsxService = new XLSXService();

        SFTPService sftpService = new SFTPService();

        SMARTService smartService = new SMARTService();
        String token = smartService.login();


        for(SourceForwarder sf:forwarders){

            if (sf.storedRequests.first!=null&&sf.storedRequests.second!=null) {
                String fileName = smartService.downloadFileByRequestId(sf.storedRequests.first, token, sf.targetMediaTempFolder);
                sf.downloadedFileNames.add(fileName);
                fileName = smartService.downloadFileByRequestId(sf.storedRequests.second, token, sf.targetMediaTempFolder);
                sf.downloadedFileNames.add(fileName);

                String zipFilename = null;
                String xlsFilename = null;

                if (sf.downloadedFileNames.first.toLowerCase().endsWith(".zip")) zipFilename = sf.downloadedFileNames.first;
                if (sf.downloadedFileNames.second.toLowerCase().endsWith(".zip")) zipFilename = sf.downloadedFileNames.second;

                if (sf.downloadedFileNames.first.toLowerCase().endsWith(".xlsx")) xlsFilename = sf.downloadedFileNames.first;
                if (sf.downloadedFileNames.second.toLowerCase().endsWith(".xlsx")) xlsFilename = sf.downloadedFileNames.second;

                if (xlsFilename==null||zipFilename==null){
                    System.out.println("MISSING REQUIRED FILE");
                }

                File zipDir = new File("dwn/tgt");
                zipDir.mkdirs();

                zipService.unzip(zipFilename,zipDir.getAbsolutePath());

                File x = new File(xlsFilename);
                Map<Integer, List<String>> map =  xlsxService.getDataFromSheet(x.getAbsolutePath());

                String[] creativeFileNames = zipDir.list();

                int namesRowId = xlsxService.getIdRowWithNames(map);

                // /2022/20221011/Opt_In_Email/US/General_Media

                String xlsTarget ="/US/"+year+"/"+dateString+"/";
                String target = xlsTarget+sf.sftpTargetPath+"/";

                HashMap<String,String> headline = new HashMap<>();
                HashMap<String,String> firstRun = new HashMap<>();


                Pair<Integer> data =  xlsxService.getDataRows(map,namesRowId);
                for(int i=data.first;i<=data.second;i++) {//for each entry there should be a file
                    String adcode = xlsxService.getAdcode(map.get(namesRowId),map.get(i));
                    ColumnarItem ci = smartService.getColumnarInfo(token, adcode, getReportId(sf));//HARRIS-DB-HM
                    headline.put(ci.adId,ci.headlineText);
                    firstRun.put(ci.adId,ci.firstRunUnitValue);

                    String renameTo = xlsxService.getFileNameTo(map.get(namesRowId),map.get(i),ci);
                    for(String name:creativeFileNames) {
                        if (name.toLowerCase().startsWith("asset_ad_"+adcode.toLowerCase()+"_")) {
                            String extension = name.substring(name.lastIndexOf(".")+1);
                            File f = new File("dwn/tgt/"+name);
                            String newFileName = "dwn/tgt/"+renameTo+"."+extension;
                            File newFile =new File(newFileName);
                            boolean renamed = f.renameTo(newFile);
                            try {

                                String targetFileName = target + newFile.getName();

                                if (!simulateWritingCreatives) {
                                    System.out.println("starting to write:" + newFile.getAbsolutePath() + " TO:" + targetFileName);
                                    sftpService.uploadFileToSFTP(newFile.getAbsolutePath(), targetFileName);
                                    System.out.println("file written");
                                    newFile.delete();
                                }

                            } catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }
                    }
                }

                int separator = xlsFilename.lastIndexOf("/");
                String newFileName = xlsFilename.substring(0,separator)+"/US_"+xlsFilename.substring(separator+1);
                try {
                    xlsxService.addDataToSheet(xlsFilename,newFileName,headline,firstRun,xlsxService.getAdcodePosition(map.get(namesRowId)),data.second-data.first+1);
                    sftpService.uploadFileToSFTP(newFileName, xlsTarget+"US_"+x.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }


        try {
            sftpService.sshClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }



        File f = new File(path);

        f.delete();



        emailService.markCompleted(subjects);

        /*
        TV
Organic Social
Radio
Mobile
Opt-In Email

General
Hispanic
         */



//        smartService.downloadFileByRequestId("632c5f1fd6d39e17157776c0",token);//632c5f1fd6d39e17157776c0
        //zip 63344832edad79814718f436



//        for(Pair<String> p:storedRequestIds) {
//            try {
//                String subject = p.first;
//                SourceForwarder actual = null;
//                for(SourceForwarder sf:forwarders){
//                    if (sf.subjectMatch(subject)) {
//                        sf.storedRequests.add(p.second);
//                        actual = sf;
//                        break;
//                    }
//                }
//                if (actual==null) {
//                    System.out.println("Mismatch between forwarders and file for storeRequestId:"+p.second);
//                    continue;
//                }
//                String fileName = smartService.downloadFileByRequestId(p.second, token, actual.targetMediaTempFolder);
//                actual.downloadedFileNames.add(fileName);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }




        //https://cft-storage-brand-c-east-1.s3.amazonaws.com/export/prod/export-b8d42c37-eac8-442c-9f4e-57df64a02fba-xnYjVg.zip?AWSAccessKeyId=AKIAJQCOJX3T2L7QPXBA&Expires=1664934459

    }

    public void executeManual(boolean simulateWritingCreatives, String continueFrom) {

//        SolrService solrService = new SolrService();
//        String mediaCode = solrService.getMediaCode("TVICCO-0986");

//        MediaServerDirect mediaServerDirect = new MediaServerDirect();
//        mediaServerDirect.copyFile("AMAZRT-66420","B");



        //"Online_Display/General/3017"
        String [] strings = null;
        if (continueFrom!=null)
            strings = continueFrom.split("/");

        File f = new File("./manual");
        File [] yearFiles = f.listFiles();
        for(File yearDir:yearFiles) {
            if (!yearDir.isDirectory()) continue;
            File[] mediaDirs = yearDir.listFiles();
            String year = yearDir.getName();
            for(File mediaDir:mediaDirs) {
                if (!mediaDir.isDirectory()) continue;
                String dir = mediaDir.getName();
                for(SourceForwarder sf:forwarders) {
                    if (!dir.contains(sf.generalOrHispanic)) continue;
                    if (!dir.contains(sf.emailSubjectMediaPartMatch)) continue;
                    File[] filesPair = mediaDir.listFiles();
                    if (filesPair.length!=2) {
                        System.out.println("INVALID FILES:"+filesPair[0].getAbsolutePath());
                        System.exit(0);
                    }
                    File xslxFile = null;
                    File zipFile = null;
                    for(File file:filesPair) {
                        if (file.getName().endsWith(".xlsx")) {
                            xslxFile = file;
                        }
                        if (file.getName().endsWith(".zip")) {
                            zipFile = file;
                        }
                    }
                    sf.downloadedFileNames = new Pair<>(xslxFile.getAbsolutePath(),zipFile.getAbsolutePath());
                    System.out.println("Running Manual:"+sf.generalOrHispanic+";"+sf.emailSubjectMediaPartMatch+";"+year);
                    Integer resume = null;
                    if (strings!=null&&sf.emailSubjectMediaPartMatch.equalsIgnoreCase(strings[0])&&sf.generalOrHispanic.equalsIgnoreCase(strings[1])){
                        resume = Integer.valueOf(strings[2]);
                    }


                    runManual(sf,year, year+"0101",simulateWritingCreatives, resume);
                    break;
                }
            }

        }






//        SourceForwarder sf16 = forwarders[16];
//        sf16.storedRequests = new Pair<>("1","1");
//        dir = "./manual/"+sf16.generalOrHispanic+"/"+sf16.emailSubjectMediaPartMatch+"/"+year;
//        sf16.downloadedFileNames = new Pair<>(dir+"/Harris_Poll_"+sf16.generalOrHispanic+"_Media_"+"Online_Video.xlsx",dir+"/1.zip");
//        runManual(sf16,year,year+"0101",simulateWritingCreatives);


//        SourceForwarder sf1 = forwarders[1];
//        sf1.storedRequests = new Pair<>("1","1");
//        dir = "./manual/"+sf1.generalOrHispanic+"/"+sf1.emailSubjectMediaPartMatch+"/"+year;
//        sf1.downloadedFileNames = new Pair<>(dir+"/Harris_Poll_"+sf1.generalOrHispanic+"_Media_"+"TV.xlsx",dir+"/1.zip");
//        runManual(sf1,year,year+"0101");

//        SourceForwarder sf0 = forwarders[0];
//        sf0.storedRequests = new Pair<>("1","1");
//        sf0.downloadedFileNames = new Pair<>("./manual/general_tv/"+year+"/Harris_Poll_"+sf0.generalOrHispanic+"_Media_"+"TV.xlsx","./manual/general_tv/"+year+"/1.zip");
//        runManual(sf0,year,year+"0101");

    }




    public void runManual(SourceForwarder sf0, String year, String date, boolean simulateWritingCreatives, Integer resume) {

        boolean fixBadFiles = true;
        boolean checkExisting = false;

        File fdel = new File(path);

        try {
            FileUtils.deleteDirectory(fdel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ZipService zipService = new ZipService();
        XLSXService xlsxService = new XLSXService();

        SFTPService sftpService = new SFTPService();
//        String q = "Z405OL-6391";
        SMARTService smartService = new SMARTService();
        String token = smartService.login();


//        List<String> missingCreatives = new LinkedList<>();

        MediaServerDirect mediaService = new MediaServerDirect();

//        for (String h:missingCreatives)
//        System.out.println(h);

        System.out.println("********************************************");
        System.out.println("********************************************");
        System.out.println("********************************************");
        SolrService solrService = new SolrService();

        if (fixBadFiles) {
            Map<String,String> [] maps;
            maps = new Map [5];

            maps[0] = sftpService.listAllBadFilesUS(2019);
            maps[1] = sftpService.listAllBadFilesUS(2020);
            maps[2] = sftpService.listAllBadFilesUS(2021);
            maps[3] = sftpService.listAllBadFilesUS(2022);
            maps[4] = sftpService.listAllBadFilesUS(2023);
            System.exit(0);
            File dir = new File("./tmp/");
            File[] files = dir.listFiles();
            int counter = 0;
            for (File f:files){
                String adcode = f.getName();
                String extension = adcode.substring(adcode.lastIndexOf(".")+1);
                adcode = adcode.substring(0,adcode.lastIndexOf("."));

                for(int i=0;i<5;i++){
                    if (maps[i].containsKey(adcode)){
                        try {
                            String targetFileName = maps[i].get(adcode);
                            String newFileName = targetFileName.substring(0, targetFileName.lastIndexOf(".")) + "." + extension;

                            System.out.println("starting to write:" + f.getAbsolutePath() + " TO:" + newFileName);
//                            sftpService.uploadFileToSFTP(f.getAbsolutePath(), newFileName);
                            System.out.println("file written, row:" + counter++ + " out of:" + files.length);

                            if (!"png".equalsIgnoreCase(extension)) {
//                                sftpService.deleteRemoteFile(targetFileName);
                                System.out.println("deleted bad file:" + targetFileName);
                            } else
                                System.out.println("bad file was overwritten");
//                            f.delete();
                            System.out.println("deleted local file:" + f.getAbsolutePath());
                        } catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }
            }

            System.exit(0);

            for(int i=0;i<4;i++){

//                Map<String,String> map=maps[i];
//                System.out.println("count:"+map.size());
//                System.out.println("Processing:"+i);
//                int counter = 0;
//                for(String s:map.keySet()) {
//                    try {
//                        String mediaCode = solrService.getMediaCode(s);
//                        System.out.println("adcode:" + s + " mediaCode:" + mediaCode+" "+counter+++":"+map.size());
//                        String fileName = mediaService.copyFile(s, mediaCode);
//                        System.out.println("downloaded:" + fileName);
//                    }catch (Exception ex){
//                        ex.printStackTrace();
//                    }
//                }
            }


//            for(String adCode:map.keySet()) {
//                String fileName = map.get(adCode);
//                fileName = fileName.substring(fileName.lastIndexOf("/")+1);
//                smartService.getCreativeAsset(token,adCode,"HARRIS-POLL-CREATIVEAPI-DB",fileName);
//            }
            return;
        }


//        SMARTService smartService = new SMARTService();
//        String token = smartService.login();

        String zipFilename = null;
        String xlsFilename = null;

        if (sf0.downloadedFileNames.first.toLowerCase().endsWith(".zip")) zipFilename = sf0.downloadedFileNames.first;
        if (sf0.downloadedFileNames.second.toLowerCase().endsWith(".zip")) zipFilename = sf0.downloadedFileNames.second;

        if (sf0.downloadedFileNames.first.toLowerCase().endsWith(".xlsx")) xlsFilename = sf0.downloadedFileNames.first;
        if (sf0.downloadedFileNames.second.toLowerCase().endsWith(".xlsx")) xlsFilename = sf0.downloadedFileNames.second;

        File zipDir = new File("dwn/tgt");
        zipDir.mkdirs();



        String xlsTarget ="/"+year+"/"+date+"/";
        String target = xlsTarget+sf0.sftpTargetPath+"/";
        int separator = xlsFilename.lastIndexOf("/");
        String fileName = "/US_Harris_Poll_"+sf0.generalOrHispanic+"_Media_"+sf0.emailSubjectMediaPartMatch+".xlsx";//+xlsFilename.substring(separator+1);
        String newFileName = xlsFilename.substring(0,separator)+fileName;
        File x = new File(xlsFilename);
        String targetXlSFileName = "/US/"+xlsTarget+fileName;
        if (sftpService.doFileExistsInFTP(targetXlSFileName) &&!checkExisting){
            System.out.println(targetXlSFileName+" exists, skipping media type");
            return;
        }





        Map<Integer, List<String>> map =  xlsxService.getDataFromSheet(x.getAbsolutePath());



        Map<String, String> uploadedFiles = sftpService.listAllFilesUS(Integer.valueOf(year));

        int namesRowId = xlsxService.getIdRowWithNames(map);


        Pair<Integer> data =  xlsxService.getDataRows(map,namesRowId);

        HashMap<String,String> headline = new HashMap<>();
        HashMap<String,String> firstRun = new HashMap<>();



        if (checkExisting) {
            LinkedList<String> extraAdcodes = new LinkedList<>();
            LinkedList<String> foundAdcodes = new LinkedList<>();

            for(String adcodeUploaded:uploadedFiles.keySet()){
                boolean found = false;
                for(int i=data.first;i<=data.second;i++) {
                    String adcode = xlsxService.getAdcode(map.get(namesRowId),map.get(i));
                    if (adcodeUploaded.equalsIgnoreCase(adcode)) {
                        ((LinkedList<String>) foundAdcodes).push(adcode);
                        found = true;
                    }
                }
                if (!found) {
                    extraAdcodes.push(adcodeUploaded);
                }
            }
//            for(String s:extraAdcodes) {
//                System.out.println("Extra adcode found:"+s+" delete file:"+uploadedFiles.get(s));
//            }
            return;
        }

        zipService.unzip(zipFilename,zipDir.getAbsolutePath());
        String [] creativeFileNames = zipDir.list();

        // /2022/20221011/Opt_In_Email/US/General_Media







        for(int i=data.first;i<=data.second;i++) {//for each entry there should be a file
            if (resume!=null) {
                i = resume-1;
                resume = null;
                continue;
            }
            System.out.println("Processing row:"+i+" for:"+sf0.emailSubjectMediaPartMatch+" "+sf0.generalOrHispanic);

            String adcode = xlsxService.getAdcode(map.get(namesRowId),map.get(i));

            if (adcode==null||adcode.isEmpty()||adcode.isBlank())
                continue;
//                    if (adcode.compareToIgnoreCase("Z405OL-6036")==0){
//                        int a=0;
//                        a++;
//                    }
            ColumnarItem ci = smartService.getColumnarInfo(token, adcode, getReportId(sf0));//HARRIS-DB-HM

            if (ci==null) {
                System.out.println("Missing information about adcode:"+adcode);
                continue;
            }

            headline.put(ci.adId,ci.headlineText);
            firstRun.put(ci.adId,ci.firstRunUnitValue);
            HashMap<String,Integer> printCounts = new HashMap<>();

            String renameTo = xlsxService.getFileNameTo(map.get(namesRowId),map.get(i),ci);
            boolean found = false;
            for(String name:creativeFileNames) {
                if (name.toLowerCase().startsWith("asset_ad_"+adcode.toLowerCase()+"_")) {
                    String extension = name.substring(name.lastIndexOf(".")+1);
                    File f = new File("dwn/tgt/"+name);
                    String newFName = "dwn/tgt/"+renameTo+"."+extension;
                    File newFile =new File(newFName);
                    boolean renamed = f.renameTo(newFile);
                    try {
                        found = true;
                        if (!simulateWritingCreatives) {

                            String targetFileName = "/US/"+target + newFile.getName();

                            if (sf0.emailSubjectMediaPartMatch.equalsIgnoreCase("print")){
                                String adId = adcode.toLowerCase();
                                Integer count = 0;
                                if (printCounts.containsKey(adId)) {
                                    count = printCounts.get(adId);
                                }
                                count=count+1;
                                printCounts.put(adId,count);
                                int extensionPos = targetFileName.lastIndexOf(".");
                                targetFileName = targetFileName.substring(0,extensionPos)+"-"+count+targetFileName.substring(extensionPos);
                            }



                            System.out.println("starting to write:" + newFile.getAbsolutePath() + " TO:" + targetFileName);

                            sftpService.uploadFileToSFTP(newFile.getAbsolutePath(), targetFileName);
                            System.out.println("file written, row:" + i+" out of:"+(data.second-data.first+1));
                            newFile.delete();
                        }
                    } catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }

            if (!found) {
                System.out.println("Missing creative for:"+ci.adId+" with target filename:"+renameTo);
            }
        }



        try {
            System.out.println("starting to create xlsx from:" + xlsFilename + " TO:" + newFileName+" entried:"+(data.second-data.first+1));
            xlsxService.addDataToSheet(xlsFilename,newFileName,headline,firstRun,xlsxService.getAdcodePosition(map.get(namesRowId)),data.second-data.first+1);
            System.out.println("Uploading xlsx file to SFTP:"+targetXlSFileName+" FROM:"+newFileName);

            sftpService.uploadFileToSFTP(newFileName, targetXlSFileName);
            System.out.println("Uploaded file");
            x.delete();
            System.out.println("Deleted temp xlsx file");
        } catch (IOException e) {
            e.printStackTrace();
        }




        try {
            FileUtils.deleteDirectory(fdel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            sftpService.sshClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getReportId(SourceForwarder sf) {
        if ("General".equalsIgnoreCase(sf.generalOrHispanic)){
            return "HARRIS-DB";//general media
        } else return "HARRIS-DB-HM";//hispanic
    }


    private void populateForwarders(){
        int cnt = 0;
//        forwarders[cnt++] = new SourceForwarder(
//                "TV/General",
//                "General",
//                "TV/General_Media/",
//                "TV");
//        forwarders[cnt++] = new SourceForwarder(
//                "TV/Hispanic",
//                "Hispanic",
//                "TV/Hispanic_Media/",
//                "TV");
//
//        forwarders[cnt++] = new SourceForwarder(
//                "Organic_Social/General",
//                "General",
//                "Organic_Social/General_Media/",
//                "Organic_Social");
//        forwarders[cnt++] = new SourceForwarder(
//                "Organic_Social/Hispanic",
//                "Hispanic",
//                "Organic_Social/Hispanic_Media/",
//                "Organic_Social");
//
//        forwarders[cnt++] = new SourceForwarder(
//                "Radio/General",
//                "General",
//                "Radio/General_Media/",
//                "Radio");
//        forwarders[cnt++] = new SourceForwarder(
//                "Radio/Hispanic",
//                "Hispanic",
//                "Radio/Hispanic_Media/",
//                "Radio");
//
//        forwarders[cnt++] = new SourceForwarder(
//                "Mobile/General",
//                "General",
//                "Mobile/General_Media/",
//                "Mobile");
//        forwarders[cnt++] = new SourceForwarder(
//                "Mobile/Hispanic",
//                "Hispanic",
//                "Mobile/Hispanic_Media/",
//                "Mobile");
//
//        forwarders[cnt++] = new SourceForwarder(
//                "Opt_In_Email/General",
//                "General",
//                "Opt_In_Email/General_Media/",
//                "Email");
//        forwarders[cnt++] = new SourceForwarder(
//                "Opt_In_Email/Hispanic",
//                "Hispanic",
//                "Opt_In_Email/Hispanic_Media/",
//                "Email");
//
//        forwarders[cnt++] = new SourceForwarder(
//                "Online_Display/General",
//                "General",
//                "Online_Display/General_Media/",
//                "Online_Display");
//        forwarders[cnt++] = new SourceForwarder(
//                "Online_Display/Hispanic",
//                "Hispanic",
//                "Online_Display/Hispanic_Media/",
//                "Online_Display");
//
//        forwarders[cnt++] = new SourceForwarder(
//                "Print/General",
//                "General",
//                "Print/General_Media/",
//                "Print");
//        forwarders[cnt++] = new SourceForwarder(
//                "Print/Hispanic",
//                "Hispanic",
//                "Print/Hispanic_Media/",
//                "Print");
//
//        forwarders[cnt++] = new SourceForwarder(
//                "Outdoor/General",
//                "General",
//                "Outdoor/General_Media/",
//                "Outdoor");
//        forwarders[cnt++] = new SourceForwarder(
//                "Outdoor/Hispanic",
//                "Hispanic",
//                "Outdoor/Hispanic_Media/",
//                "Outdoor");
//
//        forwarders[cnt++] = new SourceForwarder(
//                "Online Video/General",
//                "General",
//                "Online Video/General_Media/",
//                "Online_Video");
//        forwarders[cnt++] = new SourceForwarder(
//                "Online Video/Hispanic",
//                "Hispanic",
//                "Online Video/Hispanic_Media/",
//                "Online_Video");
//
//        forwarders[cnt++] = new SourceForwarder(
//                "Cinema/General",
//                "General",
//                "Cinema/General_Media/",
//                "Cinema");
//        forwarders[cnt++] = new SourceForwarder(
//                "Online Video/Hispanic",
//                "Hispanic",
//                "Cinema/Hispanic_Media/",
//                "Cinema");



        forwarders[cnt++] = new SourceForwarder(
                "TV/General",
                "General",
                "TV/General_Media/",
                "TV");
        forwarders[cnt++] = new SourceForwarder(
                "TV/Hispanic",
                "Hispanic",
                "TV/Hispanic_Media/",
                "TV");

        forwarders[cnt++] = new SourceForwarder(
                "Organic_Social/General",
                "General",
                "Organic_Social/General_Media/",
                "Organic Social");
        forwarders[cnt++] = new SourceForwarder(
                "Organic_Social/Hispanic",
                "Hispanic",
                "Organic_Social/Hispanic_Media/",
                "Organic Social");

        forwarders[cnt++] = new SourceForwarder(
                "Radio/General",
                "General",
                "Radio/General_Media/",
                "Radio");
        forwarders[cnt++] = new SourceForwarder(
                "Radio/Hispanic",
                "Hispanic",
                "Radio/Hispanic_Media/",
                "Radio");

        forwarders[cnt++] = new SourceForwarder(
                "Mobile/General",
                "General",
                "Mobile/General_Media/",
                "Mobile");
        forwarders[cnt++] = new SourceForwarder(
                "Mobile/Hispanic",
                "Hispanic",
                "Mobile/Hispanic_Media/",
                "Mobile");

        forwarders[cnt++] = new SourceForwarder(
                "Opt_In_Email/General",
                "General",
                "Opt_In_Email/General_Media/",
                "Opt-In Email");
        forwarders[cnt++] = new SourceForwarder(
                "Opt_In_Email/Hispanic",
                "Hispanic",
                "Opt_In_Email/Hispanic_Media/",
                "Opt-In Email");

        forwarders[cnt++] = new SourceForwarder(
                "Online_Display/General",
                "General",
                "Online_Display/General_Media/",
                "Online Display");
        forwarders[cnt++] = new SourceForwarder(
                "Online_Display/Hispanic",
                "Hispanic",
                "Online_Display/Hispanic_Media/",
                "Online Display");

        forwarders[cnt++] = new SourceForwarder(
                "Print/General",
                "General",
                "Print/General_Media/",
                "Print");
        forwarders[cnt++] = new SourceForwarder(
                "Print/Hispanic",
                "Hispanic",
                "Print/Hispanic_Media/",
                "Print");

        forwarders[cnt++] = new SourceForwarder(
                "Outdoor/General",
                "General",
                "Outdoor/General_Media/",
                "Outdoor");
        forwarders[cnt++] = new SourceForwarder(
                "Outdoor/Hispanic",
                "Hispanic",
                "Outdoor/Hispanic_Media/",
                "Outdoor");

        forwarders[cnt++] = new SourceForwarder(
                "Online Video/General",
                "General",
                "Online Video/General_Media/",
                "Online Video");
        forwarders[cnt++] = new SourceForwarder(
                "Online Video/Hispanic",
                "Hispanic",
                "Online Video/Hispanic_Media/",
                "Online Video");

        forwarders[cnt++] = new SourceForwarder(
                "Cinema/General",
                "General",
                "Cinema/General_Media/",
                "Cinema");
        forwarders[cnt++] = new SourceForwarder(
                "Online Video/Hispanic",
                "Hispanic",
                "Cinema/Hispanic_Media/",
                "Cinema");

    }


//        for(String s:badCreatives) {
//            try{
////                boolean get = smartService.getCreativeAsset(token,s,"","1.1");
//                if (get) {
//                    continue;
//                } else {
//                    ((LinkedList<String>) missingCreatives).push(s);
//                }
//            } catch (Exception ex){
//                ((LinkedList<String>) missingCreatives).push(s);
//            }
//        }
//
//        String [] badCreatives = {"AMAZRT-69561",
//                "Z405OL-6875",
//                "AMAZRT-69560",
//                "Z405OL-6676",
//                "AMAZRT-68272",
//                "AMAZRT-69488",
//                "AMAZRT-81563",
//                "RINDMS-0827",
//                "AMAZRT-67783",
//                "AMAZRT-87309",
//                "AMAZRT-67780",
//                "AMAZRT-80230",
//                "AMAZRT-84112",
//                "AMAZRT-69489",
//                "Z405OL-6391",
//                "Z405OL-4093",
//                "AMAZRT-85040",
//                "Z405OL-6871",
//                "Z405OL-7002",
//                "Z405OL-6675",
//                "Z405OL-4056",
//                "Z405OL-6872",
//                "Z405OL-6993",
//                "Z405OL-4057",
//                "Z405OL-4052",
//                "Z405OL-0145",
//                "Z68JCO-2098",
//                "WHFMRT-8887",
//                "WHFMRT-9539",
//                "AMAZRT-66881",
//                "AMAZRT-89638",
//                "AMAZRT-68941",
//                "AMAZRT-83092",
//                "ZAPPRT-3186",
//                "AMAZRT-69717",
//                "Z405OL-7091",
//                "AMAZRT-70787",
//                "Z405OL-6686",
//                "AMAZRT-73259",
//                "Z405OL-6685",
//                "Z405OL-6680",
//                "Z405OL-3025",
//                "Z405OL-0159",
//                "Z405OL-6698",
//                "Z68JCO-2101",
//                "Z405OL-7349",
//                "AMAZRT-68176",
//                "AMAZRT-67088",
//                "AMAZRT-68771",
//                "AMAZRT-68776",
//                "AMAZRT-69109",
//                "AMAZRT-69747",
//                "AMAZRT-67965",
//                "AMAZRT-70696",
//                "AMAZRT-70698",
//                "ZAPPRT-3415",
//                "AMAZRT-68280",
//                "Z405OL-3234",
//                "Z405OL-6902",
//                "AMAZRT-82701",
//                "AMAZRT-67031",
//                "Z405OL-6866",
//                "Z405OL-6987",
//                "AUDIOL-14733",
//                "Z405OL-9338",
//                "Z405OL-6865",
//                "ZAPPRT-3293",
//                "ZAPPRT-3292",
//                "AMAZRT-67671",
//                "AMAZRT-69976",
//                "Z405OL-6905",
//                "AMAZRT-66420",
//                "ZAPPRT-3290",
//                "AMAZRT-87754",
//                "ZAPPRT-3289",
//                "WHFMRT-9111",
//                "Z405OL-6664",
//                "Z405OL-7397",
//                "Z68JCO-2213",
//                "AMAZRT-80636",
//                "Z405OL-8804",
//                "Z405OL-7615",
//                "Z68JCO-2212",
//                "AMAZRT-80536",
//                "Z68JCO-1035",
//                "Z68JCO-2104",
//                "Z68JCO-2214",
//                "AMAZRT-78659",
//                "Z405OL-7436",
//                "Z405OL-7449",
//                "AUDIOL-13842",
//                "AMAZRT-83419",
//                "AUDIOL-13533",
//                "AMAZRT-87547",
//                "AUDIOL-14088",
//                "AMAZRT-87236",
//                "AMAZRT-88545",
//                "Z68JCO-2207",
//                "AMAZRT-78120",
//                "Z405OL-7521",
//                "AUDIOL-13618",
//                "AMAZRT-83806",
//                "Z405OL-9804",
//                "Z405OL-7920",
//                "AMAZRT-85529",
//                "Z405OL-8766",
//                "Z68JCO-2288",
//                "AMAZRT-84425",
//                "AMAZRT-89705",
//                "Z405OL-0755",
//                "Z68JCO-2514",
//                "AMAZRT-84097",
//                "AMAZRT-85045",
//                "Z405OL-9191",
//                "Z68JCO-2282",
//                "AMAZRT-88491",
//                "Z405OL-8234",
//                "Z405OL-8300",
//                "Z68JCO-2284",
//                "Z405OL-9632",
//                "AUDIOL-14566",
//                "AUDIOL-14565",
//                "Z405OL-11441",
//                "Z405OL-11326",
//                "AMAZRT-91172",
//                "AMAZRT-91491",
//                "Z405OL-11603",
//                "AMAZRT-92229",
//                "AMAZRT-92867",
//                "WHFMRT-10027",
//                "Z405OL-11095",
//                "AUDIOL-14835",
//                "AMAZRT-94287",
//                "Z405OL-9984",
//                "AMAZRT-94800",
//                "AMAZRT-93037",
//                "Z405OL-10563",
//                "Z405OL-11576",
//                "AUDIOL-14635",
//                "AMAZRT-92391",
//                "AMAZRT-92392",
//                "AMAZRT-92915",
//                "AMAZRT-93927",
//                "AMAZRT-92231",
//                "AMAZRT-94898",
//                "AMAZRT-94336",
//                "AMAZRT-92873",
//                "AMAZRT-94061",
//                "Z405OL-11503",
//                "Z405OL-11346",
//                "Z405OL-10379",
//                "Z405OL-11303",
//                "Z405OL-10613",
//                "AMAZRT-93539",
//                "Z405OL-10383",
//                "Z405OL-11990",
//                "AMAZRT-94105",
//                "Z405OL-11994",
//                "AUDIOL-14696",
//                "AUDIOL-14773",
//                "Z405OL-11991",
//                "AMAZRT-94791",
//                "AMAZRT-93549",
//                "AMAZRT-94957",
//                "AMAZRT-93548",
//                "Z405OL-10153",
//                "AMAZRT-93700",
//                "WHFMRT-10017",
//                "Z405OL-11361"};
}
