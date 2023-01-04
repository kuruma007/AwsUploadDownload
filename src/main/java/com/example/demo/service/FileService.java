package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
public interface FileService {

    void uploadFile(MultipartFile file);

    byte[] downloadFile(String fileName);
    List<String> listOfAllFiles();
}
