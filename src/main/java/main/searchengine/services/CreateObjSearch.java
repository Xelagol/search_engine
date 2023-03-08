package main.searchengine.services;

import main.searchengine.dto.statistics.Answer;
import main.searchengine.dto.statistics.ResponseObjSearch;
import main.searchengine.model.Page;
import main.searchengine.repository.PageRepository;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CreateObjSearch
{
    @Autowired
    PageRepository pageRepository;

    public Answer createAnswer(List list, List<String> words)
    {
        List<ResponseObjSearch> responseObjSearchList = new ArrayList<>();
        ResponseObjSearch responseObjSearch;
        Answer answer = new Answer();

        for (int i = 0; i < list.size(); i++)
        {
            for (String word : words)
            {
                responseObjSearch = new ResponseObjSearch();
                String pageIdRel = list.get(i).toString();
                String[] pageIdRelSplitArr = pageIdRel.split("=", 2);
                String pageIdRelSplit = pageIdRelSplitArr[0];
                double relevance = Double.parseDouble(pageIdRelSplitArr[1]);

                int pageIdOrderRel = Integer.parseInt(pageIdRelSplit);
                Page page = pageRepository.findById(pageIdOrderRel).get();
                String siteUrlSlesh = page.getSiteModel().getUrl();
                String siteUrl = siteUrlSlesh.substring(0, siteUrlSlesh.length() - 1);
                String siteName = page.getSiteModel().getName();
                String uri = page.getPath(); /*— путь к странице вида /path/to/page/6784*/
                int titleStart = page.getContent().indexOf("<title>") + 7;
                int titleStop = page.getContent().indexOf("</title>");
                String title = page.getContent().substring(titleStart, titleStop);/* заголовок страницы*/
                String content = page.getContent();
                String snip = Jsoup.clean(content, Safelist.simpleText());
                String snipReplace = snip.replaceAll(word, "<b>" + word + "</b>");
                int snipStart = snipReplace.indexOf("<b>");
                int snipStop = snipReplace.indexOf("</b>");
                if (snipStart < 0 || snipStop < 0)
                {
                    continue;
                }
                String snippet = snipReplace.substring(snipStart, snipStop);
                if (!snippet.contains(word))
                {
                    continue;
                }

                if (snipStart >= 0 && snipStart < 100)
                {
                    snippet = snipReplace.substring(0, snipStart + 100);
                } else
                {
                    snippet = snipReplace.substring(snipStart - 100, snipStart + 100);
                }
                System.out.println(snippet + "\n" + page.getId());

                responseObjSearch.setSiteUrl(siteUrl);
                responseObjSearch.setSiteName(siteName);
                responseObjSearch.setUri(uri);
                responseObjSearch.setTitle(title);
                responseObjSearch.setSnippet("..." + snippet + " ...");
                responseObjSearch.setRelevance(relevance);
                responseObjSearchList.add(responseObjSearch);
            }
        }
        answer.setCount(responseObjSearchList.size());
        answer.setResult(true);
        answer.setData(responseObjSearchList);
        return answer;
    }
}
