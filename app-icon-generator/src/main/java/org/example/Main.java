package org.example;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            // 创建输出目录
            File outputDir = new File("output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 从资源目录读取示例图片
            InputStream inputStream = Main.class.getResourceAsStream("/images/source.png");
            if (inputStream == null) {
                throw new RuntimeException("找不到源图片文件，请确保 src/main/resources/images/source.png 文件存在");
            }
            byte[] sourceImage = inputStream.readAllBytes();
            
            IconGeneratorService service = new IconGeneratorService();
            Map<Integer, byte[]> icons = service.generateIcons(sourceImage);
            
            // 保存生成的图标到输出目录
            for (Map.Entry<Integer, byte[]> entry : icons.entrySet()) {
                File outputFile = new File(outputDir, "icon_" + entry.getKey() + ".png");
                Files.write(outputFile.toPath(), entry.getValue());
                System.out.println("生成图标: " + outputFile.getPath());
            }
            
            System.out.println("所有图标生成完成！");
        } catch (Exception e) {
            System.err.println("生成图标时发生错误：");
            e.printStackTrace();
        }
    }
}