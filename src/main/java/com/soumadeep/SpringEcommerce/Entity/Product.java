package com.soumadeep.SpringEcommerce.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@DynamicUpdate
@Data
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String description;
    private String brand;
    private BigDecimal price;
    private String category;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
    private boolean productAvailable;
    private int stockQuantity;

    private String imageName;
    private String imageType;
    @Lob
    @JsonIgnore
    private byte[] imageData;
}
