package main.searchengine.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "`index`")
@Data
public class Index
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int id;

    @Column(name = "page_id", nullable = false)
    private int pageId; /*идентификатор страницы;*/

    @Column(name = "lemma_id", nullable = false)
    private int lemmaId;/* идентификатор леммы;*/

    @Column(name = "`rank`", nullable = false)
    private float rank;/* количество данной леммы для данной страницы.*/

}
