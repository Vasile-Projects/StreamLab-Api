package it.labforweb.streamlabapi.models;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "abbonamenti")
public class Abbonamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 45)
    private String nome;

    @Column(nullable = false, length = 45)
    private String qualita;

    @Type(JsonType.class)
    @Column(nullable = false, columnDefinition = "json")
    private List<String> features;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal prezzo;

    public Abbonamento() {}

    public Abbonamento(Integer id, String nome, String qualita, List<String> features, BigDecimal prezzo) {
        this.id = id;
        this.nome = nome;
        this.qualita = qualita;
        this.features = features;
        this.prezzo = prezzo;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getQualita() { return qualita; }
    public void setQualita(String qualita) { this.qualita = qualita; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public BigDecimal getPrezzo() { return prezzo; }
    public void setPrezzo(BigDecimal prezzo) { this.prezzo = prezzo; }
}