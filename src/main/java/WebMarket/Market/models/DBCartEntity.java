package WebMarket.Market.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="carts")
@IdClass(CartId.class)
public class DBCartEntity {

    @Id
    @Column(name = "user_id")
    private int userId;

    @Id
    @Column(name = "product_id")
    private int productId;

    @Column(name = "product_count")
    private int productCount;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private ProductEntity product;

    public DBCartEntity(int userId, int productId, int productCount) {
        this.userId = userId;
        this.productId = productId;
        this.productCount = productCount;
    }
}
