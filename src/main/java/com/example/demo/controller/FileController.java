package com.example.demo.controller;

import com.example.demo.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class FileController {

    @Autowired
    private FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @RequestMapping("/upload")
    public String upload(){
        fileService.listOfAllFiles();
        return "upload";
    }
    @RequestMapping("/uploadFile")
    public String uploadFileToS3(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        fileService.uploadFile(multipartFile);
        return "upload";
    }

    @RequestMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFileFromS3(@PathVariable("fileName") String fileName){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-type", MediaType.ALL_VALUE);
        httpHeaders.add("Content-Disposition", "attachment; fileName=" + fileName);
        byte[] bytes = fileService.downloadFile(fileName);
        return ResponseEntity.status(HttpStatus.OK).headers(httpHeaders).body(bytes);
    }

    @RequestMapping("/list")
    public @ResponseBody ResponseEntity<List<String>> getAllFilesFromS3(){
        return ResponseEntity.ok().body(fileService.listOfAllFiles());
    }
}
