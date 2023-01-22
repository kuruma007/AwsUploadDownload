package com.example.demo.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;


@Service
public class FileServiceImpl implements FileService{

    @Value("${aws.bucket.bucketName}")
    private String bucketName;

    private final AmazonS3 amazonS3;

    public FileServiceImpl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public void uploadFile(MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        try{
            File file = convertMultiPart(multipartFile);
            String name = amazonS3.getObject(bucketName,fileName).getKey();
            if(!Objects.equals(name, "")){
                Date date = new Date();
                String newFileName = fileName + " " + date;
                PutObjectResult putObjectResult = amazonS3.putObject(bucketName,newFileName, file);
                putObjectResult.getContentMd5();
            }
            else{
                PutObjectResult putObjectResult = amazonS3.putObject(bucketName,fileName, file);
                putObjectResult.getContentMd5();
            }

        }catch(IOException exception){
            throw new RuntimeException(exception);
        } catch (AmazonServiceException amazonServiceException){
            System.out.println(amazonServiceException.getErrorMessage());
            throw new AmazonServiceException(amazonServiceException.getErrorCode());
        }catch (AmazonClientException amazonClientException){
            throw new AmazonClientException(amazonClientException.getMessage());
        }

    }

    @Override
    public byte[] downloadFile(String fileName){
        S3Object s3Object = amazonS3.getObject(bucketName, fileName);
        S3ObjectInputStream objectContent = s3Object.getObjectContent();
        try{
            return IOUtils.toByteArray(objectContent);
        }catch (IOException exception){
            throw new RuntimeException(exception);
        }
    }

    @Override
    public List<String> listOfAllFiles() {
        HashMap<String, Date> response = new HashMap<>();
        List<S3ObjectSummary> s3ObjectSummaries;

        ListObjectsV2Result listObjectsV2Result = amazonS3.listObjectsV2(bucketName);
        s3ObjectSummaries = listObjectsV2Result.getObjectSummaries();

        for(S3ObjectSummary s3ObjectSummary : s3ObjectSummaries){
            String key = s3ObjectSummary.getKey();
            Date date = s3ObjectSummary.getLastModified();
            response.put(key, date);
        }

        HashMap<String, Date> hashMap = sortByValue(response);
        List<String> list = new ArrayList<>(hashMap.keySet());
        Collections.reverse(list);
        return list;
    }

    private File convertMultiPart(MultipartFile multipartFile) throws IOException{
        File convertFile = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fileOutputStream = new FileOutputStream(convertFile);
        fileOutputStream.write(multipartFile.getBytes());
        fileOutputStream.close();
        return convertFile;
    }

    public static HashMap<String, Date> sortByValue(HashMap<String, Date> hm)
    {
        List<Map.Entry<String, Date> > list =
                new LinkedList<>(hm.entrySet());

        list.sort(Map.Entry.comparingByValue());
        HashMap<String, Date> temp = new LinkedHashMap<>();

        for (Map.Entry<String, Date> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }

        return temp;
    }
}
