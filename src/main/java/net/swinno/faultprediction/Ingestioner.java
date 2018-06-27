package net.swinno.faultprediction;

public interface Ingestioner {
    public void processData();
    public boolean examData(DataBucket data);
}
