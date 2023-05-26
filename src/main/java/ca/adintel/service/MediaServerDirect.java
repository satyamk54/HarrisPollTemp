package ca.adintel.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class MediaServerDirect {

    String SERVER_PREFIX = "ssavinov@10.100.91.144:";
    String DST_FOLDER = ".//tmp//";

    HashMap<String,String[]> mediaTypeToFolder = new HashMap<>();
    HashMap<String,String[]> mediaTypeToFolderThumbnails = new HashMap<>();
    final String [] extensionsArray = {"JPG", "JPEG", "PNG", "GIF", "MPEG", "MOV", "PDF","MP3","MP4","MPG","BMP"};

    public MediaServerDirect() {
        //mnt/nfs/vir_jpg/A/ACL_DIR/ACLUAS-0043.JPG

        //folder pre is for preview
        mediaTypeToFolder.put("R", new String[]{"radio_mp3"});

        mediaTypeToFolder.put("B", new String[]{"mob_landing","mob_ads"});
        mediaTypeToFolderThumbnails.put("B", new String[]{"mob_jpg","mob_jpg_hq"});//mobile

        //
        mediaTypeToFolder.put("S", new String[]{"vir_ads","vir_mov"});//vir_pre
        mediaTypeToFolderThumbnails.put("S", new String[]{"vir_jpg","vir_jpg_hq"});//social

        mediaTypeToFolder.put("P", new String[]{"print_jpg","print_jpg_hq"});

        //Unclear web_asset_delivery website_user_data website_user_dirs_database1

        mediaTypeToFolder.put("T", new String[]{"tv_mpg","tv_mov"});//,"tv_pre"
        mediaTypeToFolderThumbnails.put("T", new String[]{"tv_jpg","tv_jpg_hq"});//tv

        mediaTypeToFolder.put("O", new String[]{"onl_ads","onl_mov","onl_landing"});//,"onl_pre"
        mediaTypeToFolderThumbnails.put("O", new String[]{"onl_jpg","onl_jpg_hq"});//online display

        mediaTypeToFolder.put("D", new String[]{"dm_pdf"});//DirectMail
        mediaTypeToFolderThumbnails.put("D", new String[]{"dm_jpg","dm_jpg_hq"});//DirectMail

        mediaTypeToFolder.put("M", new String[]{"ema_landing","ema_ads"});//Opt In Email
        mediaTypeToFolderThumbnails.put("M", new String[]{"ema_jpg","ema_jpg_hq"});//Opt In Email

        mediaTypeToFolder.put("X", new String[]{"odr_ads"});//Outdoor
        mediaTypeToFolderThumbnails.put("X", new String[]{"odr_jpg","odr_jpg_hq"});//Outdoor

        mediaTypeToFolder.put("C", new String[]{"cnm_pre","cnm_mov","cnm_mpg"});//Cinema
        mediaTypeToFolderThumbnails.put("C", new String[]{"cnm_jpg_hq","cnm_jpg"});//Cinema

        mediaTypeToFolder.put("E", new String[]{"onv_ads","onv_pre","onv_landing","onv_mov_new_format"});//Online Video
        mediaTypeToFolderThumbnails.put("E", new String[]{"onv_jpg","onv_jpg_hq"});//Online Video
    }

    private String[] makeFilePath(String adId,String mediaType) {
        String [] paths = mediaTypeToFolder.get(mediaType.toUpperCase());
        String [] result = new String[paths.length];
        for(int i=0;i<paths.length;i++){
            result[i] = "/mnt/nfs/"+paths[i]+"/"+adId.substring(0,1).toUpperCase()+"/"+adId.substring(0,3).toUpperCase()+"_DIR/"+adId.toUpperCase();
        }
        return result;
    }

    public String createFileName(String adId, String extension) {
        return adId.toUpperCase()+"."+extension;
    }

    public String copyFileWithExtension(String adId,String mediaType, String extension) {
        String [] getPaths = makeFilePath(adId,mediaType);

        for(String s:getPaths) {
            String command = "scp " +
                    SERVER_PREFIX + s + "." + extension + " "
                    + DST_FOLDER + createFileName(adId, extension);
            try {
                Process p = Runtime.getRuntime().exec(command);
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            File file = new File(DST_FOLDER + createFileName(adId, extension));
            if (file.exists())
                return file.getName();
        }

        return null;
    }

    public String copyFile(String adId,String mediaType) {
        for (String ext:extensionsArray) {
            String s = copyFileWithExtension(adId,mediaType,ext);
            if (s!=null)
                return s;
        }
        return null;
    }
}
