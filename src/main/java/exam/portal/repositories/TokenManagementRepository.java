package exam.portal.repositories;

import exam.portal.model.TokenManagement;
import exam.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenManagementRepository extends JpaRepository<TokenManagement, Long>, Serializable {
    Optional<TokenManagement> findBySessionToken(String jwt);
    List<TokenManagement> findByUserAndIsBlackListedFalse(User user);
}
