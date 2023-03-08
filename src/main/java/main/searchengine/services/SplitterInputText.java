package main.searchengine.services;

import java.util.ArrayList;
import java.util.List;

public class SplitterInputText
{
    public List<String> getWordsList(String text)
    {
//        String text = "Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа";

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
