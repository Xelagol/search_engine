package main.searchengine.dto.statistics;

import lombok.Data;

@Data
public class ResponseObjSearch
{
    String siteUrl;
    String siteName;
    String uri; /*— путь к странице вида /path/to/page/6784*/
    String title;/* заголовок страницы*/
    String snippet; /* фрагмент текста, в котором найдены совпадения (см. ниже)*/
    double relevance;
}
