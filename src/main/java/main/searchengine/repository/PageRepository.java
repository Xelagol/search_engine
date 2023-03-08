package main.searchengine.repository;

import main.searchengine.model.Page;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer>
{
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM PAGE WHERE site_id= :site_id", nativeQuery = true)
    void deletePageBySiteId(@Param("site_id") int siteId);

    @Query(value = "SELECT count(*) FROM page where code!=200 AND site_id= :site_id", nativeQuery = true)
    int indexingOk(@Param("site_id") int siteId);

    @Query(value = "SELECT COUNT(*) FROM PAGE where site_id= :site_id", nativeQuery = true)
    int getCountIdPageBySiteId(@Param("site_id") int siteId);

    @Query(value = "SELECT site_id FROM page group by site_id;", nativeQuery = true)
    List<Integer> getOldSiteId();

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO page(path, site_id, content, code) VALUES :append", nativeQuery = true)
    void insertDataPage(@Param("append") String append);

    @Query(value = "select count(*) FROM page where site_id= :site_id", nativeQuery = true)
    int isExistsSiteId(@Param("site_id") int siteId);

    @Query(value = "SELECT id FROM page where path=:path", nativeQuery = true)
    int getIdPageByPath(@Param("path") String path);

    @Transactional
    @Modifying
    @Query(value = "update page set code=:code, content=:content  where path=:path", nativeQuery = true)
    void updatePageIfExist(@Param("code") int code, @Param("content") String content, @Param("path") String path);

}
