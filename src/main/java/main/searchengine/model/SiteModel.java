package main.searchengine.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "site")
@Data
public class SiteModel
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')", nullable = false)
    private Status status; /* текущий статус полной индексации сайта, отражающий готовность поискового движка
                            осуществлять поиск по сайту — индексация или переиндексация в процессе, сайт полностью
                            проиндексирован(готов к поиску) либо его не удалось проиндексировать (сайт не готов к
                            поиску и не будет до устранения ошибок и перезапуска индексации) */

    @Column(name = "status_time", columnDefinition = "DATETIME", nullable = false)
    private Date statusTime; /* DATETIME NOT NULL — дата и время статуса (в случае статуса INDEXING дата и время должны
                                 обновляться регулярно при добавлении каждой новой страницы в индекс); */

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;   /*TEXT — текст ошибки индексации или NULL,если её не было; */

    @Column(columnDefinition = "varchar(255)", nullable = false)
    private String url; /*адрес главной страницы сайта */

    @Column(columnDefinition = "varchar(255)", nullable = false)
    private String name;  /*имя сайта */

    //
    @OneToMany(mappedBy = "siteModel", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @OneToMany (mappedBy = "siteModel")
//    @OnDelete(action = OnDeleteAction.)
    private Set<Page> pages;

//    @OneToMany(mappedBy = "siteModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
////    @OneToMany (mappedBy = "siteModel")
////    @OnDelete(action = OnDeleteAction.)
//    private Set<Lemma> lemmas;

}
