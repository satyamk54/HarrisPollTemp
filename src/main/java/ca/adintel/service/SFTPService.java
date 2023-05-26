package ca.adintel.service;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResource;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
public class SFTPService {

    public SSHClient sshClient = null;
    public SFTPClient sftpClient = null;
    final static int badSize = 5294;

    public SFTPService() {
        try {
            if (sshClient == null)
                setupSshj();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    SSHClient setupSshj() throws IOException {
        sshClient = new SSHClient();
        sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        sshClient.connect("sftp.adintel.numerator.com");
        sshClient.authPublickey("satyam.kaushik","/tmp/id_ed25519");
        sshClient.setConnectTimeout(800000000);
        sshClient.setTimeout(800000000);
        sftpClient = sshClient.newSFTPClient();
        sftpClient.getFileTransfer().setPreserveAttributes(false);
        return sshClient;
    }

    public void downloadFile(String remoteSource, String localTarget) {
        try {
            if (sshClient == null)
                setupSshj();
        } catch (Exception ex){
            ex.printStackTrace();
        }
        try {
            sftpClient.get(remoteSource,localTarget);
        } catch (IOException e) {
            try {
                sftpClient.get(remoteSource,localTarget);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public boolean doFileExistsInFTP(String targetFileName) {
        try {
            if (sshClient == null)
                setupSshj();
        } catch (Exception ex){
            ex.printStackTrace();
        }

        SFTPClient sftpClient = null;

        try {

            sftpClient = sshClient.newSFTPClient();
            sftpClient.getFileTransfer().setPreserveAttributes(false);
            int delimiter = targetFileName.lastIndexOf("/");
            String targetFileFolder = targetFileName.substring(0, delimiter);
            String fileName = targetFileName.substring(delimiter + 1);
            List<RemoteResourceInfo> ls = sftpClient.ls(targetFileFolder);
            for (RemoteResourceInfo remoteFile : ls) {
                if (fileName.equalsIgnoreCase(remoteFile.getName())) {
                    sftpClient.close();
                    return true;
                }
            }
            sftpClient.close();
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            if (sftpClient!=null) {
                try {
                    sftpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

    public void uploadFileToSFTP(String sourceFilename, String targetFileName) throws IOException {

        setupSshj();

//        SFTPClient sftpClient = sshClient.newSFTPClient();
//        sftpClient.getFileTransfer().setPreserveAttributes(false);

        String localFile = sourceFilename;
        String remoteDir = "./";

        sftpClient.put(localFile, targetFileName);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        sftpClient.close();
    }

    public void uploadMasterListToSFTP(String sourceFilename, String targetFileName) throws IOException {

        setupSshj();

//        SFTPClient sftpClient = sshClient.newSFTPClient();
//        sftpClient.getFileTransfer().setPreserveAttributes(false);

        String localFile = sourceFilename;
        String remoteDir = "./";

        sftpClient.put(localFile, targetFileName);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        sftpClient.close();
    }


    public Map<String,String>  listAllFiles(int year) {
        if (sshClient==null) {
            try {
                setupSshj();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Map<String,String> result = new HashMap<>();
        String targetFolder = "/INTL/"+year+"/"+year+"0101/";
        try {
            List<RemoteResourceInfo> ls = sftpClient.ls(targetFolder);
            for(RemoteResourceInfo rri:ls){
                if (rri.isDirectory()) {
                    String targetFolder2 = rri.getPath();
                    List<RemoteResourceInfo> level2 = sftpClient.ls(targetFolder2);
                    for(RemoteResourceInfo rri2:level2) {
                        if (rri2.isDirectory()) {
                            String targetFolder3 = rri2.getPath();
                            List<RemoteResourceInfo> level3 = sftpClient.ls(targetFolder3);
                            if (rri.isDirectory()) {
                                for (RemoteResourceInfo rri3 : level3) {
                                    String fileName = rri3.getName();
                                    String adcode = fileName.substring(0, fileName.indexOf('_'));
                                    result.put(adcode, rri3.getPath());
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Map<String,String>  listAllBadFilesUS(int year) {
        if (sshClient==null) {
            try {
                setupSshj();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Map<String,String> result = new HashMap<>();
        String targetFolder = "/US/"+year+"/"+year+"0101/";
        try {
            List<RemoteResourceInfo> ls = sftpClient.ls(targetFolder);
            for(RemoteResourceInfo rri:ls){
                if (rri.isDirectory()) {
                    String targetFolder2 = rri.getPath();
                    List<RemoteResourceInfo> level2 = sftpClient.ls(targetFolder2);
                    for(RemoteResourceInfo rri2:level2) {
                        if (rri2.isDirectory()) {
                            String targetFolder3 = rri2.getPath();
                            List<RemoteResourceInfo> level3 = sftpClient.ls(targetFolder3);
                            if (rri.isDirectory()) {
                                for (RemoteResourceInfo rri3 : level3) {
                                    String fileName = rri3.getName();
                                    String adcode = fileName.substring(0, fileName.indexOf('_'));
                                    if (rri3.getAttributes().getSize()==badSize){
                                        result.put(adcode, rri3.getPath());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Map<String,String>  listAllFilesUS(int year) {
        if (sshClient==null) {
            try {
                setupSshj();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Map<String,String> result = new HashMap<>();
        String targetFolder = "/US/"+year+"/"+year+"0101/";
        try {
            List<RemoteResourceInfo> ls = sftpClient.ls(targetFolder);
            for(RemoteResourceInfo rri:ls){
                if (rri.isDirectory()) {
                    String targetFolder2 = rri.getPath();
                    List<RemoteResourceInfo> level2 = sftpClient.ls(targetFolder2);
                    for(RemoteResourceInfo rri2:level2) {
                        if (rri2.isDirectory()) {
                            String targetFolder3 = rri2.getPath();
                            List<RemoteResourceInfo> level3 = sftpClient.ls(targetFolder3);
                            if (rri.isDirectory()) {
                                for (RemoteResourceInfo rri3 : level3) {
                                    String fileName = rri3.getName();
                                    String adcode = fileName.substring(0, fileName.indexOf('_'));
                                    result.put(adcode, rri3.getPath());
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean deleteRemoteFile(String path) {
        try {
            sftpClient.rm(path);
        } catch (IOException e) {

            e.printStackTrace();
            return false;
        }
        return true;
    }

    ///INTL/2019/20190101
    List<String> getAllXLSXFilesINTL(){
        if (sshClient==null) {
            try {
                setupSshj();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<String> result = new LinkedList<>();
        String files2019 = "/INTL/2019/20190101";
        String files2020 = "/INTL/2020/20200101";
        String files2021 = "/INTL/2021/20210101";
//        String files2022 = "/INTL/2022/20220101";
        String filesCurrent = "/INTL/2022/";
        String filesCurrent2 = "/INTL/2023/";
        try{
            List<RemoteResourceInfo> ls = sftpClient.ls(files2019);
            for(RemoteResourceInfo rri:ls){
                if (rri.isDirectory()) continue;
                if (rri.getName().toLowerCase().endsWith(".xlsx")) {
                    result.add(rri.getPath());//add name? no
                }
            }
            ls = sftpClient.ls(files2020);
            for(RemoteResourceInfo rri:ls){
                if (rri.isDirectory()) continue;
                if (rri.getName().toLowerCase().endsWith(".xlsx")) {
                    result.add(rri.getPath());//add name? no
                }
            }
            ls = sftpClient.ls(files2021);
            for(RemoteResourceInfo rri:ls){
                if (rri.isDirectory()) continue;
                if (rri.getName().toLowerCase().endsWith(".xlsx")) {
                    result.add(rri.getPath());//add name? no
                }
            }

            List<RemoteResourceInfo> list = sftpClient.ls(filesCurrent);
            for(RemoteResourceInfo rrim:list) {
                if (rrim.isDirectory()) {
                    ls = sftpClient.ls(rrim.getPath());
                    for (RemoteResourceInfo rri : ls) {
                        if (rri.isDirectory()) continue;
                        if (rri.getName().toLowerCase().endsWith(".xlsx")) {
                            result.add(rri.getPath());//add name? no
                        }
                    }
                }
            }
            List<RemoteResourceInfo> list2 = sftpClient.ls(filesCurrent2);
            for(RemoteResourceInfo rrim:list2) {
                if (rrim.isDirectory()) {
                    ls = sftpClient.ls(rrim.getPath());
                    for (RemoteResourceInfo rri : ls) {
                        if (rri.isDirectory()) continue;
                        if (rri.getName().toLowerCase().endsWith(".xlsx")) {
                            result.add(rri.getPath());//add name? no
                        }
                    }
                }
            }
         } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    public Map<String,Map<String,Map<String,List<String>>>>  listFilesByMedia(int year, boolean adcodeOnly) {
        if (sshClient == null) {
            try {
                setupSshj();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Map<String, String> allFiles = new HashMap<>();
        Map<String, Map<String, Map<String, List<String>>>> result = new HashMap<>();
        String targetParentFolder = "/INTL/" + year + "/";
        try {
            List<RemoteResourceInfo> parentRRI = sftpClient.ls(targetParentFolder);

            for (RemoteResourceInfo targetFolderRRI:parentRRI) {
                if (!targetFolderRRI.isDirectory()) continue;

                String targetFolder = targetFolderRRI.getPath();
                try {
                    List<RemoteResourceInfo> ls = sftpClient.ls(targetFolder);
                    for (RemoteResourceInfo rri : ls) {
                        if (rri.isDirectory()) {
                            Map<String, Map<String, List<String>>> mediaMap = new HashMap<>();


                            String targetFolder2 = rri.getPath();
                            List<RemoteResourceInfo> level2 = sftpClient.ls(targetFolder2);
                            for (RemoteResourceInfo rri2 : level2) {
                                if (rri2.isDirectory()) {
                                    Map<String, List<String>> country = new HashMap<>();
                                    mediaMap.put(rri2.getName(), country);
                                    String targetFolder3 = rri2.getPath();
                                    if (rri.getName().equalsIgnoreCase("internet_display") && rri2.getName().equalsIgnoreCase("italy")) {
                                        int a = 0;
                                        a++;
                                    }
                                    List<RemoteResourceInfo> level3 = sftpClient.ls(targetFolder3);
                                    if (rri.isDirectory()) {
                                        for (RemoteResourceInfo rri3 : level3) {


                                            String fileName = rri3.getName();
                                            String adcode = fileName.substring(0, fileName.indexOf('_'));
                                            String key = rri.getName() + "_" + rri2.getName() + "_" + adcode;
//                                            if (files.containsKey(key)) {
//                                                System.out.println("duplicate of creative:" + key + ";" + result.get(key) + ";" + rri3.getPath());
//                                            }
//                                            if (allFiles.containsKey(adcode)) {
//                                                System.out.println("duplicate of creative between medias:" + adcode + ";" + result.get(key) + ";" + rri3.getPath());
//                                            }
                                            if (adcodeOnly) {
                                                List<String> fileNames = country.get(adcode);
                                                if(fileNames==null) {
                                                    fileNames = new LinkedList<>();
                                                    country.put(adcode,fileNames);
                                                }
                                                fileNames.add(rri3.getPath());

                                            } else {
                                                List<String> fileNames = country.get(key);
                                                if(fileNames==null) {
                                                    fileNames = new LinkedList<>();
                                                    country.put(adcode,fileNames);
                                                }
                                                fileNames.add(rri3.getPath());

                                            }
                                            allFiles.put(adcode, rri3.getPath());
                                        }
                                    }
                                }
                            }
                            if (!result.containsKey(rri.getName())) {
                                result.put(rri.getName(), mediaMap);
                            } else {
                                Map<String, Map<String, List<String>>> previousMapMedia = result.get(rri.getName());
                                for(String s:mediaMap.keySet()){
                                    if (previousMapMedia.containsKey(s)){
                                        Map<String, List<String>> countryMap = previousMapMedia.get(s);
                                        Map<String, List<String>> newCountryMap = mediaMap.get(s);
                                        for(String k:newCountryMap.keySet()){
                                            if (!countryMap.containsKey(k)) {
                                                countryMap.put(k,newCountryMap.get(k));
                                            } else {
                                                countryMap.get(k).addAll(newCountryMap.get(k));
                                            }
                                        }
                                    } else {
                                        previousMapMedia.put(s,mediaMap.get(s));
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }
}
