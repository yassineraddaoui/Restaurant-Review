package com.restaurant.domain.entities;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "features")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Feature {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    @NotBlank(message = "Feature name cannot be blank")
    private String name;
}