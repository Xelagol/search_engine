package main.searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import main.searchengine.model.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import main.searchengine.repository.*;

import java.util.*;
import java.util.concurrent.RecursiveAction;


@Setter
@Getter
@RequiredArgsConstructor
public class RecursivePage extends RecursiveAction
{

    PageRepository pageRepository;
    SiteRepository siteRepository;
    private String path;
    private int id;

    static List<String> haveUrl = new ArrayList<>();
    static List<Page> pagesList = new ArrayList<>();
    List<RecursivePage> tasks = new ArrayList<>();
//    static  List<RecursivePage> tasksForChecking = new ArrayList<>();

    public RecursivePage(String path, int id, PageRepository pageRepository, SiteRepository siteRepository)
    {
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.path = path;
        this.id = id;
    }


    @Override
    protected void compute()
    {
        if (!haveUrl.contains(path))
        {
            haveUrl.add(path);
            String siteUrl = path.replaceAll("(?<=(?<=http\\w?://)[^/]{1,65535}/).*", ""); /* обрезаем до главного адреса без www*/
            Page page = new Page();
            int code = 598;
            String content = "Read Timeout";
            Elements link = null;
            int codeError = 0;
            String trimPath = path.replaceAll("(?<![^.])http\\w?://[^/]+/?", "/");/* меняем главный сайт на "/"*/
            page.setPath(trimPath);/* адрес страницы от корня сайта (должен начинаться со слэша, например: /news/372189/)*/
//            page.setSiteId(id);/*ID веб-сайта из таблицы site;*/
            Optional<SiteModel> siteModelOpt = siteRepository.findById(id);

            SiteModel siteModel = siteModelOpt.get();

            page.setSiteModel(siteModel);
//            page.setSiteId(siteModel.getId());
            try
            {
                Thread.sleep(300);
                Document doc = Jsoup.connect(path).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36")
                        .referrer("http://www.google.com")
                        .get();

                code = doc.connection().response().statusCode();
                content = doc.outerHtml();
                link = doc.select("a");
            } catch (Exception e)
            {
                int start = e.getMessage().indexOf("Status=");
                content = e.getMessage().substring(start - 25, start - 1);
                code = Integer.parseInt(e.getMessage().substring(start + 7, start + 10));
                siteRepository.updateStatusSite(Status.FAILED.toString(), e.getMessage(), id);
                Optional<SiteModel> changeDateTime = siteRepository.findById(id);
                if (changeDateTime.isPresent())
                {
                    changeDateTime.get().setStatusTime(new Date(System.currentTimeMillis()));
                    siteRepository.save(changeDateTime.get());
                }
                System.out.println(e.getMessage());
            }

            page.setContent(content); /*контент страницы(HTML-код).*/
            page.setCode(code);/* код HTTP-ответа, полученный при запросе страницы (например, 200, 404, 500 или другие);*/
            pageRepository.save(page);
//                pagesList.add(page);

            if (link != null)
            {
                for (Element headline : link)
                {
                    String absHref = headline.attr("abs:href");

                    if (!absHref.matches(".+htm(l)?"))
                    {
                        absHref = absHref.replaceAll("(?!.+/)[^/]+", ""); /* обрезаем хвост с файлами, хештегами, кодами*/
                    }
                    String absHrefTrim = absHref.replaceAll("(?<=(?<=http\\w?://)[^/]{1,65535}/).*", "");
                    if (absHrefTrim.equals(siteUrl))
                    {
                        RecursivePage task = new RecursivePage(absHref, id, pageRepository, siteRepository);
                        task.fork();
                        tasks.add(task);
//                        tasksForChecking.add(task);
                    }
                    Optional<SiteModel> changeDateTime = siteRepository.findById(id);
                    if (changeDateTime.isPresent())
                    {
                        changeDateTime.get().setStatusTime(new Date(System.currentTimeMillis()));
                        siteRepository.save(changeDateTime.get());
                    }
                }
            }

            for (RecursivePage task : tasks)
            {
                task.join();


            }

        }
    }
}
