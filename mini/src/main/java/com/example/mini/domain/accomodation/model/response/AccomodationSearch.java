package com.example.mini.domain.accomodation.model.response;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;

@Getter
@AllArgsConstructor
@Document(indexName = "accomodation")
public class AccomodationSearch {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
}
