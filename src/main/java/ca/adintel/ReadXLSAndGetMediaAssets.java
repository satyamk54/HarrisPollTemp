package ca.adintel;

import java.util.LinkedList;
import java.util.List;

public class ReadXLSAndGetMediaAssets {

    private String xlsFileName;

    private List<String> assetsToDownload;

    public void setXLSName(String filename) {
        xlsFileName = filename;
    }

    public void process() {
        assetsToDownload = new LinkedList<>();

    }
}
