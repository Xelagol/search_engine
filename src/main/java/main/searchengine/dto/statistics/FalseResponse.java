package main.searchengine.dto.statistics;

import lombok.Value;

@Value
public class FalseResponse implements ResponseTF
{
    boolean result;
    String error;
}
