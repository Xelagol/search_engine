package main.searchengine.controllers;

import main.searchengine.services.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import main.searchengine.dto.statistics.StatisticsResponse;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ApiController
{

    private final StatisticsService statisticsService;
    private final SiteIndexing siteIndexing;
    private final PageIndexing pageIndexing;
    private final LemmasCreator lemmasCreator;
    private final SearchText searchText;


    public ApiController(StatisticsService statisticsService,
                         SiteIndexing siteIndexing,
                         PageIndexing pageIndexing,
                         LemmasCreator lemmasCreator,
                         SearchText searchText)
    {
        this.statisticsService = statisticsService;
        this.siteIndexing = siteIndexing;
        this.pageIndexing = pageIndexing;
        this.lemmasCreator = lemmasCreator;
        this.searchText = searchText;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics()
    {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity startIndexing() throws IOException
    {
//        siteIndexing.indexingSite();
        return ResponseEntity.ok(siteIndexing.indexingIsAllowed());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity stopIndexing()
    {
        return ResponseEntity.ok(pageIndexing.terminateIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity startIndexingPage(String url) throws IOException
    {
        return ResponseEntity.ok(lemmasCreator.pageLemmaCreating(url));
    }

    @GetMapping("/search")
    public ResponseEntity searchText(@RequestParam(value = "query") String text,
                                     @RequestParam(value = "offset") int offset,
                                     @RequestParam(value = "limit") int limit,
                                     @RequestParam(value = "site", required = false) String site) throws IOException
    {
        return ResponseEntity.ok(searchText.search(text, site));
    }
}
