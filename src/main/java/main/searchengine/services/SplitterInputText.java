package main.searchengine.services;

import java.util.ArrayList;
import java.util.List;

public class SplitterInputText
{
    public List<String> getWordsList(String text)
    {
        String[] words = text.split(" ");
        List<String> wordsList = new ArrayList<>();
        for (String word : words)
        {
            String trim = word.toLowerCase().replaceAll("[^А-яа-яЁё]", "");
            if (trim.length() > 3)
            {
                wordsList.add(trim);
            }
        }
        return wordsList;
    }
}
