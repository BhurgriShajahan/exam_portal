package exam.portal.repositories;


import exam.portal.constants.Roles;
import exam.portal.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    boolean existsByRole(Roles role);
    Optional<Role> findByRole(Roles role);
//
//    @Query("SELECT r FROM Role r WHERE r.name <> 'ROLE_SUPER_ADMIN'")
//    List<Role> findAllRolesWithOutSuperAdmin();
}
