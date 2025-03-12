package com.example.jampot.global.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


@Component
@RequiredArgsConstructor
public class ProfileAudioUtil {
    /*
    private final AmazonS3Client amazonS3Client;

    private static final String bucket = "";
    private static final String baseUrl  = "https://s3.amazonaws.com/profile-image/";

    public String uploadAudioFile(MultipartFile file, String fileName)throws Exception{
        System.out.println(fileName);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3Client.putObject(bucket, "profile-image/" + fileName, file.getInputStream(), metadata);
        return baseUrl + fileName;
    }
    */

}
