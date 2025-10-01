package exam.portal.services.service_impl;

import exam.portal.constants.Roles;
import exam.portal.dtos.request.RoleRequestDto;
import exam.portal.global_exceptions_handler.CustomResponseEntity;
import exam.portal.model.Role;
import exam.portal.repositories.RoleRepository;
import exam.portal.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public CustomResponseEntity<?> createRole(RoleRequestDto roleDto) {
        try {
            if (roleDto.getName() == null || roleDto.getName().isBlank()) {
                return CustomResponseEntity.error("Role name is required");
            }

            String roleName = roleDto.getName().trim().toUpperCase();

            Roles roleEnum;
            try {
                roleEnum = Roles.valueOf(roleName);
            } catch (IllegalArgumentException e) {
                return CustomResponseEntity.error("Invalid role name: " + roleDto.getName());
            }

            if (roleRepository.existsByRole(roleEnum)) {
                return CustomResponseEntity.error("Role already exists with name: " + roleDto.getName());
            }

            Role role = new Role();
            role.setRole(roleEnum);
            Role saved = roleRepository.save(role);

            return new CustomResponseEntity<>(saved, "Role created successfully");
        } catch (Exception e) {
            return CustomResponseEntity.error("An error occurred while creating role: " + e.getMessage());
        }
    }

    @Override
    public CustomResponseEntity fetchRole(String roleName) {
        try {
            Optional<Role> roleOpt = roleRepository.findByRole(Roles.valueOf(roleName.toUpperCase()));

            if (roleOpt.isEmpty()) {
                return CustomResponseEntity.error("Role not found: " + roleName);
            }

            Role role = roleOpt.get();

            return new CustomResponseEntity<>(role, "Role fetched successfully");
        } catch (Exception e) {
            return CustomResponseEntity.error("An error occurred while fetching role: " + e.getMessage());
        }
    }

    @Override
    public CustomResponseEntity<?> deleteRole(Long roleId) {
        try {
            Optional<Role> roleOpt = roleRepository.findById(roleId);
            if (roleOpt.isEmpty()) {
                return CustomResponseEntity.error("Role not found with ID: " + roleId);
            }

            Role role = roleOpt.get();

//            if ("ROLE_SUPER_ADMIN".equalsIgnoreCase(Role(role.getRole().toString())) {
//                return CustomResponseEntity.error("Cannot delete SUPER ADMIN role");
//            }

            roleRepository.deleteById(roleId);

            return new CustomResponseEntity<>(null, "Role deleted successfully");
        } catch (Exception e) {
            return CustomResponseEntity.error("An error occurred while deleting role: " + e.getMessage());
        }
    }
}
