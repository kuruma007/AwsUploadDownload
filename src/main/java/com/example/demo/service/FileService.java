package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
public interface FileService {

    void uploadFile(MultipartFile file) throws IOException;

    byte[] downloadFile(String fileName);
    List<String> listOfAllFiles();
}
