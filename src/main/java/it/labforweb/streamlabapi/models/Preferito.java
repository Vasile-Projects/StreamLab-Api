package it.labforweb.streamlabapi.models;

import jakarta.persistence.*;

@Entity
@Table(name = "preferiti")
public class Preferito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id",nullable = false)
    private Integer userId;

    @Column(name = "tmdb_id",nullable = false)
    private Integer tmdbId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoTitolo tipo;

    public Preferito() {}

    public Preferito(Integer id, Integer userId, Integer tmdbId, TipoTitolo tipo) {
        this.id = id;
        this.userId = userId;
        this.tmdbId = tmdbId;
        this.tipo = tipo;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getTmdbId() { return tmdbId; }
    public void setTmdbId(Integer tmdbId) { this.tmdbId = tmdbId; }

    public TipoTitolo getTipo() { return tipo; }
    public void setTipo(TipoTitolo tipo) { this.tipo = tipo; }
}
