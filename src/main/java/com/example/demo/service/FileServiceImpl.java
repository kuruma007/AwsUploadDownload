package com.example.demo.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Value("${aws.bucket.bucketName}")
    private String bucketName;

    private final AmazonS3 amazonS3;

    public FileServiceImpl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public void uploadFile(MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        File file = convertMultiPart(multipartFile);
        try {
            Path filePath = Path.of(file.getPath());
            Date date = new Date();
            String newFileName = fileName + "~" + date;

            PutObjectResult putObjectResult = amazonS3.putObject(bucketName, newFileName, file);
            putObjectResult.getContentMd5();

            Files.deleteIfExists(filePath);
        } catch (AmazonServiceException amazonServiceException) {
            log.info(amazonServiceException.getErrorMessage());
            throw new AmazonServiceException(amazonServiceException.getErrorCode());
        } catch (AmazonClientException amazonClientException) {
            throw new AmazonClientException(amazonClientException.getMessage());
        }
    }

    @Override
    public byte[] downloadFile(String fileName) {
        ObjectListing files = amazonS3.listObjects(bucketName);
        List<S3ObjectSummary> uploadedFiles = files.getObjectSummaries();
        List<String> filesInS3Bucket = uploadedFiles.stream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
        String fileToBeDownloaded = filesInS3Bucket.stream().filter(file -> file.contains(fileName)).findFirst().get();
        try {
            S3Object s3Object = amazonS3.getObject(bucketName, fileToBeDownloaded);
            S3ObjectInputStream objectContent = s3Object.getObjectContent();

            return IOUtils.toByteArray(objectContent);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public List<String> listOfAllFiles() {new LinkedHashMap<>();
        List<S3ObjectSummary> s3ObjectSummaries;
        ListObjectsV2Result listObjectsV2Result = amazonS3.listObjectsV2(bucketName);
        s3ObjectSummaries = listObjectsV2Result.getObjectSummaries();

        ArrayList<String> files = new ArrayList<>();
        for (S3ObjectSummary s3ObjectSummary : s3ObjectSummaries) {
            String key = s3ObjectSummary.getKey();
            String fileName = key.replaceFirst("~.*", "");
            files.add(fileName);
        }
        Collections.reverse(files);

        return files;
    }

    private File convertMultiPart(MultipartFile multipartFile) throws IOException {
        File convertFile = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fileOutputStream = new FileOutputStream(convertFile);
        fileOutputStream.write(multipartFile.getBytes());
        fileOutputStream.close();

        return convertFile;
    }

    public static HashMap<String, Date> sortByValue(HashMap<String, Date> hm) {
        List<Map.Entry<String, Date>> list =
                new LinkedList<>(hm.entrySet());

        list.sort(Map.Entry.comparingByValue());
        HashMap<String, Date> temp = new LinkedHashMap<>();

        for (Map.Entry<String, Date> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }

        return temp;
    }
}
