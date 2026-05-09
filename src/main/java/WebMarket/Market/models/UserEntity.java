package WebMarket.Market.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private int id;

    @Column(name="username")
    @NotEmpty
    @Size(min=2, max=30)
    private String username;

    @Column(name="password")
    @NotEmpty
    private String password;

    @Column(name="email")
    @Email
    private String email;

    @Column(name="phone_number")
    private String phoneNumber;

    @Column(name="role")
    private String role;

}
