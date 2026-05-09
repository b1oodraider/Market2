package WebMarket.Market.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DBCartDTO {
    private int userId;
    private int productId;
    private int productCount;
    private String productsName;
}
