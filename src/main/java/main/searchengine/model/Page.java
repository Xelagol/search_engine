package main.searchengine.model;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.Data;
import main.searchengine.config.Site;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Table(name = "page")
@Data
public class Page
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;

//        @Column(name = "site_id", nullable = false, insertable=false, updatable=false)
//    private int siteId; /*ID веб-сайта из таблицы site;*/
//    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = SiteModel.class)
    @JoinColumn(foreignKey = @ForeignKey(name = "siteIdPageKey"), columnDefinition = "Integer",
            referencedColumnName = "id", name = "site_id", nullable = false)
//    @OnDelete(action = OnDeleteAction.CASCADE)
    private SiteModel siteModel;

    @Column(columnDefinition = "TEXT not null, index (path(30))")
    private String path; /* адрес страницы от корня сайта (должен начинаться со слэша, например: /news/372189/)*/

    @Column(nullable = false)
    private int code; /* код HTTP-ответа, полученный при запросе страницы (например, 200, 404, 500 или другие);*/

    @Column(columnDefinition = "mediumtext", nullable = false)
    private String content; /*контент страницы(HTML-код).*/


//    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private Site site;

}
