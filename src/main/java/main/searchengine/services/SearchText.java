package main.searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import main.searchengine.dto.statistics.Answer;
import main.searchengine.repository.IndexRepository;
import main.searchengine.repository.LemmasRepository;
import main.searchengine.repository.PageRepository;
import main.searchengine.repository.SiteRepository;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Setter
@Getter
@Service
@RequiredArgsConstructor
public class SearchText
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
    CalculareRank calculareRank;
    private String site;


    public Answer search(String text, String site) throws IOException
    {
        List<String> wordsList = new SplitterInputText().getWordsList(text);

        List<Integer> pagesId = new ArrayList<>();


        List<String> words = createUniqLemma(wordsList);
        List<Integer> listPagesContainsLemma = searchPage(words, pagesId, site);
        return calculareRank.calculateRank(listPagesContainsLemma, words);

    }

    private List<String> createUniqLemma(List<String> wordsList) throws IOException
    {
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        List<String> wordBaseForms = new ArrayList<>();

        Map<String, Integer> lemmasUniqWordRank = new HashMap<>();
        for (String word : wordsList)
        {
            boolean delete = false;

            wordBaseForms = luceneMorph.getMorphInfo(word);
            String wordsToken = wordBaseForms.get(0);
            String[] wordSplit = wordsToken.split(" ");
            for (int i = 0; i < wordSplit.length; i++)
            {
                if (wordSplit[i].contains("СОЮЗ")
                        || wordSplit[i].contains("ПРЕДЛ")
                        || wordSplit[i].contains("МС")
                        || wordSplit[i].contains("МЕЖД")
                        || wordSplit[i].contains("ЧАСТ")
                )
                {
                    delete = true;
                }
            }
            if (!delete)
            {
                String wrd = luceneMorph.getNormalForms(word).get(0);
                int rank = lemmasUniqWordRank.get(wrd) != null ? lemmasUniqWordRank.get(wrd) + 1 : 1;
                lemmasUniqWordRank.put(wrd, rank);
            }
        }
        return checkLemmas(lemmasUniqWordRank);
    }

    private List<String> checkLemmas(Map<String, Integer> lemmasUniq)
    {
        List<String> wordsRang = new ArrayList<>();

        for (Map.Entry<String, Integer> lemma : lemmasUniq.entrySet())
        {
            String freqString = lemmasRepository.getMaxFreqLemma(lemma.getKey());
            int freq = 0;
            if (freqString != null)
            {
                freq = Integer.parseInt(lemmasRepository.getMaxFreqLemma(lemma.getKey()));
            }
            lemmasUniq.replace(lemma.getKey(), freq);
        }

        List list = new ArrayList(lemmasUniq.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>()
        {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2)
            {
                return o1.getValue() - o2.getValue();
            }
        });
        for (Object lst : list)
        {
            String[] words = lst.toString().split("=");
            if (Integer.parseInt(words[1]) <= 100)
            {
                wordsRang.add(words[0]);
            }
        }
        return wordsRang;
    }

    private List<Integer> searchPage(List<String> words, List<Integer> pages, String site)
    {
        List<String> wrd = new ArrayList<>();
        List<Integer> listPagesContainsLemma = new ArrayList<>();

        if (words.size() == 1)
        {

            String word = words.get(0);
            List<Integer> listLemmaId = lemmasRepository.getLemmaIdListByLemma(word);
            for (int lemmaId : listLemmaId)
            {

                List<Integer> listPageId;
                if (site == null)
                {
                    listPageId = indexRepository.getPageIdListByLemmaId(lemmaId);
                } else
                {
                    int siteId = siteRepository.getIdByUrl(site);
                    listPageId = indexRepository.getPageIdListByLemmaIdSiteId(lemmaId, siteId);
                }
                if (pages.size() == 0 ) {
                    return listPageId;
                }
                for (int pId : pages)
                {
                    if (listPageId.contains(pId))
                    {
                        listPagesContainsLemma.add(pId);
                    }
                }
            }
            return listPagesContainsLemma;
        }

        if (!pages.isEmpty())
        {
            List<Integer> restPages = new ArrayList<>();
            String word = words.get(0);
            List<Integer> listLemmaId = lemmasRepository.getLemmaIdListByLemma(word);
            for (int lemmaId : listLemmaId)
            {
                List<Integer> listPageId;
                if (site == null)
                {
                    listPageId = indexRepository.getPageIdListByLemmaId(lemmaId);
                } else
                {
                    int siteId = siteRepository.getIdByUrl(site);
                    listPageId = indexRepository.getPageIdListByLemmaIdSiteId(lemmaId, siteId);
                }
                for (int pId : pages)
                {
                    if (listPageId.contains(pId))
                    {
                        restPages.add(pId);
                    }
                }
                if (restPages.isEmpty())
                {
                    return restPages;
                }
            }
            for (int i = 1; i < words.size(); i++)
            {
                wrd.add(words.get(i));
            }

            return searchPage(wrd, restPages, site);
        }


        String word = words.get(0);
        List<Integer> restPages;
        if (site == null)
        {
            restPages = lemmasRepository.getPageIdListByLemmas(word);
        } else
        {
            int siteId = siteRepository.getIdByUrl(site);
            restPages = lemmasRepository.getPageIdListByLemmaSiteId(word, siteId);
        }

        for (int i = 1; i < words.size(); i++)
        {
            wrd.add(words.get(i));
        }

        return searchPage(wrd, restPages, site);


    }

}


