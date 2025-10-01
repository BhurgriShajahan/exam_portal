package exam.portal.services;

import exam.portal.dtos.request.RoleRequestDto;
import exam.portal.global_exceptions_handler.CustomResponseEntity;

public interface RoleService {

    CustomResponseEntity<?> createRole(RoleRequestDto roleDto);

    CustomResponseEntity fetchRole(String roleName);

    CustomResponseEntity<?> deleteRole(Long roleId);
}
