package main.searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import main.searchengine.config.Site;
import main.searchengine.config.SitesList;
import main.searchengine.dto.statistics.FalseResponse;
import main.searchengine.dto.statistics.ResponseTF;
import main.searchengine.dto.statistics.TrueResponse;
import main.searchengine.model.*;
import main.searchengine.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Setter
@Getter
@Service
@RequiredArgsConstructor
public class SiteIndexing
{
    //    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH-mm-ss");
    private static final int aviableProc = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executorService = Executors.newFixedThreadPool(aviableProc);
    static List<Future<?>> futureList = new ArrayList<>();
    private final SitesList sites;

    @Autowired
    PageRepository pageRepository;
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    LemmasRepository lemmasRepository;
    @Autowired
    IndexRepository indexRepository;

    public void indexingSite()
    {

        Map<String, Integer> objIdName = new TreeMap<>();
        List<Site> sitesList = sites.getSites();
        List<Integer> idSites = lemmasRepository.getIdSitesFromLemmas();
        Iterable<SiteModel> sitesObj = siteRepository.findAll();
        sitesObj.forEach(v -> objIdName.put(v.getName(), v.getId()));

        for (Site site : sitesList)
        {
            if (objIdName.containsKey(site.getName()))
            {
                int siteId = objIdName.get(site.getName());
                if (idSites.contains(siteId))
                lemmasRepository.deleteLemmaBySiteId(siteId);
                pageRepository.deletePageBySiteId(siteId);
                    siteRepository.deleteSiteByName(site.getName());
//                SiteModel sM = siteRepository.findById(siteId).get();
//                siteRepository.deleteById(siteId);
            }
            String url = site.getUrl().replaceAll("(?<![^.]http\\w?://)www\\.", "") + "/"; /*убираем www и добавляем слэш, если нет*/
            url = url.replaceAll("(?<=(?<=http\\w?://)[^/]{1,65535}/).*", ""); /*убираем второй слэш в конце, если есть*/
            int countSiteUpdt = siteRepository.updateSite(new Date(System.currentTimeMillis()), Status.INDEXING.toString(), "", site.getName());
            if (countSiteUpdt == 0)
            {
                SiteModel siteEntity = new SiteModel();

                siteEntity.setName(site.getName());
                siteEntity.setUrl(url);
                siteEntity.setLastError("");
                siteEntity.setStatus(Status.INDEXING);
                siteEntity.setStatusTime(new Date(System.currentTimeMillis()));
                siteRepository.save(siteEntity);
            }

            Runnable task = new PageIndexing(site, pageRepository, siteRepository);
            Future<?> f = executorService.submit(task);
            futureList.add(f);

        }
        try
        {
            checkIndexing(futureList);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

    private void checkIndexing(List<Future<?>> futureList) throws IOException
    {
        List<Future> list = new ArrayList<>();
        boolean indexingComplited = false;
        int b = futureList.size();
        while (!indexingComplited)
        {
            for (Future tsk : futureList)
            {
                if (tsk.isDone() && !list.contains(tsk))
                {
                    b--;
                    list.add(tsk);
                }
            }
            if (b == 0)
            {
                indexingComplited = true;
                new LemmasCreator(pageRepository, siteRepository, lemmasRepository, indexRepository).sitesLemmaCreator();
            }
        }
    }

    public ResponseTF indexingIsAllowed() throws IOException
    {
        for (Future<?> future : SiteIndexing.futureList)
        {
            if (!future.isDone())
            {
                return new FalseResponse(false, "Индексация уже запущена");
            }
        }
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                indexingSite();
            }
        };
        thread.start();

        return new TrueResponse(true);
    }
}
