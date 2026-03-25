package aib.noticeboard.controller;

import aib.noticeboard.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    private final S3Service s3Service;

    @PostMapping
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String url = s3Service.upload(file, "images");
        return ResponseEntity.ok(url);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam("fileUrl") String fileUrl) {
        s3Service.delete(fileUrl);
        return ResponseEntity.noContent().build();
    }
}
