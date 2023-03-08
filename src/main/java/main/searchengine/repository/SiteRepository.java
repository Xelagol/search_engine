package main.searchengine.repository;

import main.searchengine.model.SiteModel;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
@Repository
public interface SiteRepository extends CrudRepository<SiteModel, Integer>
{
    @Query(value = "SELECT url FROM SITE WHERE id= :id", nativeQuery = true)
    String getUrlById(@Param("id") int id);

    @Query(value = "SELECT id FROM SITE;", nativeQuery = true)
    int[] getIdSitesList();

    @Query(value = "SELECT id FROM SITE WHERE url= :url", nativeQuery = true)
    int getIdByUrl(@Param("url") String url);

    @Transactional
    @Modifying
    @Query(value = "update site set status= :status, last_error= :error where id= :id", nativeQuery = true)
    void updateStatusSite(@Param("status") String status, @Param("error") String error, @Param("id") int id);

    @Transactional
    @Modifying
    @Query(value = "update site set last_error= :error where id= :id", nativeQuery = true)
    void updateLastErrorSite(@Param("error") String status, @Param("id") int id);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM SITE WHERE name= :name", nativeQuery = true)
    void deleteSiteByName(@Param("name") String name);

    @Query(value = "SELECT count(*) FROM site where status='Failed' or status='indexing';", nativeQuery = true)
    int statusSiteOk();

    @Query(value = "select status FROM site where name= :name", nativeQuery = true)
    String getStatusSite(@Param("name") String name);

    @Transactional
    @Modifying
    @Query(value = "update site set status_time= :status_time where id= :id", nativeQuery = true)
    void updateTimeSite(@Param("status_time") String status_time, @Param("id") int id);

    @Transactional
    @Modifying
    @Query(value = "update site set status_time= :status_time, status=:status, last_error=:lastError where name= :name", nativeQuery = true)
    int updateSite(@Param("status_time") Date status_time, @Param("status") String status, @Param("lastError") String lastError, @Param("name") String name);

    @Query(value = "select count(*) FROM site where name= :name", nativeQuery = true)
    int isExistsSite(@Param("name") String name);

    @Query(value = "SELECT COUNT(*) FROM SITE;", nativeQuery = true)
    int getCountIdSites();

    @Query(value = "SELECT name FROM SITE WHERE id= :id", nativeQuery = true)
    int getNameById(@Param("id") int id);

    @Query(value = "SELECT id FROM SITE WHERE name= :name", nativeQuery = true)
    int getIdByName(@Param("name") String name);
}
