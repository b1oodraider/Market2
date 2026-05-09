package WebMarket.Market.repositories;

import WebMarket.Market.models.CartId;
import WebMarket.Market.models.DBCartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<DBCartEntity, CartId> {

    List<DBCartEntity> findByUserId(int userId);

    Optional<DBCartEntity> findByUserIdAndProductId(int userId, int productId);

    void deleteAllByUserIdAndProductId(int userId, int productId);

    void deleteAllByUserId(int userId);
}
