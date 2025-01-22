package org.example.controller;

import org.example.service.IconGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/icons")
public class IconController {
    private final IconGeneratorService iconGeneratorService;

    public IconController(IconGeneratorService iconGeneratorService) {
        this.iconGeneratorService = iconGeneratorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateIcons(@RequestParam("image") MultipartFile file) {
        try {
            // 生成图标
            Map<Integer, byte[]> icons = iconGeneratorService.generateIcons(file.getBytes());
            
            // 创建ZIP文件
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (Map.Entry<Integer, byte[]> entry : icons.entrySet()) {
                    ZipEntry zipEntry = new ZipEntry(String.format("icon_%dx%d.png", entry.getKey(), entry.getKey()));
                    zos.putNextEntry(zipEntry);
                    zos.write(entry.getValue());
                    zos.closeEntry();
                }
            }

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "icons.zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(baos.toByteArray());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
} 