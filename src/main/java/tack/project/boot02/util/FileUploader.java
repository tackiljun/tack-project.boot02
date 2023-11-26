package tack.project.boot02.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.log4j.Log4j2;

import net.coobird.thumbnailator.Thumbnailator;


@Log4j2
@Component
public class FileUploader {

    ////////////////////////////////////////////////////////////////////////////////////
    // 예외정의된 파일이 없으면 예외발생.
    public static class UploadException extends RuntimeException {

        public UploadException(String msg) {
            super(msg);
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////
    @Value("${tack.project.upload.path}")
    private String path;

    ////////////////////////////////////////////////////////////////////////////////////
    public void removeFiles(List<String> fileNames) {

        if(fileNames == null || fileNames.size() == 0) {
            throw new UploadException(("Files do not axist"));
        }

        // forLoop.
        for(String fname: fileNames) {
            File original = new File(path, fname);
            File thumb = new File(path, "s_"+fname);

            if(thumb.exists()) {
                thumb.delete();
            }
            original.delete();
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////
    public List<String> uploadFiles(List<MultipartFile> files, boolean makeThumbnail) {

        if(files == null || files.size() == 0) {
            throw new UploadException("No File");
        }

        List<String> uploadFileNames = new ArrayList<>();

        log.info("path: " + path);
        log.info(files);

        // loop.
        for(MultipartFile mFile : files) {
            String originalFileName = mFile.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String saveFileName = uuid+"_"+originalFileName;
            File saveFile = new File(path, saveFileName);

            try (InputStream in = mFile.getInputStream();
             OutputStream out = new FileOutputStream(saveFile);) {

                FileCopyUtils.copy(in, out);

                // 썸네일이 필요하다면.
                if(makeThumbnail) {
                    File thumbOutFile = new File(path, "s_"+saveFileName);
                    Thumbnailator.createThumbnail(saveFile, thumbOutFile, 200, 200);
                }
                
                uploadFileNames.add(saveFileName);
                
            } catch (Exception e) {
                throw new UploadException("Upload Fail:" + e.getMessage());
            }    

        }
        
        return uploadFileNames;
        
    }
    
}
