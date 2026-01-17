package wtf.alexhan.thousandlines.dto;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class UploadComicRequest {
    private String title;
    private String description;
    private String author;
    private List<String> tags;
    private MultipartFile coverImage;
    private MultipartFile[] pages; // 改为数组

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public MultipartFile getCoverImage() { return coverImage; }
    public void setCoverImage(MultipartFile coverImage) { this.coverImage = coverImage; }

    public MultipartFile[] getPages() { return pages; } // Getter改为数组
    public void setPages(MultipartFile[] pages) { this.pages = pages; } // Setter改为数组
}