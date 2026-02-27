package com.validaya.validaya.utils;

import com.validaya.validaya.entity.*;
import com.validaya.validaya.entity.enums.*;
import com.validaya.validaya.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InstitutionRepository institutionRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final ProcedureRepository procedureRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Inicializando datos base...");
        initRoles();
        initAdminUser();
        initDocumentTypes();
        initInstitutions();
        log.info("Datos base inicializados correctamente.");
    }

    private void initRoles() {
        createRoleIfAbsent("ADMIN", "Administrador del sistema");
        createRoleIfAbsent("STAFF", "Personal de institución");
        createRoleIfAbsent("CITIZEN", "Ciudadano");
    }

    private void createRoleIfAbsent(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .isSystem(true)
                    .build();
            roleRepository.save(role);
            log.info("Rol creado: {}", name);
        }
    }

    private void initAdminUser() {
        if (!userRepository.existsByEmail("admin@validaya.com")) {
            User admin = User.builder()
                    .email("admin@validaya.com")
                    .passwordHash(passwordEncoder.encode("[CHANGE_ME_ADMIN_PASSWORD]"))
                    .fullName("Administrador Validaya")
                    .identification("0000000")
                    .userType(UserType.admin)
                    .isActive(true)
                    .faceVerified(false)
                    .build();
            userRepository.save(admin);
            log.info("Usuario admin creado: admin@validaya.com");
        }
    }

    private void initDocumentTypes() {
        createDocTypeIfAbsent("CI", "Cédula de Identidad", false);
        createDocTypeIfAbsent("BIRTH_CERT", "Certificado de Nacimiento", false);
        createDocTypeIfAbsent("MARRIAGE_CERT", "Certificado de Matrimonio", false);
        createDocTypeIfAbsent("DEATH_CERT", "Certificado de Defunción", false);
        createDocTypeIfAbsent("PASSPORT", "Pasaporte", true);
        createDocTypeIfAbsent("DRIVER_LICENSE", "Licencia de Conducir", false);
        createDocTypeIfAbsent("TITLE", "Título Universitario", false);
    }

    private void createDocTypeIfAbsent(String code, String name, boolean biometric) {
        if (documentTypeRepository.findByCode(code).isEmpty()) {
            DocumentType dt = DocumentType.builder()
                    .code(code)
                    .name(name)
                    .isBiometric(biometric)
                    .build();
            documentTypeRepository.save(dt);
            log.info("Tipo de documento creado: {}", code);
        }
    }

    private void initInstitutions() {
        if (institutionRepository.findBySlug("segip").isEmpty()) {
            Institution segip = Institution.builder()
                    .name("SEGIP")
                    .slug("segip")
                    .institutionType(InstitutionType.PUBLIC)
                    .description("Servicio General de Identificación Personal")
                    .contactEmail("contacto@segip.gob.bo")
                    .isActive(true)
                    .build();
            segip = institutionRepository.save(segip);

            Branch sucursalLaPaz = Branch.builder()
                    .institution(segip)
                    .name("Sucursal La Paz - Centro")
                    .address("Calle Mercado esquina Colón, La Paz")
                    .city("La Paz")
                    .maxDailyAppointments(50)
                    .isActive(true)
                    .build();
            branchRepository.save(sucursalLaPaz);

            createProcedure(segip, "Renovación de Cédula de Identidad",
                    "renovacion-ci", new BigDecimal("50"), new BigDecimal("3"), 5);
            createProcedure(segip, "Primera Cédula de Identidad",
                    "primera-ci", new BigDecimal("30"), new BigDecimal("3"), 7);

            log.info("Institución SEGIP creada con sucursal y trámites");
        }

        if (institutionRepository.findBySlug("sereci").isEmpty()) {
            Institution sereci = Institution.builder()
                    .name("SERECI")
                    .slug("sereci")
                    .institutionType(InstitutionType.PUBLIC)
                    .description("Servicio de Registro Cívico")
                    .contactEmail("contacto@sereci.gob.bo")
                    .isActive(true)
                    .build();
            sereci = institutionRepository.save(sereci);

            createProcedure(sereci, "Certificado de Nacimiento",
                    "cert-nacimiento", new BigDecimal("20"), new BigDecimal("3"), 3);
            createProcedure(sereci, "Certificado de Matrimonio",
                    "cert-matrimonio", new BigDecimal("20"), new BigDecimal("3"), 3);

            log.info("Institución SERECI creada");
        }
    }

    private void createProcedure(Institution institution, String name, String code,
                                 BigDecimal basePrice, BigDecimal platformFee, int estimatedDays) {
        if (procedureRepository.findByCode(code).isEmpty()) {
            Procedure procedure = Procedure.builder()
                    .institution(institution)
                    .name(name)
                    .code(code)
                    .basePrice(basePrice)
                    .platformFee(platformFee)
                    .estimatedDays(estimatedDays)
                    .isActive(true)
                    .build();
            procedureRepository.save(procedure);
        }
    }
}