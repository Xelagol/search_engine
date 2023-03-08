package main.searchengine.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "lemmas")
@Data
public class Lemma
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;

    @Column(name = "site_id", nullable = false)
    private int siteId; /*ID веб-сайта из таблицы site;*/
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "site_id", nullable = false)
////    @OnDelete(action = OnDeleteAction.CASCADE)
//    private SiteModel siteModel;

    @Column(columnDefinition = "varchar(255)", nullable = false)
    private String lemma; /* нормальная форма слова (лемма);*/

    @Column(nullable = false)
    private int frequency; /*количество страниц, на которых слово встречается хотя бы один раз.
                            Максимальное значение не может превышать общее количество слов на сайте.*/

}
