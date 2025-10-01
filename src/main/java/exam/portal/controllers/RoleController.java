package exam.portal.controllers;

import exam.portal.dtos.request.RoleRequestDto;
import exam.portal.global_exceptions_handler.CustomResponseEntity;
import exam.portal.services.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/create")
    public CustomResponseEntity<?> createRole(@Valid @RequestBody RoleRequestDto roleDto) {
        return roleService.createRole(roleDto);
    }

    @GetMapping("/fetch")
    public CustomResponseEntity fetchRole(@RequestParam String roleName) {
        return roleService.fetchRole(roleName);
    }

    @DeleteMapping("/delete")
    public CustomResponseEntity<?> deleteRole(@RequestParam Long roleId) {
        return roleService.deleteRole(roleId);
    }
}
