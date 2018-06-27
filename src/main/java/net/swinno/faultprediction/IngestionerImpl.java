package net.swinno.faultprediction;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;

import java.io.File;

public class IngestionerImpl<T> implements Ingestioner {

    private Encoder<T> dataEncoder;
    private Dataset<T> ds;
    private SparkSession spark;
    private DataSource dataSource;

    public IngestionerImpl(SparkSession spark, Class dataClass, DataSource dataSource) {

        this.spark = spark;
        this.dataSource = dataSource;
        setEncoder(dataClass);
    }

    public boolean examData(DataBucket data) {
        return false;
    }

    private void setEncoder(Class dataClass) {
        dataEncoder = Encoders.bean(dataClass);
    }

    public void processData() {

        if(dataSource.getDataType() == "file") {

            FileDataSource fileDataSource = (FileDataSource) dataSource;
            loadData(fileDataSource.getFileDir());

        } else if(dataSource.getDataType() == "database") {


        }

    }

    private void loadData(String fileDir) {
        File folder = new File(fileDir);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {

            ds.union(loadDataFromFile(fileDir+"\\"+listOfFiles[i].getName()));

        }
    }

    private Dataset<T> loadDataFromFile(String filePath) {
        Dataset<T> data = spark.read().json(filePath).as(dataEncoder);
        return data;
    }

    public Dataset<T> getData() {
        return ds;
    }
}
