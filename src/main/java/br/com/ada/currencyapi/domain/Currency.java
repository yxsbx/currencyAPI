package br.com.ada.currencyapi.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Currency implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;

    @ElementCollection
    @CollectionTable(name = "exchanges",
            joinColumns = {@JoinColumn(name = "currency_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "currency_name")
    private Map<String, BigDecimal> exchanges;

}