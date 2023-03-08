package main.searchengine.services;

import lombok.RequiredArgsConstructor;
import main.searchengine.config.Site;
import main.searchengine.dto.statistics.*;
import main.searchengine.model.SiteModel;
import main.searchengine.repository.IndexRepository;
import main.searchengine.repository.LemmasRepository;
import main.searchengine.repository.PageRepository;
import main.searchengine.repository.SiteRepository;
import org.springframework.stereotype.Service;
import main.searchengine.config.SitesList;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService
{

    private final Random random = new Random();
    private final SitesList sites;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmasRepository lemmasRepository;
    private SiteModel siteModel;
    private final SiteIndexing siteIndexing;

    @Override
    public StatisticsResponse getStatistics()
    {
        String[] statuses = {"INDEXED", "FAILED", "INDEXING"};
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };
        TotalStatistics total = new TotalStatistics();
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        DetailedStatisticsItem item;
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();

        Iterable<SiteModel> siteModels = siteRepository.findAll();
        if (!siteModels.iterator().hasNext())
        {
            total.setSites(sites.getSites().size());
            total.setIndexing(true);

            List<Site> sitesList = sites.getSites();
            for (int i = 0; i < sitesList.size(); i++)
            {
                item = new DetailedStatisticsItem();
                Site site = sitesList.get(i);
                item.setName(site.getName());
                item.setUrl(site.getUrl());
                item.setPages(0);
                item.setLemmas(0);
                item.setStatus("Not indexed");
                item.setError("");
                item.setStatusTime(System.currentTimeMillis());
                total.setPages(0);
                total.setLemmas(0);
                detailed.add(item);
            }

            data.setTotal(total);
            data.setDetailed(detailed);
            response.setStatistics(data);
            response.setResult(true);

            return response;
        }


        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<Site> sitesList = sites.getSites();
        for (int i = 0; i < sitesList.size(); i++)
        {
            item = new DetailedStatisticsItem();
            Site site = sitesList.get(i);
            int siteId = siteRepository.getIdByName(site.getName());
            Optional<SiteModel> siteModelOptional = siteRepository.findById(siteId);
            if (siteModelOptional.isPresent())
            {
                siteModel = siteModelOptional.get();
            }
            item.setName(siteModel.getName());
            item.setUrl(siteModel.getUrl());
            int pages = pageRepository.getCountIdPageBySiteId(siteId);
            int lemmas = lemmasRepository.getCountIdLemmasBySiteId(siteId);
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(siteModel.getStatus().toString());
            item.setError(siteModel.getLastError());
            item.setStatusTime(System.currentTimeMillis());
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }


        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
