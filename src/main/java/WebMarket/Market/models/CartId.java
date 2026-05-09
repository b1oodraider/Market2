package WebMarket.Market.models;

import java.io.Serializable;
import java.util.Objects;

public class CartId implements Serializable {
    private int userId;
    private int productId;

    public CartId() {}

    public CartId(int userId, int productId) {
        this.userId = userId;
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartId cartId = (CartId) o;
        return userId == cartId.userId && productId == cartId.productId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, productId);
    }
}
