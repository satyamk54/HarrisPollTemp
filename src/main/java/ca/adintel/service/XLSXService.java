package ca.adintel.service;


import ca.adintel.Pair;
import ca.adintel.service.Neilsen.CreativeDTO;
import ca.adintel.service.US.UsCreativeDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ALL")
public class XLSXService {


    public void createNewXlsxFilesWithCreativesBacklog(String workbookLocation, List<CreativeDTO> data) {
        FileOutputStream saveExcel = null;
        try {
            saveExcel = new FileOutputStream(workbookLocation);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Workbook workbook = null;
        workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        int rownum = 0;
        int cnt = 0;

        Row namesRow = sheet.createRow(rownum++);
        sheet.createFreezePane(0,1,0,1);

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);

        CellStyle cellStyle =workbook.createCellStyle();
        cellStyle.setFont(font);

        createAndSetStyle(namesRow,"Creative ID",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Advertiser",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Brand",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Category",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Channel",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"First Recorded Date",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Length",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Media",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Ad Type",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Country",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Title",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Description",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Source",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Target",cnt++,font,cellStyle);

        for(CreativeDTO dto:data) {
            cnt = 0;
            Row row = sheet.createRow(rownum++);
            row.createCell(cnt++).setCellValue(dto.CreativeID);
            row.createCell(cnt++).setCellValue(dto.Advertiser);
            row.createCell(cnt++).setCellValue(dto.Brand);
            row.createCell(cnt++).setCellValue(Stream.of(dto.Category)
                    .collect(Collectors.joining(" | ")));
            row.createCell(cnt++).setCellValue(dto.InitialChannel);
            row.createCell(cnt++).setCellValue(dto.TransmissionDate);
            row.createCell(cnt++).setCellValue(dto.Length);
            row.createCell(cnt++).setCellValue(dto.Media);
            row.createCell(cnt++).setCellValue(dto.AdType);
            row.createCell(cnt++).setCellValue(dto.Country);
            row.createCell(cnt++).setCellValue(dto.Title);
            row.createCell(cnt++).setCellValue(dto.Description);
            row.createCell(cnt++).setCellValue(dto.sourceURL);
            row.createCell(cnt++).setCellValue(dto.targetFileName);
        }

        //Brand	Sub Brand	Category	Channel/Publication	First Recorded Date	Last Recorded Date	Length/File(s)	Media	Ad Type	Country	Title	Description	Endline


        try {
            workbook.write(saveExcel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void mergeINTLFiles(String workbookLocation) {

        FileOutputStream saveExcel = null;
        try {
            saveExcel = new FileOutputStream(workbookLocation);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        XLSXService xlsxService = new XLSXService();
        SFTPService sftpService = new SFTPService();

        Map<String,Map<String,Map<String,List<String>>>>[] fileMaps = new Map[5];

        fileMaps[4] = sftpService.listFilesByMedia(2023,true);
        fileMaps[3] = sftpService.listFilesByMedia(2022,true);
        fileMaps[0] = sftpService.listFilesByMedia(2019,true);
        fileMaps[1] = sftpService.listFilesByMedia(2020,true);
        fileMaps[2] = sftpService.listFilesByMedia(2021,true);


        Map<String,List<String>> listAllFiles = new HashMap<>();

        for(int year=2023;year>=2019;year--){
            Map<String,Map<String,Map<String,List<String>>>> fileMapByYear = fileMaps[year-2019];
            for(String mediaType:fileMapByYear.keySet()){
                Map<String,Map<String,List<String>>> fileMapByMedia = fileMapByYear.get(mediaType);
                for(String country:fileMapByMedia.keySet()){
                    Map<String,List<String>> fileMapByCountry = fileMapByMedia.get(country);
                    for(String adcode:fileMapByCountry.keySet()) {
                        List<String> fileName = fileMapByCountry.get(adcode);
                        List<String> fileNames = listAllFiles.get(adcode);
                        if(fileNames==null) {
                            fileNames = new LinkedList<>();
                            listAllFiles.put(adcode,fileNames);
                        }
                        fileNames.addAll(fileName);
                    }
                }
            }
        }
        List<String> xlsxFiles = sftpService.getAllXLSXFilesINTL();

        Workbook workbook = null;
        workbook = new XSSFWorkbook();
        Sheet writeSheet = workbook.createSheet();

        int rownum=0;

        Row namesRow = writeSheet.createRow(rownum++);
        writeSheet.createFreezePane(0,1,0,1);

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);

        CellStyle cellStyle =workbook.createCellStyle();
        cellStyle.setFont(font);

        int cnt = 0;



        createAndSetStyle(namesRow,"Creative ID",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Advertiser",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Brand",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Category",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Channel",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"First Run Year-Month-Date",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Length",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Media",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Ad Type",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Country",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Title",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Description",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"File Name",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"File Location",cnt++,font,cellStyle);//

        int fileCounter = 0;
        int adcodeCounter = 0;

        CellStyle cellStyleDate = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        cellStyleDate.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));//2021-10-19
        SimpleDateFormat formatterDate=new SimpleDateFormat("yyyy-MM-dd", Locale.US);
//        cellStyleDate.set

        for(String s:xlsxFiles) {
            if (!s.contains("Italy")){
                String filemName = s.substring(s.lastIndexOf("/")+1);
                String localName = "./tmp/"+filemName;
                sftpService.downloadFile(s,localName);
                File f = new File(localName);

                Workbook loadedWorkbook = null;
                try {
                    loadedWorkbook = new XSSFWorkbook(f.getAbsolutePath());
                    Sheet sheet = loadedWorkbook.getSheetAt(0);
                    int count = sheet.getPhysicalNumberOfRows();
                    int cellNums = sheet.getRow(0).getLastCellNum();
                    String [][] strings = new String[count-1][];
                    for(int i=1;i<count;i++){//for each adcode
                        Row row = sheet.getRow(i);
                        strings[i-1] = new String[14+1];//filename
                        for(int j=0;j<cellNums;j++){
                            strings[i-1][j] = row.getCell(j).getStringCellValue();
                        }
                        String adCode = strings[i-1][0];
                        List<String> filePaths = listAllFiles.get(adCode);
                        if (filePaths!=null)
                            fileCounter+=filePaths.size();
                        adcodeCounter++;

                        List<String> fileNames = new LinkedList<>();
                        String res = "";
                        String fName = "";
                        if (filePaths!=null) {
                            if (filePaths.size()==1){
                                res += filePaths.get(0);
                                fName = res.substring(res.lastIndexOf("/") + 1);
                            } else {
                                for (String k : filePaths) {
                                    res += k + ";";
                                    fName += k.substring(k.lastIndexOf("/") + 1)+";";
                                }
                            }
                        }
                        strings[i-1][12] = fName;
                        strings[i-1][13] = res;

                        Row dataRow = writeSheet.createRow(rownum++);
                        for(int k=0;k<strings[i-1].length;k++){
                            dataRow.createCell(k);
                            if (k!=5) { //First Recorded Date
                                dataRow.getCell(k).setCellValue(strings[i - 1][k]);
                            } else {
//                            dataRow.getCell(k).setCellType();
                                String val = strings[i - 1][k];
                                if (val!=null&&!val.isEmpty()&&val.contains("T")) {
                                    val = val.substring(0, val.indexOf("T"));
                                }
//                            dataRow.getCell(k).setCellValue(val);
                                try {
                                    dataRow.getCell(k).setCellValue(formatterDate.parse(val));
                                } catch (Exception ex) {
                                    dataRow.getCell(k).setCellValue(val);
                                }
                                dataRow.getCell(k).setCellStyle(cellStyleDate);
                            }

                        }
                    }

                    loadedWorkbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        writeSheet.setAutoFilter(new CellRangeAddress(0, writeSheet.getLastRowNum(), 0, cnt));

        System.out.println("Processed:"+xlsxFiles+" xlsx files of:"+xlsxFiles.size());
        System.out.println("Processed:"+fileCounter+" creatives, "+adcodeCounter+" adcodes, "+listAllFiles.size()+" of all files");
//        System.exit(0);

        try {
            workbook.write(saveExcel);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.exit(0);
    }

    public void createNewXlsxFiles(String workbookLocation, List<CreativeDTO> data, boolean addURLS) {
        FileOutputStream saveExcel = null;
        try {
            saveExcel = new FileOutputStream(workbookLocation);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Workbook workbook = null;
        workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        int rownum = 0;
        int cnt = 0;

        Row namesRow = sheet.createRow(rownum++);
        sheet.createFreezePane(0,1,0,1);

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);

        CellStyle cellStyle =workbook.createCellStyle();
        cellStyle.setFont(font);

        createAndSetStyle(namesRow,"Creative ID",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Advertiser",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Brand",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Category",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Channel",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"First Recorded Date",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Length",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Media",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Ad Type",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Country",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Title",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Description",cnt++,font,cellStyle);
        if (addURLS) {
            createAndSetStyle(namesRow,"Source",cnt++,font,cellStyle);
            createAndSetStyle(namesRow,"Target",cnt++,font,cellStyle);
        }

        for(CreativeDTO dto:data) {
            cnt = 0;
            Row row = sheet.createRow(rownum++);
            row.createCell(cnt++).setCellValue(dto.CreativeID);
            row.createCell(cnt++).setCellValue(dto.Advertiser);
            row.createCell(cnt++).setCellValue(dto.Brand);
            row.createCell(cnt++).setCellValue(Stream.of(dto.Category)
                    .collect(Collectors.joining(" | ")));
            row.createCell(cnt++).setCellValue(dto.InitialChannel);
            row.createCell(cnt++).setCellValue(dto.TransmissionDate);
            row.createCell(cnt++).setCellValue(dto.Length);
            row.createCell(cnt++).setCellValue(dto.Media);
            row.createCell(cnt++).setCellValue(dto.AdType);
            row.createCell(cnt++).setCellValue(dto.Country);
            row.createCell(cnt++).setCellValue(dto.Title);
            row.createCell(cnt++).setCellValue(dto.Description);
            if (addURLS) {
                row.createCell(cnt++).setCellValue(dto.sourceURL);
                row.createCell(cnt++).setCellValue(dto.targetFileName);
            }
        }

        //Brand	Sub Brand	Category	Channel/Publication	First Recorded Date	Last Recorded Date	Length/File(s)	Media	Ad Type	Country	Title	Description	Endline


        try {
            workbook.write(saveExcel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createNewXlsxFilesForUS(String workbookLocation, List<UsCreativeDTO> data) {
        FileOutputStream saveExcel = null;
        try {
            saveExcel = new FileOutputStream(workbookLocation);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Workbook workbook = null;
        workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        int rownum = 0;
        int cnt = 0;

        Row namesRow = sheet.createRow(rownum++);
        sheet.createFreezePane(0,1,0,1);

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);

        CellStyle cellStyle =workbook.createCellStyle();
        cellStyle.setFont(font);

        createAndSetStyle(namesRow,"Creative ID",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Advertiser",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Brand",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Category",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Channel",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"First Recorded Date",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Length",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Media",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Ad Type",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Country",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Title",cnt++,font,cellStyle);
        createAndSetStyle(namesRow,"Description",cnt++,font,cellStyle);

        for(UsCreativeDTO dto:data) {
            cnt = 0;
            Row row = sheet.createRow(rownum++);
//            row.createCell(cnt++).setCellValue(dto.ge);
            row.createCell(cnt++).setCellValue(dto.getAdvertiserName());
//            row.createCell(cnt++).setCellValue(dto.);
//            row.createCell(cnt++).setCellValue(Stream.of(dto.Category)
//                    .collect(Collectors.joining(" | ")));
//            row.createCell(cnt++).setCellValue(dto.InitialChannel);
//            row.createCell(cnt++).setCellValue(dto.TransmissionDate);
//            row.createCell(cnt++).setCellValue(dto.Length);
//            row.createCell(cnt++).setCellValue(dto.Media);
//            row.createCell(cnt++).setCellValue(dto.AdType);
//            row.createCell(cnt++).setCellValue(dto.Country);
//            row.createCell(cnt++).setCellValue(dto.Title);
//            row.createCell(cnt++).setCellValue(dto.Description);
        }

        try {
            workbook.write(saveExcel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Cell createAndSetStyle(Row namesRow, String columnName, int columnCounter, Font font,CellStyle cellStyle) {
        Cell cell = namesRow.createCell(columnCounter);
        cell.setCellValue(columnName);
//        CellStyle style = cell.getCellStyle();
//        style.setFont(font);
//        cell.setCellStyle(style);
        cell.setCellStyle(cellStyle);
        return cell;
    }

        public void addDataToSheet(String workbookLocationRead,String workbookLocationWrite, HashMap<String,String> headlineTextData, HashMap<String,String> firstRunUnitValue, int adCodeColumn, int entriesNum) {

            FileInputStream readExcel = null;
            try {
                readExcel = new FileInputStream(workbookLocationRead);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Workbook workbook = null;
            try {
                workbook = new XSSFWorkbook(readExcel);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream saveExcel = null;
            try {
                saveExcel = new FileOutputStream(workbookLocationWrite);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Sheet sheet = workbook.getSheetAt(0);

            Row row  = sheet.getRow(5);//columns, get that programmaticaly

            Cell cell = row.createCell(row.getLastCellNum());
            cell.setCellValue("Lead Text");
            cell.setCellStyle(row.getCell(row.getLastCellNum()-3).getCellStyle());
            cell = row.createCell(row.getLastCellNum());
            cell.setCellValue("Length");
            cell.setCellStyle(row.getCell(row.getLastCellNum()-3).getCellStyle());

            Cell cellData;



            for(int i=6;i<6+entriesNum;i++) {
                row = sheet.getRow(i);

                String adCode = row.getCell(adCodeColumn).getStringCellValue();
                cellData = row.createCell(row.getLastCellNum());
                String headline = headlineTextData.get(adCode);
                cellData.setCellValue(headline);
                cellData.setCellStyle(row.getCell(row.getLastCellNum()-3).getCellStyle());
                System.out.println("setting "+headline+" headline for:"+adCode+" row:"+i);

                cellData = row.createCell(row.getLastCellNum());
                String firstRun = firstRunUnitValue.get(adCode);
                cellData.setCellValue(firstRun);
                cellData.setCellStyle(row.getCell(row.getLastCellNum()-3).getCellStyle());
//                System.out.println("setting "+firstRun+" first run for:"+adCode+" row:"+i);
            }

            try {
                workbook.write(saveExcel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public Map<Integer, List<String>> getDataFromSheet(String workbookLocation) {
        Map<Integer, List<String>> data = new HashMap<>();
        FileInputStream fileStream = null;
        Workbook workbook = null;
        try {

            fileStream = new FileInputStream(new File(workbookLocation));

            workbook = new XSSFWorkbook(fileStream);

            Sheet sheet = workbook.getSheetAt(0);
            int i = 0;
            for (Row row : sheet) {
                data.put(i, new ArrayList<String>());
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING:
                            data.get(i).add(cell.getStringCellValue());
                            System.out.println(cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            data.get(i).add(Double.toString(cell.getNumericCellValue()));
                            System.out.println(cell.getNumericCellValue());
                            break;
                        case BOOLEAN:
                            data.get(i).add(cell.getStringCellValue());
                            System.out.println(cell.getStringCellValue());
                            break;
                        case FORMULA:
                            data.get(i).add(cell.getCellFormula());
                            System.out.println(cell.getCellFormula());
                            break;
                        default:
                            data.get(new Integer(i)).add("");
                    }


                }
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (fileStream != null)
                fileStream.close();
        }catch (Exception ex){

        }

        try {
            if (workbook != null)
                workbook.close();
        }catch (Exception ex){

        }

        System.out.println("*****DONE XLSX PRINT*****");

        return data;
    }

    public int getIdRowWithNames(Map<Integer, List<String>> data) {
        for(int i=0;i<data.keySet().size();i++) {
            List<String> row = data.get(i);
            if (row.size()<5) continue;
            boolean actionsPresent = false;
            boolean adCodePresent = false;
            boolean mediaPreset = false;
            int namesCounter = 0;
            for(int j=0;j<row.size();j++) {
                if (row.get(j).toLowerCase().startsWith("actions")) {
                    actionsPresent = true;
                }
                if (row.get(j).toLowerCase().startsWith("ad code")) {
                    adCodePresent = true;
                }
                if (row.get(j).toLowerCase().startsWith("media")) {
                    mediaPreset = true;
                }
                if (actionsPresent&&adCodePresent||actionsPresent&&mediaPreset||adCodePresent&&mediaPreset) {//2 out of 3
                    return i;
                }
            }
        }
        return -1;
    }

    public Pair<Integer> getDataRows(Map<Integer, List<String>> data, int rowWithNames) {
        for(int i=rowWithNames+1;i<data.keySet().size();i++) {
            List<String> row = data.get(i);
            if (row.size()<5) return new Pair<>(rowWithNames+1,i-1);
        }
        for(int i=rowWithNames+1;i<data.keySet().size();i++) {
            List<String> row = data.get(i);
            if (row.get(0).startsWith("Please Note:")) return new Pair<>(rowWithNames+1,i-1);
        }

        return new Pair<>(rowWithNames+1,data.keySet().size()-2);

//        return null;
    }

    public String getFileNameTo(List<String> names, List<String> row, SMARTService.ColumnarItem ci) {
        //Actions	Ad Code	Media	Parent Advertiser	Advertiser	Market	Product	First Run Date	Category	Subcategory	Day Part	Title	Recut Detail	Target	Clearance	Color	Edition	Format	Size	New/Recut	Pages Count	Celebrities
        //Ad Code,  Advertiser, Title, Size , New/Recut
        int codePosition = getColumnName(names,"Ad Code");
        int advertiserPosition = getColumnName(names,"Advertiser");
        int titlePosition = getColumnName(names,"Title");
        int headlinePosition = getColumnName(names,"headlineText");
        int sizePosition = getColumnName(names,"firstRunUnitValue");
        int newRecutPosition = getColumnName(names,"New/Recut");
        String headline = "";
        if (ci!=null&&ci.headlineText!=null) headline = ci.headlineText;
        String size = "";
        if (ci!=null&&ci.firstRunUnitValue!=null) size = ci.firstRunUnitValue;
        String result =
                row.get(codePosition)+"_"+
                row.get(advertiserPosition)+"_"+
                row.get(titlePosition)+"_"+
                headline+"_"+
                size+"_"+
                row.get(newRecutPosition);
        if (result.contains("/"))
            result = result.replace('/','-');
        return result;
    }

    public String getAdcode(List<String> names, List<String> row) {
        int codePosition = getColumnName(names,"Ad Code");
        return row.get(codePosition);
    }

    public int getAdcodePosition(List<String> names) {
        return getColumnName(names,"Ad Code");
    }

    public int getColumnName(List<String> names, String columnName){

        for(int i=0;i<names.size();i++) {
            if (names.get(i).equalsIgnoreCase(columnName)) return i;
        }
        return -1;

    }



}
