package wtf.alexhan.thousandlines.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    public String storeCoverImage(MultipartFile file) throws IOException {
        if (!isJpgFile(file)) {
            throw new IllegalArgumentException("封面图片必须是 JPG 格式");
        }
        return storeFile(file, "covers");
    }

    public String storeComicPage(MultipartFile file, String comicFolder) throws IOException {
        if (!isJpgFile(file)) {
            throw new IllegalArgumentException("漫画页面必须是 JPG 格式");
        }
        return storeFile(file, "comics/" + comicFolder);
    }

    private boolean isJpgFile(MultipartFile file) {
        return file.getContentType().equals("image/jpeg");
    }

    private String storeFile(MultipartFile file, String subDir) throws IOException {
        String filename = file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir, subDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return subDir + "/" + filename;
    }

    public void deleteFolder(String folderName) throws IOException {
        Path folderPath = Paths.get(uploadDir, "comics", folderName);
        if (Files.exists(folderPath)) {
            Files.walk(folderPath)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }
}
