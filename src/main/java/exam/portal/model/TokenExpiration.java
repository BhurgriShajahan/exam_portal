package exam.portal.model;

import exam.portal.constants.TokenType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ca_exp_token", schema = "hrms_auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenExpiration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private TokenType tokenType;

    @Column(name = "token_exp_time")
    private Long expirationTimeInMins;
}
