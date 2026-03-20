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
    private final UserDocumentRepository userDocumentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Inicializando datos base...");
        initAdminUser();
        initTestUser();
        initDocumentTypes();
        initInstitutions();
        initTestUserDocuments();
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

        if (!userRepository.existsByEmail("isabela@validaya.com")) {
            User admin = User.builder()
                    .email("isabela@validaya.com")
                    .passwordHash(passwordEncoder.encode("testpass123"))
                    .fullName("Isabela Ortiz")
                    .identification("9876543")
                    .userType(UserType.citizen)
                    .isActive(true)
                    .faceVerified(false)
                    .build();
            userRepository.save(admin);
            log.info("Usuario admin creado: admin@validaya.com");
        }
    }

    private void initDocumentTypes() {
        // Existentes
        createDocTypeIfAbsent("CI",             "Cédula de Identidad",           false);
        createDocTypeIfAbsent("BIRTH_CERT",     "Certificado de Nacimiento",     false);
        createDocTypeIfAbsent("MARRIAGE_CERT",  "Certificado de Matrimonio",     false);
        createDocTypeIfAbsent("DEATH_CERT",     "Certificado de Defunción",      false);
        createDocTypeIfAbsent("PASSPORT",       "Pasaporte",                     true);
        createDocTypeIfAbsent("DRIVER_LICENSE", "Licencia de Conducir",          false);
        createDocTypeIfAbsent("TITLE",          "Título Universitario",          false);
        // Nuevos
        createDocTypeIfAbsent("CERT_DOM",          "Certificado de Domicilio",       false);
        createDocTypeIfAbsent("CERT_TRABAJO",      "Certificado de Trabajo",         false);
        createDocTypeIfAbsent("EXTRACTO_BANCARIO", "Extracto Bancario",              false);
        createDocTypeIfAbsent("FOTO",              "Fotografía Reciente",            false);
        createDocTypeIfAbsent("SEGURO_SALUD",      "Certificado de Seguro de Salud", false);
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
        // ── SEGIP ────────────────────────────────────────────────────────────
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

            DocumentType ci       = documentTypeRepository.findByCode("CI").orElse(null);
            DocumentType foto     = documentTypeRepository.findByCode("FOTO").orElse(null);
            DocumentType certTrab = documentTypeRepository.findByCode("CERT_TRABAJO").orElse(null);

            Procedure primeraCI = createProcedure(segip, "Primera Cédula de Identidad",
                    "primera-ci", new BigDecimal("30"), new BigDecimal("3"), 7, ci);
            Procedure renovacionCI = createProcedure(segip, "Renovación de Cédula de Identidad",
                    "renovacion-ci", new BigDecimal("50"), new BigDecimal("3"), 5, ci);

            if (ci != null) {
                saveProcReqIfAbsent(primeraCI, ci, true, "Cédula de Identidad original y copia", 1);
                saveProcReqIfAbsent(renovacionCI, ci, true, "Cédula de Identidad vencida", 1);
            }
            if (foto != null) {
                saveProcReqIfAbsent(primeraCI, foto, true, "Fotografía reciente tomada en SEGIP", 2);
                saveProcReqIfAbsent(renovacionCI, foto, false, "Fotografía reciente (opcional)", 2);
            }
            if (certTrab != null) {
                saveProcReqIfAbsent(primeraCI, certTrab, false, "Comprobante de actividad laboral (opcional)", 3);
            }

            createAppointmentSlots(sucursalLaPaz, 30);
            log.info("Institución SEGIP creada con sucursal, trámites y slots de citas");
        }

        // ── SERECI ───────────────────────────────────────────────────────────
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

            DocumentType birthCert    = documentTypeRepository.findByCode("BIRTH_CERT").orElse(null);
            DocumentType marriageCert = documentTypeRepository.findByCode("MARRIAGE_CERT").orElse(null);
            DocumentType ci           = documentTypeRepository.findByCode("CI").orElse(null);

            Procedure certNacimiento = createProcedure(sereci, "Certificado de Nacimiento",
                    "cert-nacimiento", new BigDecimal("20"), new BigDecimal("3"), 3, birthCert);
            Procedure certMatrimonio = createProcedure(sereci, "Certificado de Matrimonio",
                    "cert-matrimonio", new BigDecimal("20"), new BigDecimal("3"), 3, marriageCert);

            if (birthCert != null) saveProcReqIfAbsent(certNacimiento, birthCert, false, "Certificado de Nacimiento original", 1);
            if (ci != null)        saveProcReqIfAbsent(certNacimiento, ci, false, "Cédula de Identidad válida", 2);

            if (marriageCert != null) saveProcReqIfAbsent(certMatrimonio, marriageCert, true, "Certificado de Matrimonio original", 1);
            if (ci != null)           saveProcReqIfAbsent(certMatrimonio, ci, true, "Cédula de Identidad válida de ambos cónyuges", 2);

            createAppointmentSlots(sucursalDowntown, 30);
            log.info("Institución SERECI creada con sucursal, trámites y slots de citas");
        }

        // ── GOBIERNO MUNICIPAL ───────────────────────────────────────────────
        if (institutionRepository.findBySlug("gob-municipal").isEmpty()) {
            Institution gobMunicipal = Institution.builder()
                    .name("Gobierno Municipal")
                    .slug("gob-municipal")
                    .institutionType(InstitutionType.PUBLIC)
                    .description("Gobierno Autónomo Municipal de Santa Cruz de la Sierra")
                    .contactEmail("contacto@municipio.gob.bo")
                    .isActive(true)
                    .build();
            gobMunicipal = institutionRepository.save(gobMunicipal);

            Branch sucursalMunicipal = Branch.builder()
                    .institution(gobMunicipal)
                    .name("Oficina Central Municipal")
                    .address("Plaza Principal, Santa Cruz de la Sierra")
                    .city("Santa Cruz")
                    .maxDailyAppointments(30)
                    .isActive(true)
                    .build();
            sucursalMunicipal = branchRepository.save(sucursalMunicipal);

            DocumentType certDom = documentTypeRepository.findByCode("CERT_DOM").orElse(null);
            DocumentType ci      = documentTypeRepository.findByCode("CI").orElse(null);

            Procedure certDomProc = createProcedure(gobMunicipal, "Certificado de Domicilio",
                    "cert-domicilio", new BigDecimal("0"), new BigDecimal("0"), 1, certDom);

            if (ci != null)      saveProcReqIfAbsent(certDomProc, ci, true, "Cédula de Identidad vigente", 1);
            if (certDom != null) saveProcReqIfAbsent(certDomProc, certDom, false, "Certificado de domicilio anterior (si aplica)", 2);

            createAppointmentSlots(sucursalMunicipal, 30);
            log.info("Institución Gobierno Municipal creada");
        }

        // ── BANCO NACIONAL ───────────────────────────────────────────────────
        if (institutionRepository.findBySlug("banco-nacional").isEmpty()) {
            Institution banco = Institution.builder()
                    .name("Banco Nacional de Bolivia")
                    .slug("banco-nacional")
                    .institutionType(InstitutionType.PRIVATE)
                    .description("Entidad bancaria nacional")
                    .contactEmail("contacto@bnb.com.bo")
                    .isActive(true)
                    .build();
            banco = institutionRepository.save(banco);

            Branch sucursalBanco = Branch.builder()
                    .institution(banco)
                    .name("Sucursal Centro")
                    .address("Calle Sucre, Santa Cruz de la Sierra")
                    .city("Santa Cruz")
                    .maxDailyAppointments(20)
                    .isActive(true)
                    .build();
            sucursalBanco = branchRepository.save(sucursalBanco);

            DocumentType extracto = documentTypeRepository.findByCode("EXTRACTO_BANCARIO").orElse(null);
            DocumentType ci       = documentTypeRepository.findByCode("CI").orElse(null);
            DocumentType certDom  = documentTypeRepository.findByCode("CERT_DOM").orElse(null);
            DocumentType certTrab = documentTypeRepository.findByCode("CERT_TRABAJO").orElse(null);

            Procedure extractoProc = createProcedure(banco, "Extracto Bancario",
                    "extracto-bancario", new BigDecimal("0"), new BigDecimal("0"), 0, extracto);
            Procedure cuentaProc   = createProcedure(banco, "Apertura de Cuenta Bancaria",
                    "apertura-cuenta", new BigDecimal("0"), new BigDecimal("3"), 1, extracto);

            if (ci != null)       saveProcReqIfAbsent(extractoProc, ci, true, "Cédula de Identidad vigente", 1);
            if (ci != null)       saveProcReqIfAbsent(cuentaProc, ci, true, "Cédula de Identidad vigente", 1);
            if (certDom != null)  saveProcReqIfAbsent(cuentaProc, certDom, true, "Certificado de Domicilio", 2);
            if (certTrab != null) saveProcReqIfAbsent(cuentaProc, certTrab, false, "Certificado de trabajo o ingresos (si aplica)", 3);

            createAppointmentSlots(sucursalBanco, 30);
            log.info("Institución Banco Nacional creada");
        }

        // ── SEGUROS BOLIVIA ──────────────────────────────────────────────────
        if (institutionRepository.findBySlug("seguros-bolivia").isEmpty()) {
            Institution seguros = Institution.builder()
                    .name("Seguros Bolivia")
                    .slug("seguros-bolivia")
                    .institutionType(InstitutionType.PRIVATE)
                    .description("Seguro de salud estatal boliviano")
                    .contactEmail("contacto@seguros.gob.bo")
                    .isActive(true)
                    .build();
            seguros = institutionRepository.save(seguros);

            Branch sucursalSeguros = Branch.builder()
                    .institution(seguros)
                    .name("Oficina Central Seguros")
                    .address("Av. Monseñor Rivero, Santa Cruz de la Sierra")
                    .city("Santa Cruz")
                    .maxDailyAppointments(25)
                    .isActive(true)
                    .build();
            sucursalSeguros = branchRepository.save(sucursalSeguros);

            DocumentType seguroSalud = documentTypeRepository.findByCode("SEGURO_SALUD").orElse(null);
            DocumentType ci          = documentTypeRepository.findByCode("CI").orElse(null);
            DocumentType certTrab    = documentTypeRepository.findByCode("CERT_TRABAJO").orElse(null);
            DocumentType partida     = documentTypeRepository.findByCode("BIRTH_CERT").orElse(null);
            DocumentType foto        = documentTypeRepository.findByCode("FOTO").orElse(null);
            DocumentType certDom     = documentTypeRepository.findByCode("CERT_DOM").orElse(null);

            Procedure seguroProc = createProcedure(seguros, "Afiliación Seguro de Salud",
                    "seguro-salud", new BigDecimal("5"), new BigDecimal("3"), 3, seguroSalud);

            if (ci != null)       saveProcReqIfAbsent(seguroProc, ci, true, "Cédula de Identidad vigente", 1);
            if (certTrab != null) saveProcReqIfAbsent(seguroProc, certTrab, true, "Certificado de trabajo", 2);
            if (partida != null)  saveProcReqIfAbsent(seguroProc, partida, false, "Partida de nacimiento", 3);
            if (foto != null)     saveProcReqIfAbsent(seguroProc, foto, false, "Fotografía reciente 4×4", 4);
            if (certDom != null)  saveProcReqIfAbsent(seguroProc, certDom, false, "Certificado de domicilio", 5);

            createAppointmentSlots(sucursalSeguros, 30);
            log.info("Institución Seguros Bolivia creada");
        }
    }

    private void initTestUserDocuments() {
        userRepository.findByIdentification("1234567").ifPresent(testUser -> {
            // CI — verificada
            createUserDocIfAbsent(testUser, "CI",
                    DocumentStatus.active, VerificationStatus.verified, DocumentSource.platform_generated,
                    "1234567", LocalDate.of(2020, 3, 12), LocalDate.of(2028, 3, 12));

            // Certificado de Nacimiento — verificado
            createUserDocIfAbsent(testUser, "BIRTH_CERT",
                    DocumentStatus.active, VerificationStatus.verified, DocumentSource.platform_generated,
                    null, LocalDate.of(1990, 6, 1), null);

            // Certificado de Domicilio — pendiente verificación
            createUserDocIfAbsent(testUser, "CERT_DOM",
                    DocumentStatus.active, VerificationStatus.unverified, DocumentSource.manual_entry,
                    null, LocalDate.of(2026, 1, 4), LocalDate.of(2027, 1, 4));

            // Fotografía reciente
            createUserDocIfAbsent(testUser, "FOTO",
                    DocumentStatus.active, VerificationStatus.unverified, DocumentSource.manual_entry,
                    null, LocalDate.of(2026, 2, 18), null);

            // Certificado de Trabajo
            createUserDocIfAbsent(testUser, "CERT_TRABAJO",
                    DocumentStatus.active, VerificationStatus.unverified, DocumentSource.manual_entry,
                    null, LocalDate.of(2025, 12, 10), LocalDate.of(2026, 12, 10));

            log.info("Documentos de prueba creados para usuario 1234567");
        });
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

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

    private void saveProcReqIfAbsent(Procedure procedure, DocumentType docType,
                                     boolean mandatory, String notes, int order) {
        if (procedure == null || docType == null) return;
        boolean exists = procedureDocumentRequirementRepository
                .findByProcedureIdAndDocumentTypeId(procedure.getId(), docType.getId())
                .isPresent();
        if (!exists) {
            procedureDocumentRequirementRepository.save(ProcedureDocumentRequirement.builder()
                    .procedure(procedure)
                    .documentType(docType)
                    .isMandatory(mandatory)
                    .notes(notes)
                    .displayOrder(order)
                    .build());
        }
    }

    private void createUserDocIfAbsent(User user, String docTypeCode,
                                       DocumentStatus status, VerificationStatus verStatus,
                                       DocumentSource source, String docNumber,
                                       LocalDate issueDate, LocalDate expiryDate) {
        documentTypeRepository.findByCode(docTypeCode).ifPresent(docType -> {
            boolean exists = userDocumentRepository
                    .findByUserIdAndDocumentTypeCode(user.getId(), docTypeCode)
                    .isPresent();
            if (!exists) {
                userDocumentRepository.save(UserDocument.builder()
                        .user(user)
                        .documentType(docType)
                        .status(status)
                        .verificationStatus(verStatus)
                        .source(source)
                        .documentNumber(docNumber)
                        .issueDate(issueDate)
                        .expiryDate(expiryDate)
                        .build());
            }
        });
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