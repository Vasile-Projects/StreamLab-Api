package it.labforweb.streamlabapi.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "abbonamenti_utenti")
public class AbbonamentoUtente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @Column(name = "tipo_abbonamento_id", nullable = false)
    private Integer tipoAbbonamentoId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public AbbonamentoUtente() {
    }

    public AbbonamentoUtente(Integer id, Integer userId, Integer tipoAbbonamentoId, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.tipoAbbonamentoId = tipoAbbonamentoId;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getTipoAbbonamentoId() {
        return tipoAbbonamentoId;
    }

    public void setTipoAbbonamentoId(Integer tipoAbbonamentoId) {
        this.tipoAbbonamentoId = tipoAbbonamentoId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
