package main.searchengine.dto.statistics;

import lombok.Data;

@Data
public class ResponseObjSearch
{
    String site;
    String siteName;
    String uri;
    String title;
    String snippet;
    double relevance;
}
