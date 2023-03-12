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
    private int siteId;

    @Column(columnDefinition = "varchar(255)", nullable = false)
    private String lemma;

    @Column(nullable = false)
    private int frequency;
}
