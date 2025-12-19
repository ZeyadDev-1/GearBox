package web.GearBox.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "{product.name.notnull}")
    private String name;

    @NotBlank(message = "{product.description.notnull}")
    private String description;

    @NotBlank(message = "{product.brand.notnull}")
    private String brand;

    @NotNull(message = "{product.price.notnull}")
    @DecimalMin(value = "0.0", message = "{product.price.min}")
    private BigDecimal price;

    @NotNull(message = "{product.categories.notnull}")
    @ElementCollection
    private List<String> categories;

    @NotNull(message = "{product.releaseDate.notnull}")
    private LocalDate releaseDate;

    private boolean productAvailable;

    @NotNull(message = "{product.stockQuantity.notnull}")
    @Min(value = 0, message = "{product.stockQuantity.min}")
    private Integer stockQuantity;

    private String imageName;

    private String imageType;

    @Lob
    private byte[] imageData;
}