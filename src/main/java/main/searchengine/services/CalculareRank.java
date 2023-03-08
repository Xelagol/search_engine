package main.searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import main.searchengine.dto.statistics.Answer;
import main.searchengine.dto.statistics.ResponseObjSearch;
import main.searchengine.model.Page;
import main.searchengine.model.SiteModel;
import main.searchengine.repository.IndexRepository;
import main.searchengine.repository.LemmasRepository;
import main.searchengine.repository.PageRepository;
import main.searchengine.repository.SiteRepository;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.*;

@Setter
@Getter
@Service
@RequiredArgsConstructor


public class CalculareRank
{
    @Autowired
    PageRepository pageRepository;
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    LemmasRepository lemmasRepository;
    @Autowired
    IndexRepository indexRepository;
    @Autowired
    CreateObjSearch createObjSearch;


    public Answer calculateRank(List<Integer> listPagesContainsLemma, List<String> words)
    {
        Map<Integer, Integer> rankAbsPageIdRank = new HashMap<>();
        Map<Integer, Double> rankPageIdRelevant = new HashMap<>();
        List<Integer> rankList = new ArrayList<>();

        double maxRankTotAbs = 0;

        for (int pageId : listPagesContainsLemma)
        {
            int rankTotAbs = 0;
            Integer rank = 0;
            for (String word : words)
            {
//                if (site.isEmpty())
//                {
                    rank = lemmasRepository.getRankByLemmaPageId(word, pageId);
                    if (rank == null)
                    {
                        continue;
                    }
//                } else
//                {
//                    int siteId = siteRepository.getIdByName(site);
////                    rank = lemmasRepository.getRankByLemmaPageIdSiteId(word, pageId, siteId);
//                }
                rankTotAbs +=  rank;
            }
            if (rankTotAbs > maxRankTotAbs)
            {
                maxRankTotAbs = rankTotAbs;
            }
            rankAbsPageIdRank.put(pageId, rankTotAbs);
        }

        for (Map.Entry<Integer, Integer> pageIdRankTotAbs : rankAbsPageIdRank.entrySet())
        {
            double rankTotRelevant = pageIdRankTotAbs.getValue() / maxRankTotAbs;
            rankPageIdRelevant.put(pageIdRankTotAbs.getKey(), rankTotRelevant);
        }
        List list = new ArrayList(rankPageIdRelevant.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>()
        {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2)
            {
                double z = o1.getValue() - o2.getValue();
                if (z < 0)
                {
                    return 1;
                } else if (z > 0)
                {
                    return -1;
                }
                return 0;
            }
        });

        return createObjSearch.createAnswer(list, words);


    }
}
