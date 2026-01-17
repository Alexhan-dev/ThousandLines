package wtf.alexhan.thousandlines.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wtf.alexhan.thousandlines.model.Comic;

import java.util.List;

@Repository
public interface ComicRepository extends JpaRepository<Comic, Long> {

    // 基本查询
    List<Comic> findByTitleContainingIgnoreCase(String title);
    List<Comic> findByAuthorContainingIgnoreCase(String author);
    List<Comic> findByUserId(Long userId);

    // 分页查询
    Page<Comic> findAll(Pageable pageable);
    Page<Comic> findByUserId(Long userId, Pageable pageable);
    Page<Comic> findAllByOrderByViewCountDesc(Pageable pageable);
    Page<Comic> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Comic> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Comic> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    // 热门和最新
    List<Comic> findTop10ByOrderByViewCountDesc();
    List<Comic> findTop10ByOrderByCreatedAtDesc();

    // 标签相关查询
    @Query("SELECT DISTINCT c FROM Comic c JOIN c.tags t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :tagName, '%'))")
    List<Comic> findByTagName(@Param("tagName") String tagName);

    @Query("SELECT DISTINCT c FROM Comic c JOIN c.tags t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :tagName, '%'))")
    Page<Comic> findByTagName(@Param("tagName") String tagName, Pageable pageable);

    // 综合搜索
    @Query("SELECT DISTINCT c FROM Comic c " +
            "LEFT JOIN c.tags t " +
            "WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Comic> searchComics(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT c FROM Comic c " +
            "LEFT JOIN c.tags t " +
            "WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.author) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Comic> searchComics(@Param("keyword") String keyword, Pageable pageable);

    // 统计查询
    long countByUserId(Long userId);

    // 热门标签查询
    @Query("SELECT t.name, COUNT(c) as count FROM Comic c JOIN c.tags t GROUP BY t.name ORDER BY count DESC")
    List<Object[]> findPopularTags(@Param("limit") int limit);
}