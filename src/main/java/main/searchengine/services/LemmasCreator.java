package main.searchengine.services;

import lombok.Getter;
import lombok.Setter;
import main.searchengine.dto.statistics.FalseResponse;
import main.searchengine.dto.statistics.ResponseTF;
import main.searchengine.dto.statistics.TrueResponse;
import main.searchengine.model.Index;
import main.searchengine.model.Lemma;
import main.searchengine.model.Page;
import main.searchengine.model.SiteModel;
import main.searchengine.repository.PageRepository;
import main.searchengine.repository.SiteRepository;
import main.searchengine.repository.LemmasRepository;
import main.searchengine.repository.IndexRepository;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;


@Setter
@Getter
@Service
public class LemmasCreator
{
    @Autowired
    PageRepository pageRepository;
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    LemmasRepository lemmasRepository;
    @Autowired
    IndexRepository indexRepository;

    Map<Integer, Map<Integer, String>> siteIdPageIdPageContent = new HashMap<>();
    Map<Integer, String> pageIdPageContent = new HashMap<>();
    LuceneMorphology luceneMorph = new RussianLuceneMorphology();
    List<String> wordBaseForms = new ArrayList<>();
    List<String> clearWords = new ArrayList<>();
    static Map<String, Integer> lemmasWordFreq;


    public LemmasCreator(PageRepository pageRepository, SiteRepository siteRepository, LemmasRepository lemmasRepository, IndexRepository indexRepository) throws IOException
    {
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.lemmasRepository = lemmasRepository;
        this.indexRepository = indexRepository;
    }

    public void sitesLemmaCreator()
    {
        List<Integer> idSites = lemmasRepository.getIdSitesFromLemmas();
        Iterable<SiteModel> siteModels = siteRepository.findAll();
        List<Integer> sM = new ArrayList<>();
        for (SiteModel sModel : siteModels)
        {
            sM.add(sModel.getId());
        }
        for (int id : idSites)
        {
            if (!sM.contains(id))
                lemmasRepository.deleteLemmaBySiteId(id);
        }
        lemmasWordFreq = new HashMap<>();
        Iterable<Page> pages = pageRepository.findAll();
        SplitterInputText inputText = new SplitterInputText();
        int pageId;
        int siteId;
        for (Page page : pages)
        {
            if (page.getCode() == 200)
            {
                pageIdPageContent = new HashMap<>();
                siteId = page.getSiteModel().getId();
                if (siteIdPageIdPageContent.containsKey(siteId)){
                    pageIdPageContent.putAll(siteIdPageIdPageContent.get(siteId));
                }
                pageIdPageContent.put(page.getId(), page.getContent());
                siteIdPageIdPageContent.put(page.getSiteModel().getId(), pageIdPageContent);
            }
        }

        for (Map.Entry<Integer, Map<Integer, String>> site : siteIdPageIdPageContent.entrySet())
        {
//            lemmasWordFreq = new HashMap<>();

            for (Map.Entry<Integer, String> pageIdContent : site.getValue().entrySet())
            {
                pageId = pageIdContent.getKey();
                siteId = site.getKey();
//            siteId = page.get().getSiteId();

                String contentText = pageIdContent.getValue();
//            String text = Jsoup.clean(contentText, Safelist.relaxed());
                String cleanText = Jsoup.clean(contentText, Safelist.simpleText());
                clearWords = inputText.getWordsList(cleanText);
                if (clearWords.size() > 0)
                {
                    createLemmas(clearWords, siteId, pageId, false);

                }
            }

        }
    }


