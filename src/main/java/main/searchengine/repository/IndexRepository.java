package main.searchengine.repository;

import main.searchengine.model.Index;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Repository
public interface IndexRepository extends CrudRepository<Index, Integer>

{

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM `INDEX` WHERE lemma_id= :lemma_id", nativeQuery = true)
    void deleteIndexLemmaById(@Param("lemma_id") int lemmaId);

    @Transactional
    @Modifying
    @Query(value = "update `index` set frequency=:frequency where lemma=:lemma and site_id=:site_id", nativeQuery = true)
    int updateFreqLemma(@Param("lemma") String lemma, @Param("frequency") int frequency, @Param("site_id") int site_id);

    @Transactional
    @Modifying
    @Query(value = "update `index` set `rank`=:rank where lemma_id=:lemma_id and page_id=:page_id", nativeQuery = true)
    int updateRankLemma(@Param("lemma_id") int lemma_id, @Param("rank") int rank, @Param("page_id") int page_id);

    @Query(value = "select lemma_id FROM search_engine.index i join lemmas l on i.lemma_id=l.id where page_id=:page_id", nativeQuery = true)
    List<Integer> getListLemmaIdByLemmas(@Param("page_id") int page_id);

    @Query(value = "select page_id FROM search_engine.index where lemma_id=:lemma_id", nativeQuery = true)
    List<Integer> getPageIdListByLemmaId(@Param("lemma_id") int lemma_id);

    @Query(value = "select page_id FROM search_engine.index i join lemmas l on i.lemma_id=l.id where lemma_id=:lemma_id and site_id=:site_id", nativeQuery = true)
    List<Integer> getPageIdListByLemmaIdSiteId(@Param("lemma_id") int lemma_id, @Param("site_id") int site_id);







}
