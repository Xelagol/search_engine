package main.searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import main.searchengine.config.Site;
import main.searchengine.dto.statistics.FalseResponse;
import main.searchengine.dto.statistics.ResponseTF;
import main.searchengine.dto.statistics.TrueResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import main.searchengine.repository.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Setter
@Getter
@Service
@RequiredArgsConstructor
public class PageIndexing implements Runnable
{
    private static final int aviableProc = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executorService = Executors.newFixedThreadPool(aviableProc);
    private Site site;
    @Autowired
    PageRepository pageRepository;
    @Autowired
    SiteRepository siteRepository;
    static List<ForkJoinPool> tasksFork = new ArrayList<>();

    public PageIndexing(Site site, PageRepository pageRepository, SiteRepository siteRepository)
    {
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.site = site;
    }

    @Override
    public void run()
    {
        tasksFork.clear();
        int id = siteRepository.getIdByName(site.getName());
        String path = siteRepository.getUrlById(id);
        RecursivePage.haveUrl.clear();
        ForkJoinPool task = new ForkJoinPool();
        task.invoke(new RecursivePage(path, id, pageRepository, siteRepository));
        tasksFork.add(task);
}


    public ResponseTF terminateIndexing()
    {
        boolean isRunning = false;
        for (ForkJoinPool task : tasksFork)
        {
            if (!task.isTerminated())
            {
                task.shutdownNow();
                isRunning = true;
            }
        }
        return isRunning ? new TrueResponse(true) : new FalseResponse(false, "Индексация не запущена");
    }
}
