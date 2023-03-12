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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, optional = false)
    @JoinColumn(name = "site_id", nullable = false, foreignKey =
    @ForeignKey(name = "FK_page_site"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SiteModel siteModel;

    @Column(columnDefinition = "TEXT not null, index (path(30))")
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(columnDefinition = "mediumtext", nullable = false)
    private String content;


//    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private Site site;

}
