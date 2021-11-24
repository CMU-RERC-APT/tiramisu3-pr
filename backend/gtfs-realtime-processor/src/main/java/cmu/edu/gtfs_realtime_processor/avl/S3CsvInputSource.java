package cmu.edu.gtfs_realtime_processor.avl;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

import org.onebusaway.csv_entities.CsvInputSource;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class S3CsvInputSource implements CsvInputSource {

    private String s3Bucket;
    private String s3Folder;
    private AmazonS3 s3Client;

    public S3CsvInputSource(String gtfsVersionJson) {
        s3Client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());
        JSONObject gtfsVersion = new JSONObject(gtfsVersionJson);
        s3Bucket = gtfsVersion.getString("s3_bucket");
        s3Folder = gtfsVersion.getString("s3_folder");

	System.out.println(s3Bucket);
	System.out.println(s3Folder);
    }

    public boolean hasResource(String objectName) throws IOException {
        return s3Client.doesObjectExist(s3Bucket, s3Folder + objectName);
    }

    public InputStream getResource(String objectName) throws IOException {
        S3Object object = s3Client.getObject(new GetObjectRequest(s3Bucket, s3Folder + objectName));
        return object.getObjectContent();
    }

    public void close() throws IOException {
        
    }
}
