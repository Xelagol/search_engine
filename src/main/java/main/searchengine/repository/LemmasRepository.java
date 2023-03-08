package main.searchengine.repository;

import main.searchengine.model.Lemma;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LemmasRepository extends CrudRepository<Lemma, Integer>
{
    @Query(value = "SELECT `rank` FROM search_engine.index i join  lemmas l on i.lemma_id=l.id where lemma=:lemma and page_id=:page_id", nativeQuery = true)
    Integer getRankByLemmaPageId(@Param("lemma") String lemma, @Param("page_id") int pageId);

//    @Query(value = "SELECT `rank`FROM search_engine.index i join  lemmas l on i.lemma_id=l.id where lemma=:lemma and page_id=:page_id and site_id=:site_id", nativeQuery = true)
//    int getRankByLemmaPageIdSiteId(@Param("lemma") String lemma, @Param("page_id") int pageId, @Param("site_id") int site_id);


    @Query(value = "SELECT id FROM LEMMAS WHERE lemma=:lemma", nativeQuery = true)
    List<Integer> getLemmaIdListByLemma(@Param("lemma") String lemma);

    @Query(value = "SELECT id FROM LEMMAS WHERE lemma=:lemma AND SITE_ID=:site_id", nativeQuery = true)
    String getIdByLemmaSiteId(@Param("lemma") String lemma, @Param("site_id") int siteId);

    @Query(value = "SELECT frequency FROM LEMMAS WHERE lemma=:lemma AND SITE_ID=:site_id", nativeQuery = true)
    int getFreqByLemmaSiteId(@Param("lemma") String lemma, @Param("site_id") int siteId);

    @Query(value = "SELECT frequency FROM search_engine.index i join lemmas l on i.lemma_id=l.id " +
            "where lemma=:lemma " +
            "order by frequency desc limit 1", nativeQuery = true)
    String getMaxFreqLemma(@Param("lemma") String lemma);

    @Query(value = "SELECT count(*) FROM search_engine.index i join lemmas l on i.lemma_id=l.id " +
            "where lemma=:lemma and page_id=:page_id", nativeQuery = true)
    int getCountLemmaIsExist(@Param("lemma") String lemma, @Param("page_id") int page_id);

    @Query(value = "select distinct site_id from lemmas;", nativeQuery = true)
    List<Integer> getIdSitesFromLemmas();

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM LEMMAS WHERE lemma= :lemma", nativeQuery = true)
    void deleteLemmaByName(@Param("lemma") String lemma);


    @Transactional
    @Modifying
    @Query(value = "DELETE FROM LEMMAS WHERE site_id= :site_id", nativeQuery = true)
    void deleteLemmaBySiteId(@Param("site_id") int site_id);

    @Query(value = "SELECT COUNT(*) FROM LEMMAS where site_id= :site_id", nativeQuery = true)
    int getCountIdLemmasBySiteId(@Param("site_id") int siteId);

    @Transactional
    @Modifying
    @Query(value = "update lemmas set frequency=:frequency where lemma=:lemma and site_id=:site_id", nativeQuery = true)
    int updateFreqLemma(@Param("lemma") String lemma, @Param("frequency") int frequency, @Param("site_id") int site_id);

    @Query(value = "SELECT page_id FROM `index` i join  lemmas l on i.lemma_id=l.id where lemma=:lemma", nativeQuery = true)
    List<Integer> getPageIdListByLemmas(@Param("lemma") String lemma);

    @Query(value = "SELECT page_id FROM `index` i join  lemmas l on i.lemma_id=l.id where lemma=:lemma and site_id=:site_id", nativeQuery = true)
    List<Integer> getPageIdListByLemmaSiteId(@Param("lemma") String lemma, @Param("site_id") int site_id);


}
