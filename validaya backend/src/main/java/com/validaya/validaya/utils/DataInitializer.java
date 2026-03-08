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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final ProcedureRepository procedureRepository;
    private final BranchRepository branchRepository;
    private final ProcedureDocumentRequirementRepository procedureDocumentRequirementRepository;
    private final AppointmentSlotRepository appointmentSlotRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Inicializando datos base...");
        initAdminUser();
        initTestUser();
        initDocumentTypes();
        initInstitutions();
        log.info("Datos base inicializados correctamente.");
    }

    private void initAdminUser() {
        if (!userRepository.existsByEmail("admin@validaya.com")) {
            User admin = User.builder()
                    .email("admin@validaya.com")
                    .passwordHash(passwordEncoder.encode("pass123"))
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

    private void initTestUser() {
        if (!userRepository.existsByEmail("testuser@validaya.com")) {
            User testUser = User.builder()
                    .email("testuser@validaya.com")
                    .passwordHash(passwordEncoder.encode("testpass123"))
                    .fullName("Juan Pérez García")
                    .identification("1234567")
                    .userType(UserType.citizen)
                    .isActive(true)
                    .faceVerified(false)
                    .build();
            userRepository.save(testUser);
            log.info("Usuario de prueba creado: testuser@validaya.com (ID: 1234567)");
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
            sucursalLaPaz = branchRepository.save(sucursalLaPaz);

            // Get CI document type for output
            DocumentType ciDocType = documentTypeRepository.findByCode("CI").orElse(null);

            // Create procedures with document requirements and output document type
            Procedure primeraCI = createProcedure(segip, "Primera Cédula de Identidad",
                    "primera-ci", new BigDecimal("30"), new BigDecimal("3"), 7, ciDocType);
            Procedure renovacionCI = createProcedure(segip, "Renovación de Cédula de Identidad",
                    "renovacion-ci", new BigDecimal("50"), new BigDecimal("3"), 5, ciDocType);

            // Add document requirements for CI procedures
            DocumentType ci = documentTypeRepository.findByCode("CI").orElse(null);
            if (ci != null) {
                procedureDocumentRequirementRepository.save(ProcedureDocumentRequirement.builder()
                        .procedure(primeraCI)
                        .documentType(ci)
                        .isMandatory(true)
                        .maxAgeMonths(null)
                        .notes("Cédula de Identidad original y copia")
                        .displayOrder(1)
                        .build());

                procedureDocumentRequirementRepository.save(ProcedureDocumentRequirement.builder()
                        .procedure(renovacionCI)
                        .documentType(ci)
                        .isMandatory(true)
                        .maxAgeMonths(null)
                        .notes("Cédula de Identidad vencida")
                        .displayOrder(1)
                        .build());
            }

            // Create appointment slots for next 30 days
            createAppointmentSlots(sucursalLaPaz, 30);

            log.info("Institución SEGIP creada con sucursal, trámites y slots de citas");
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

            Branch sucursalDowntown = Branch.builder()
                    .institution(sereci)
                    .name("Sucursal Centro")
                    .address("Avenida Camacho esquina Ecuador, La Paz")
                    .city("La Paz")
                    .maxDailyAppointments(40)
                    .isActive(true)
                    .build();
            sucursalDowntown = branchRepository.save(sucursalDowntown);

            // Get output document types
            DocumentType birthCert = documentTypeRepository.findByCode("BIRTH_CERT").orElse(null);
            DocumentType marriageCert = documentTypeRepository.findByCode("MARRIAGE_CERT").orElse(null);
            DocumentType ci = documentTypeRepository.findByCode("CI").orElse(null);

            // Create procedures with document requirements and output document type
            Procedure certNacimiento = createProcedure(sereci, "Certificado de Nacimiento",
                    "cert-nacimiento", new BigDecimal("20"), new BigDecimal("3"), 3, birthCert);
            Procedure certMatrimonio = createProcedure(sereci, "Certificado de Matrimonio",
                    "cert-matrimonio", new BigDecimal("20"), new BigDecimal("3"), 3, marriageCert);

            // Add document requirements

            if (birthCert != null) {
                procedureDocumentRequirementRepository.save(ProcedureDocumentRequirement.builder()
                        .procedure(certNacimiento)
                        .documentType(birthCert)
                        .isMandatory(false)
                        .maxAgeMonths(null)
                        .notes("Certificado de Nacimiento original")
                        .displayOrder(1)
                        .build());

                if (ci != null) {
                    procedureDocumentRequirementRepository.save(ProcedureDocumentRequirement.builder()
                            .procedure(certNacimiento)
                            .documentType(ci)
                            .isMandatory(false)
                            .maxAgeMonths(null)
                            .notes("Cédula de Identidad válida")
                            .displayOrder(2)
                            .build());
                }
            }

            if (marriageCert != null) {
                procedureDocumentRequirementRepository.save(ProcedureDocumentRequirement.builder()
                        .procedure(certMatrimonio)
                        .documentType(marriageCert)
                        .isMandatory(true)
                        .maxAgeMonths(null)
                        .notes("Certificado de Matrimonio original")
                        .displayOrder(1)
                        .build());

                if (ci != null) {
                    procedureDocumentRequirementRepository.save(ProcedureDocumentRequirement.builder()
                            .procedure(certMatrimonio)
                            .documentType(ci)
                            .isMandatory(true)
                            .maxAgeMonths(null)
                            .notes("Cédula de Identidad válida")
                            .displayOrder(2)
                            .build());
                }
            }

            // Create appointment slots for next 30 days
            createAppointmentSlots(sucursalDowntown, 30);

            log.info("Institución SERECI creada con sucursal, trámites y slots de citas");
        }
    }

    private Procedure createProcedure(Institution institution, String name, String code,
                                 BigDecimal basePrice, BigDecimal platformFee, int estimatedDays,
                                 DocumentType outputDocumentType) {
        if (procedureRepository.findByCode(code).isEmpty()) {
            Procedure procedure = Procedure.builder()
                    .institution(institution)
                    .name(name)
                    .code(code)
                    .basePrice(basePrice)
                    .platformFee(platformFee)
                    .estimatedDays(estimatedDays)
                    .outputDocumentType(outputDocumentType)
                    .isActive(true)
                    .build();
            return procedureRepository.save(procedure);
        }
        return procedureRepository.findByCode(code).orElse(null);
    }

    private void createAppointmentSlots(Branch branch, int days) {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalTime[] slotTimes = {
                LocalTime.of(8, 0),
                LocalTime.of(8, 30),
                LocalTime.of(9, 0),
                LocalTime.of(9, 30),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                LocalTime.of(14, 0),
                LocalTime.of(14, 30),
                LocalTime.of(15, 0),
                LocalTime.of(15, 30),
        };

        for (int i = 0; i < days; i++) {
            LocalDate slotDate = startDate.plusDays(i);
            // Skip weekends
            if (slotDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    slotDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }

            for (LocalTime slotTime : slotTimes) {
                if (appointmentSlotRepository.findByBranchIdAndSlotDateAndSlotTime(
                        branch.getId(), slotDate, slotTime).isEmpty()) {
                    appointmentSlotRepository.save(AppointmentSlot.builder()
                            .branch(branch)
                            .slotDate(slotDate)
                            .slotTime(slotTime)
                            .maxCapacity(5)
                            .reservedCount(0)
                            .isBlocked(false)
                            .build());
                }
            }
        }
        log.info("Slots de cita creados para la sucursal: {}", branch.getName());
    }
}