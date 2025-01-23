package org.example.service.impl;

import org.example.model.IconSize;
import org.example.service.IconGeneratorService;
import org.springframework.stereotype.Service;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import net.coobird.thumbnailator.resizers.configurations.Rendering;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class IconGeneratorServiceImpl implements IconGeneratorService {
    private static final int BASE_SIZE = 1024; // 基准尺寸

    public Map<Integer, byte[]> generateIcons(byte[] sourceImageData) throws IOException {
        Map<Integer, byte[]> result = new HashMap<>();
        BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(sourceImageData));
        
        // 首先将源图像调整为 1024x1024
        BufferedImage baseImage = Thumbnails.of(sourceImage)
                .size(BASE_SIZE, BASE_SIZE)
                .imageType(BufferedImage.TYPE_INT_ARGB)
                .asBufferedImage();
        
        // 生成所有尺寸的图标
        for (IconSize size : IconSize.values()) {
            byte[] resizedImage = resizeImage(baseImage, size.getSize());
            result.put(size.getSize(), resizedImage);
        }
        
        return result;
    }
    
    private byte[] resizeImage(BufferedImage source, int targetSize) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        float compressionQuality = calculateCompressionQuality(targetSize);
        
        Thumbnails.of(source)
                .size(targetSize, targetSize)
                .outputQuality(compressionQuality)
                .outputFormat("png")
                .useExifOrientation(false)
                .imageType(BufferedImage.TYPE_INT_ARGB)
                .antialiasing(targetSize > 256 ? Antialiasing.ON : Antialiasing.OFF)
                .rendering(targetSize > 256 ? Rendering.QUALITY : Rendering.SPEED)
                .toOutputStream(outputStream);
                
        return outputStream.toByteArray();
    }
    
    private float calculateCompressionQuality(int size) {
        if (size <= 64) {
            return 0.85f;
        } else if (size <= 256) {
            return 0.80f;
        } else if (size <= 512) {
            return 0.75f;
        } else {
            return 0.70f;
        }
    }
}