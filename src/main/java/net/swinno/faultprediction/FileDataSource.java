package net.swinno.faultprediction;

public class FileDataSource extends DataSource {

    private int fileType;
    public static int FILE_TYPE_JSON = 1;
    public static int FILE_TYPE_XML = 2;
    public static int FILE_TYPE_PLAIN = 3;

    private String fileDir;

    public String getFileDir() {
        return fileDir;
    }

    public int getFileType() {

        return fileType;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }
}
