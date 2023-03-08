package main.searchengine.dto.statistics;

import lombok.Value;

@Value
public class TrueResponse implements ResponseTF
{
    boolean result;
}