    public ResponseTF pageLemmaCreating(String path) throws IOException
    {
        String siteUrl = path.replaceAll("(?<=https?://[^/]{1,65353}/).*", "");/* обрезаем http(s)://www и добавляем / */
        siteUrl = siteUrl.replaceAll("^www\\.", "");
        siteUrl = siteUrl.replaceAll("^https?://w{0,3}\\.?", "");
//        siteUrl = siteUrl.replaceAll("(?<=(?<=https?://)[^/]{1,65535}/).*", "");/* обрезаем до главного адреса без www*/
//        String url = siteUrl.replaceAll("(?<=^https?://)www\\.", "") + "/"; /*убираем www и добавляем слэш, если нет*/
//        String url = siteUrl.replaceAll("(?<=(?<=http\\w?://)[^/]{1,65535}/).*", ""); /*убираем второй слэш в конце, если есть*/
        Iterable<SiteModel> sitesObj = siteRepository.findAll();
        String pathForFindInRepo = path.replaceAll("(?<![^.])http\\w?://[^/]+/?", "/"); /*убираем www и добавляем слэш, если нет*/
//        pathForFindInRepo = pathForFindInRepo.replaceAll("(?<=(?<=http\\w?://)[^/]{1,65535}/).*", ""); /*убираем второй слэш в конце, если есть*/

        for (SiteModel site : sitesObj)
        {
            String objSiteUrl = site.getUrl();
            objSiteUrl = objSiteUrl.replaceAll("^https?://w{0,3}\\.?", "");
            if (objSiteUrl.equals(siteUrl))
            {
                int siteId = siteRepository.getIdByName(site.getName());
                Document doc = Jsoup.connect(path).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                "Chrome/109.0.0.0 Safari/537.36")
                        .referrer("http://www.google.com")
                        .get();
                String contentHtml = doc.outerHtml();
                int code = doc.connection().response().statusCode();
                String content = Jsoup.clean(contentHtml, Safelist.simpleText());
                List<String> wordsList = new SplitterInputText().getWordsList(content);
                Page page = new Page();
                page.setPath(path);/* адрес страницы от корня сайта (должен начинаться со слэша, например: /news/372189/)*/
//                page.setSiteId(siteId);
                page.setSiteModel(siteRepository.findById(siteId).get());
                page.setContent(contentHtml); /*контент страницы(HTML-код).*/
                page.setCode(code);/* код HTTP-ответа, полученный при запросе страницы (например, 200, 404, 500 или другие);*/
                int pageId = pageRepository.getIdPageByPath(pathForFindInRepo);
                if (pageId == 0)
                {
                    pageId = pageRepository.save(page).getId(); /*получаю Id сохраненной page*/
                } else
                {
                    pageRepository.updatePageIfExist(code, contentHtml, pathForFindInRepo);
                }
                lemmasWordFreq = new HashMap<>();
                createLemmas(wordsList, siteId, pageId, true);
                return new TrueResponse(true);
            }
        }
        return new FalseResponse(false, "Данная страница находится за пределами сайтов, \n" +
                "указанных в конфигурационном файле\n");
    }

    private void createLemmas(List<String> wordsList, int siteId, int pageId, boolean isPage)
    {
        Map<String, Integer> lemmasWordRank = new HashMap<>();
        List<String> pageIsContainsWord = new ArrayList<>();
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
            if (!delete && isPage)
            {
                String wrd = luceneMorph.getNormalForms(word).get(0);

                if (!pageIsContainsWord.contains(wrd))
                {
                    int freq = lemmasRepository.getFreqByLemmaSiteId(wrd, siteId);
                    lemmasWordFreq.put(wrd, freq);
                    pageIsContainsWord.add(wrd);
                }
                int rank = lemmasWordRank.get(wrd) != null ? lemmasWordRank.get(wrd) + 1 : 1;
                lemmasWordRank.put(wrd, rank);
            }
            if (!delete && !isPage)
            {
                String wrd = luceneMorph.getNormalForms(word).get(0);
                if (!pageIsContainsWord.contains(wrd))
                {
                    int freq = lemmasWordFreq.get(wrd) != null ? lemmasWordFreq.get(wrd) + 1 : 1;
                    lemmasWordFreq.put(wrd, freq);
                    pageIsContainsWord.add(wrd);
                }
                int rank = lemmasWordRank.get(wrd) != null ? lemmasWordRank.get(wrd) + 1 : 1;
                lemmasWordRank.put(wrd, rank);
            }
        }

        lemmasWordRank.entrySet().forEach(obj ->
        {
            Lemma lemma = new Lemma();
            int lmmId;
            Lemma lmm;
            String lemmaId = lemmasRepository.getIdByLemmaSiteId(obj.getKey(), siteId);
            if (lemmaId != null)
            {
                lmmId = Integer.parseInt(lemmaId);
                lmm = lemmasRepository.findById(lmmId).get();
                lemmasRepository.updateFreqLemma(obj.getKey(), lemmasWordFreq.get(obj.getKey()), siteId);

            } else
            {
                lemma.setLemma(obj.getKey());
//                lemma.setSiteModel(siteRepository.findById(siteId).get());
                lemma.setSiteId(siteId);
                lemma.setFrequency(lemmasWordFreq.get(obj.getKey()));
                lmm = lemmasRepository.save(lemma);
            }

            int countLemmaString = lemmasRepository.getCountLemmaIsExist(obj.getKey(), pageId);

            if (countLemmaString != 0)
            {
                indexRepository.updateRankLemma(lmm.getId(), obj.getValue(), pageId);
            } else
            {
                Index index = new Index();
                index.setLemmaId(lmm.getId());
                index.setRank(obj.getValue());
                index.setPageId(pageId);
                indexRepository.save(index);
            }

        });

    }
}

