package WebMarket.Market.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="product_id")
    private int id;

    @Column(name="products_name")
    private String productsName;

    @Column(name="products_type")
    private String productsType;

    @Column(name="products_in_stock")
    private Integer productsInStock;

    @Column(name="products_description")
    private String productsDescription;
}
