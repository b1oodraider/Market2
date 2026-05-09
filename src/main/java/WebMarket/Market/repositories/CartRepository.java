package WebMarket.Market.repositories;

import WebMarket.Market.models.CartId;
import WebMarket.Market.models.DBCartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<DBCartEntity, CartId> {

    List<DBCartEntity> findByUserId(int userId);

    void deleteAllByUserIdAndProductId(int userId, int productId);

    void deleteAllByUserId(int userId);
}
