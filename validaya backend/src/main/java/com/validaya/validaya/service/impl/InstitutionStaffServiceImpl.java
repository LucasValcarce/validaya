package com.validaya.validaya.service.impl;

import com.validaya.validaya.entity.Institution;
import com.validaya.validaya.entity.InstitutionStaff;
import com.validaya.validaya.entity.User;
import com.validaya.validaya.entity.enums.UserType;
import com.validaya.validaya.exception.NotDataFoundException;
import com.validaya.validaya.repository.BranchRepository;
import com.validaya.validaya.repository.InstitutionRepository;
import com.validaya.validaya.repository.InstitutionStaffRepository;
import com.validaya.validaya.repository.UserRepository;
import com.validaya.validaya.service.InstitutionStaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InstitutionStaffServiceImpl implements InstitutionStaffService {

    private final InstitutionStaffRepository staffRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final BranchRepository branchRepository;

    @Override
    public InstitutionStaff assign(Long userId, Long institutionId, boolean isAdmin, Long branchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotDataFoundException("Usuario no encontrado", null));

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new NotDataFoundException("Institución no encontrada", null));

        // Validar que el usuario sea ciudadano
        if (!UserType.citizen.equals(user.getUserType())) {
            throw new IllegalArgumentException("Solo se pueden asignar ciudadanos como staff");
        }

        // Cambiar user_type a institution_admin si es admin, senó a staff
        UserType newUserType = isAdmin ? UserType.institution_admin : UserType.staff;
        user.setUserType(newUserType);
        userRepository.save(user);

        // Crear registro de InstitutionStaff
        InstitutionStaff staff = InstitutionStaff.builder()
                .user(user)
                .institution(institution)
                .branch(branchRepository.findById(branchId).orElse(null))
                .isActive(true)
                .build();

        log.info("Usuario {} asignado como {} en institución {}", userId, isAdmin ? "institution_admin" : "staff", institutionId);
        return staffRepository.save(staff);
    }

    @Override
    public InstitutionStaff update(Long staffId, boolean isAdmin, Long branchId) {
        InstitutionStaff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new NotDataFoundException("Staff no encontrado", null));

        // Actualizar user_type
        UserType newUserType = isAdmin ? UserType.institution_admin : UserType.staff;
        User user = staff.getUser();
        user.setUserType(newUserType);
        userRepository.save(user);

        staff.setBranch(branchRepository.findById(branchId).orElse(null));

        log.info("Staff {} actualizado a {} ", staffId, isAdmin ? "institution_admin" : "staff");
        return staffRepository.save(staff);
    }

    @Override
    public Optional<InstitutionStaff> findById(Long id) {
        return staffRepository.findById(id);
    }

    @Override
    public List<InstitutionStaff> findByInstitution(Long institutionId) {
        return staffRepository.findByInstitutionIdAndIsActiveTrue(institutionId);
    }

    @Override
    public List<InstitutionStaff> findByUser(Long userId) {
        return staffRepository.findByUserId(userId);
    }

    @Override
    public Optional<InstitutionStaff> findByUserAndInstitution(Long userId, Long institutionId) {
        return staffRepository.findByUserIdAndInstitutionId(userId, institutionId);
    }

    @Override
    public void deactivate(Long staffId) {
        InstitutionStaff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new NotDataFoundException("Staff no encontrado", null));

        User user = staff.getUser();

        // Cambiar user_type de vuelta a citizen
        user.setUserType(UserType.citizen);
        userRepository.save(user);

        // Desactivar el registro de staff
        staff.setIsActive(false);
        staffRepository.save(staff);

        log.info("Staff {} desactivado, usuario {} cambiado a ciudadano", staffId, user.getId());
    }

    @Override
    public boolean isInstitutionAdmin(Long userId, Long institutionId) {
        return staffRepository.findByUserIdAndInstitutionId(userId, institutionId)
                .map(staff -> Boolean.TRUE.equals(staff.getIsActive())
                        && UserType.institution_admin.equals(staff.getUser().getUserType()))
                .orElse(false);
    }
}
