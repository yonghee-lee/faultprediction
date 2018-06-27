package net.swinno.faultprediction;

import org.json.*;
import java.io.File;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;

public class JsonDataBucket<T> implements DataBucket {

    private Encoder<T> dataEncoder;
    private Dataset<T> ds;
    private SparkSession spark;

    public JsonDataBucket(SparkSession spark, Class dataClass) {
        this.spark = spark;
        setEncoder(dataClass);
    }

    private void setEncoder(Class dataClass) {
        dataEncoder = Encoders.bean(dataClass);
    }

    public boolean saveData() {

        return false;

    }

    public Dataset<T> getDataset() {

        return ds;
    }

}
