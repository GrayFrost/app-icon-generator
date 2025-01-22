package org.example;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import net.coobird.thumbnailator.resizers.configurations.Rendering;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class IconGeneratorService {
    
    public Map<Integer, byte[]> generateIcons(byte[] sourceImageData) throws IOException {
        Map<Integer, byte[]> result = new HashMap<>();
        BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(sourceImageData));
        
        // 确保源图像使用 ARGB 格式以保持透明度
        BufferedImage argbImage = new BufferedImage(
            sourceImage.getWidth(),
            sourceImage.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        argbImage.getGraphics().drawImage(sourceImage, 0, 0, null);
        
        for (IconSize size : IconSize.values()) {
            byte[] resizedImage = resizeImage(argbImage, size.getSize());
            result.put(size.getSize(), resizedImage);
        }
        
        return result;
    }
    
    private byte[] resizeImage(BufferedImage source, int targetSize) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        Thumbnails.of(source)
                .size(targetSize, targetSize)
                .outputQuality(1.0) // 使用最高质量
                .outputFormat("png")
                .useExifOrientation(false)
                .imageType(BufferedImage.TYPE_INT_ARGB) // 保持 ARGB 格式
                .antialiasing(Antialiasing.ON)
                .rendering(Rendering.QUALITY) // 使用质量优先的渲染
                .toOutputStream(outputStream);
                
        return outputStream.toByteArray();
    }
} 