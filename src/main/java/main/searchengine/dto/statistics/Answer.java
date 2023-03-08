package main.searchengine.dto.statistics;

import lombok.Data;

import java.util.List;

@Data
public class Answer
{
    boolean result;
    int count;
    List<ResponseObjSearch> data;
}
