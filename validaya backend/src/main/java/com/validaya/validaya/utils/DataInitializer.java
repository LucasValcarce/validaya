package com.validaya.validaya.utils;

import com.validaya.validaya.entity.*;
import com.validaya.validaya.entity.enums.*;
import com.validaya.validaya.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional  // FIX #1: todo en una única transacción con flush garantizado
    public void run(String... args) {
        log.info("Inicializando datos base...");
        initAdminUser();
        initTestUsers();
        initDocumentTypes();   // FIX #2: primero types, luego institutions/procedures
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

    private void initTestUsers() {
        if (!userRepository.existsByEmail("testuser@validaya.com")) {
            userRepository.save(User.builder()
                    .email("testuser@validaya.com")
                    .passwordHash(passwordEncoder.encode("testpass123"))
                    .fullName("Juan Pérez García")
                    .identification("1234567")
                    .userType(UserType.citizen)
                    .isActive(true).faceVerified(false).build());
            log.info("Usuario Juan Pérez creado: CI 1234567");
        }

        if (!userRepository.existsByEmail("isabela@validaya.com")) {
            userRepository.save(User.builder()
                    .email("isabela@validaya.com")
                    .passwordHash(passwordEncoder.encode("testpass123"))
                    .fullName("Isabela Ortiz")
                    .identification("9876543")
                    .userType(UserType.citizen)
                    .isActive(true).faceVerified(false).build());
            log.info("Usuario Isabela Ortiz creado: CI 9876543");
        }

        if (!userRepository.existsByEmail("carlos@validaya.com")) {
            userRepository.save(User.builder()
                    .email("carlos@validaya.com")
                    .passwordHash(passwordEncoder.encode("testpass123"))
                    .fullName("Carlos Mamani Quispe")
                    .identification("7654321")
                    .userType(UserType.citizen)
                    .isActive(true).faceVerified(false).build());
            log.info("Usuario Carlos Mamani creado: CI 7654321 (solo CI)");
        }

        if (!userRepository.existsByEmail("sofia@validaya.com")) {
            userRepository.save(User.builder()
                    .email("sofia@validaya.com")
                    .passwordHash(passwordEncoder.encode("testpass123"))
                    .fullName("Sofía Vargas Luna")
                    .identification("5551234")
                    .userType(UserType.citizen)
                    .isActive(true).faceVerified(false).build());
            log.info("Usuario Sofía Vargas creada: CI 5551234");
        }

        if (!userRepository.existsByEmail("pedro@validaya.com")) {
            userRepository.save(User.builder()
                    .email("pedro@validaya.com")
                    .passwordHash(passwordEncoder.encode("testpass123"))
                    .fullName("Pedro Flores Chávez")
                    .identification("3331111")
                    .userType(UserType.citizen)
                    .isActive(true).faceVerified(false).build());
            log.info("Usuario Pedro Flores creado: CI 3331111 (todos los docs)");
        }
    }

    private void initDocumentTypes() {
        createDocTypeIfAbsent("CI",                "Cédula de Identidad",            false);
        createDocTypeIfAbsent("BIRTH_CERT",        "Certificado de Nacimiento",      false);
        createDocTypeIfAbsent("MARRIAGE_CERT",     "Certificado de Matrimonio",      false);
        createDocTypeIfAbsent("DEATH_CERT",        "Certificado de Defunción",       false);
        createDocTypeIfAbsent("PASSPORT",          "Pasaporte",                      true);
        createDocTypeIfAbsent("DRIVER_LICENSE",    "Licencia de Conducir",           false);
        createDocTypeIfAbsent("TITLE",             "Título Universitario",           false);
        createDocTypeIfAbsent("CERT_DOM",          "Certificado de Domicilio",       false);
        createDocTypeIfAbsent("CERT_TRABAJO",      "Certificado de Trabajo",         false);
        createDocTypeIfAbsent("EXTRACTO_BANCARIO", "Extracto Bancario",              false);
        createDocTypeIfAbsent("FOTO",              "Fotografía Reciente",            false);
        createDocTypeIfAbsent("SEGURO_SALUD",      "Certificado de Seguro de Salud", false);
    }

    private void createDocTypeIfAbsent(String code, String name, boolean biometric) {
        if (documentTypeRepository.findByCode(code).isEmpty()) {
            documentTypeRepository.save(DocumentType.builder()
                    .code(code).name(name).isBiometric(biometric).build());
            log.info("Tipo de documento creado: {}", code);
        }
    }

    private void initInstitutions() {
        // FIX #3: los DocumentType se obtienen UNA SOLA VEZ al inicio, fuera de los if-blocks.
        // Así están disponibles tanto en el primer arranque como en reinicios.
        DocumentType ci           = requireDocType("CI");
        DocumentType foto         = requireDocType("FOTO");
        DocumentType certTrab     = requireDocType("CERT_TRABAJO");
        DocumentType birthCert    = requireDocType("BIRTH_CERT");
        DocumentType marriageCert = requireDocType("MARRIAGE_CERT");
        DocumentType certDom      = requireDocType("CERT_DOM");
        DocumentType extracto     = requireDocType("EXTRACTO_BANCARIO");
        DocumentType seguroSalud  = requireDocType("SEGURO_SALUD");

        // ── SEGIP ─────────────────────────────────────────────────────────────
        if (institutionRepository.findBySlug("segip").isEmpty()) {
            Institution segip = institutionRepository.save(Institution.builder()
                    .name("SEGIP").slug("segip")
                    .institutionType(InstitutionType.PUBLIC)
                    .description("Servicio General de Identificación Personal")
                    .contactEmail("contacto@segip.gob.bo").isActive(true).build());

            Branch sucursalLaPaz = branchRepository.save(Branch.builder()
                    .institution(segip).name("Sucursal La Paz - Centro")
                    .address("Calle Mercado esquina Colón, La Paz").city("La Paz")
                    .maxDailyAppointments(50).isActive(true).build());

            Procedure primeraCI = createProcedure(segip, "Primera Cédula de Identidad",
                    "primera-ci", new BigDecimal("30"), new BigDecimal("3"), 7, ci);
            Procedure renovCI   = createProcedure(segip, "Renovación de Cédula de Identidad",
                    "renovacion-ci", new BigDecimal("50"), new BigDecimal("3"), 5, ci);

            saveProcReqIfAbsent(primeraCI, ci,       true,  "CI original y copia", 1);
            saveProcReqIfAbsent(primeraCI, foto,     true,  "Fotografía reciente", 2);
            saveProcReqIfAbsent(primeraCI, certTrab, false, "Comprobante laboral (opcional)", 3);
            saveProcReqIfAbsent(renovCI,   ci,       true,  "CI vencida", 1);
            saveProcReqIfAbsent(renovCI,   foto,     false, "Fotografía reciente (opcional)", 2);

            createAppointmentSlots(sucursalLaPaz, 30);
            log.info("Institución SEGIP creada");
        }

        // ── SERECI ────────────────────────────────────────────────────────────
        if (institutionRepository.findBySlug("sereci").isEmpty()) {
            Institution sereci = institutionRepository.save(Institution.builder()
                    .name("SERECI").slug("sereci")
                    .institutionType(InstitutionType.PUBLIC)
                    .description("Servicio de Registro Cívico")
                    .contactEmail("contacto@sereci.gob.bo").isActive(true).build());

            Branch sucursalDowntown = branchRepository.save(Branch.builder()
                    .institution(sereci).name("Sucursal Centro")
                    .address("Avenida Camacho esquina Ecuador, La Paz").city("La Paz")
                    .maxDailyAppointments(40).isActive(true).build());

            Procedure certNac = createProcedure(sereci, "Certificado de Nacimiento",
                    "cert-nacimiento", new BigDecimal("20"), new BigDecimal("3"), 3, birthCert);
            Procedure certMat = createProcedure(sereci, "Certificado de Matrimonio",
                    "cert-matrimonio", new BigDecimal("20"), new BigDecimal("3"), 3, marriageCert);

            saveProcReqIfAbsent(certNac, birthCert,    false, "Certificado de Nacimiento original", 1);
            saveProcReqIfAbsent(certNac, ci,           false, "CI válida", 2);
            saveProcReqIfAbsent(certMat, marriageCert, true,  "Certificado de Matrimonio original", 1);
            saveProcReqIfAbsent(certMat, ci,           true,  "CI válida de ambos cónyuges", 2);

            createAppointmentSlots(sucursalDowntown, 30);
            log.info("Institución SERECI creada");
        }

        // ── GOBIERNO MUNICIPAL ────────────────────────────────────────────────
        if (institutionRepository.findBySlug("gob-municipal").isEmpty()) {
            Institution gobMunicipal = institutionRepository.save(Institution.builder()
                    .name("Gobierno Municipal").slug("gob-municipal")
                    .institutionType(InstitutionType.PUBLIC)
                    .description("Gobierno Autónomo Municipal de Santa Cruz de la Sierra")
                    .contactEmail("contacto@municipio.gob.bo").isActive(true).build());

            Branch sucursalMunicipal = branchRepository.save(Branch.builder()
                    .institution(gobMunicipal).name("Oficina Central Municipal")
                    .address("Plaza Principal, Santa Cruz de la Sierra").city("Santa Cruz")
                    .maxDailyAppointments(30).isActive(true).build());

            Procedure certDomProc = createProcedure(gobMunicipal, "Certificado de Domicilio",
                    "cert-domicilio", new BigDecimal("0"), new BigDecimal("0"), 1, certDom);

            saveProcReqIfAbsent(certDomProc, ci,      true,  "CI vigente", 1);
            saveProcReqIfAbsent(certDomProc, certDom, false, "Certificado anterior (si aplica)", 2);

            createAppointmentSlots(sucursalMunicipal, 30);
            log.info("Institución Gobierno Municipal creada");
        }

        // ── BANCO NACIONAL ────────────────────────────────────────────────────
        if (institutionRepository.findBySlug("banco-nacional").isEmpty()) {
            Institution banco = institutionRepository.save(Institution.builder()
                    .name("Banco Nacional de Bolivia").slug("banco-nacional")
                    .institutionType(InstitutionType.PRIVATE)
                    .description("Entidad bancaria nacional")
                    .contactEmail("contacto@bnb.com.bo").isActive(true).build());

            Branch sucursalBanco = branchRepository.save(Branch.builder()
                    .institution(banco).name("Sucursal Centro")
                    .address("Calle Sucre, Santa Cruz de la Sierra").city("Santa Cruz")
                    .maxDailyAppointments(20).isActive(true).build());

            Procedure extractoProc = createProcedure(banco, "Extracto Bancario",
                    "extracto-bancario", new BigDecimal("0"), new BigDecimal("0"), 0, extracto);
            Procedure cuentaProc   = createProcedure(banco, "Apertura de Cuenta Bancaria",
                    "apertura-cuenta", new BigDecimal("0"), new BigDecimal("3"), 1, extracto);

            saveProcReqIfAbsent(extractoProc, ci,       true,  "CI vigente", 1);
            saveProcReqIfAbsent(cuentaProc,   ci,       true,  "CI vigente", 1);
            saveProcReqIfAbsent(cuentaProc,   certDom,  true,  "Certificado de Domicilio", 2);
            saveProcReqIfAbsent(cuentaProc,   certTrab, false, "Certificado de trabajo (si aplica)", 3);

            createAppointmentSlots(sucursalBanco, 30);
            log.info("Institución Banco Nacional creada");
        }

        // ── SEGUROS BOLIVIA ───────────────────────────────────────────────────
        if (institutionRepository.findBySlug("seguros-bolivia").isEmpty()) {
            Institution seguros = institutionRepository.save(Institution.builder()
                    .name("Seguros Bolivia").slug("seguros-bolivia")
                    .institutionType(InstitutionType.PRIVATE)
                    .description("Seguro de salud estatal boliviano")
                    .contactEmail("contacto@seguros.gob.bo").isActive(true).build());

            Branch sucursalSeguros = branchRepository.save(Branch.builder()
                    .institution(seguros).name("Oficina Central Seguros")
                    .address("Av. Monseñor Rivero, Santa Cruz de la Sierra").city("Santa Cruz")
                    .maxDailyAppointments(25).isActive(true).build());

            Procedure seguroProc = createProcedure(seguros, "Afiliación Seguro de Salud",
                    "seguro-salud", new BigDecimal("5"), new BigDecimal("3"), 3, seguroSalud);

            saveProcReqIfAbsent(seguroProc, ci,        true,  "CI vigente", 1);
            saveProcReqIfAbsent(seguroProc, certTrab,  true,  "Certificado de trabajo", 2);
            saveProcReqIfAbsent(seguroProc, birthCert, false, "Partida de nacimiento", 3);
            saveProcReqIfAbsent(seguroProc, foto,      false, "Fotografía reciente 4×4", 4);
            saveProcReqIfAbsent(seguroProc, certDom,   false, "Certificado de domicilio", 5);

            createAppointmentSlots(sucursalSeguros, 30);
            log.info("Institución Seguros Bolivia creada");
        }
    }

    private void initTestUserDocuments() {
        // ── Juan Pérez: CI, Nacimiento, Domicilio, Foto, Trabajo ─────────────
        userRepository.findByIdentification("1234567").ifPresent(u -> {
            createUserDocIfAbsent(u, "CI",           DocumentStatus.active, VerificationStatus.verified,   DocumentSource.platform_generated, "1234567", LocalDate.of(2020, 3, 12),  LocalDate.of(2028, 3, 12));
            createUserDocIfAbsent(u, "BIRTH_CERT",   DocumentStatus.active, VerificationStatus.verified,   DocumentSource.platform_generated, null,      LocalDate.of(1990, 6, 1),   null);
            createUserDocIfAbsent(u, "CERT_DOM",     DocumentStatus.active, VerificationStatus.unverified, DocumentSource.manual_entry,       null,      LocalDate.of(2026, 1, 4),   LocalDate.of(2027, 1, 4));
            createUserDocIfAbsent(u, "FOTO",         DocumentStatus.active, VerificationStatus.unverified, DocumentSource.manual_entry,       null,      LocalDate.of(2026, 2, 18),  null);
            createUserDocIfAbsent(u, "CERT_TRABAJO", DocumentStatus.active, VerificationStatus.unverified, DocumentSource.manual_entry,       null,      LocalDate.of(2025, 12, 10), LocalDate.of(2026, 12, 10));
            log.info("Docs creados para Juan Pérez (1234567)");
        });

        // ── Isabela Ortiz: CI + Nacimiento ───────────────────────────────────
        userRepository.findByIdentification("9876543").ifPresent(u -> {
            createUserDocIfAbsent(u, "CI",         DocumentStatus.active, VerificationStatus.verified, DocumentSource.platform_generated, "9876543", LocalDate.of(2022, 7, 5),  LocalDate.of(2030, 7, 5));
            createUserDocIfAbsent(u, "BIRTH_CERT", DocumentStatus.active, VerificationStatus.verified, DocumentSource.platform_generated, null,      LocalDate.of(1995, 4, 15), null);
            log.info("Docs creados para Isabela Ortiz (9876543)");
        });

        // ── Carlos Mamani: solo CI ────────────────────────────────────────────
        userRepository.findByIdentification("7654321").ifPresent(u -> {
            createUserDocIfAbsent(u, "CI", DocumentStatus.active, VerificationStatus.verified, DocumentSource.platform_generated, "7654321", LocalDate.of(2021, 5, 10), LocalDate.of(2029, 5, 10));
            log.info("Docs creados para Carlos Mamani (7654321) — solo CI");
        });

        // ── Sofía Vargas: CI + Domicilio + Trabajo ────────────────────────────
        userRepository.findByIdentification("5551234").ifPresent(u -> {
            createUserDocIfAbsent(u, "CI",           DocumentStatus.active, VerificationStatus.verified, DocumentSource.platform_generated, "5551234", LocalDate.of(2019, 8, 15),  LocalDate.of(2027, 8, 15));
            createUserDocIfAbsent(u, "CERT_DOM",     DocumentStatus.active, VerificationStatus.verified, DocumentSource.platform_generated, null,      LocalDate.of(2025, 11, 1),  LocalDate.of(2026, 11, 1));
            createUserDocIfAbsent(u, "CERT_TRABAJO", DocumentStatus.active, VerificationStatus.verified, DocumentSource.platform_generated, null,      LocalDate.of(2025, 9, 20),  LocalDate.of(2026, 9, 20));
            log.info("Docs creados para Sofía Vargas (5551234)");
        });

        // ── Pedro Flores: todos los documentos ───────────────────────────────
        userRepository.findByIdentification("3331111").ifPresent(u -> {
            createUserDocIfAbsent(u, "CI",                DocumentStatus.active, VerificationStatus.verified,   DocumentSource.platform_generated, "3331111", LocalDate.of(2022, 1, 5),   LocalDate.of(2030, 1, 5));
            createUserDocIfAbsent(u, "BIRTH_CERT",        DocumentStatus.active, VerificationStatus.verified,   DocumentSource.platform_generated, null,      LocalDate.of(1985, 3, 22),  null);
            createUserDocIfAbsent(u, "CERT_DOM",          DocumentStatus.active, VerificationStatus.verified,   DocumentSource.platform_generated, null,      LocalDate.of(2026, 2, 10),  LocalDate.of(2027, 2, 10));
            createUserDocIfAbsent(u, "CERT_TRABAJO",      DocumentStatus.active, VerificationStatus.verified,   DocumentSource.platform_generated, null,      LocalDate.of(2025, 6, 1),   LocalDate.of(2026, 6, 1));
            createUserDocIfAbsent(u, "FOTO",              DocumentStatus.active, VerificationStatus.unverified, DocumentSource.manual_entry,       null,      LocalDate.of(2026, 3, 1),   null);
            createUserDocIfAbsent(u, "EXTRACTO_BANCARIO", DocumentStatus.active, VerificationStatus.verified,   DocumentSource.platform_generated, null,      LocalDate.of(2026, 3, 15),  LocalDate.of(2026, 9, 15));
            createUserDocIfAbsent(u, "SEGURO_SALUD",      DocumentStatus.active, VerificationStatus.verified,   DocumentSource.platform_generated, null,      LocalDate.of(2026, 1, 1),   LocalDate.of(2027, 1, 1));
            createUserDocIfAbsent(u, "MARRIAGE_CERT",     DocumentStatus.active, VerificationStatus.verified,   DocumentSource.platform_generated, null,      LocalDate.of(2015, 6, 20),  null);
            log.info("Docs creados para Pedro Flores (3331111) — todos los docs");
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * FIX #3: reemplaza los findByCode(...).orElse(null) dispersos dentro de los if-blocks.
     * Lanza excepción clara si el tipo de documento no existe, evitando fallos silenciosos.
     */
    private DocumentType requireDocType(String code) {
        return documentTypeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException(
                        "DocumentType con código '" + code + "' no existe. " +
                                "Verifica que initDocumentTypes() se ejecutó correctamente."));
    }

    private Procedure createProcedure(Institution institution, String name, String code,
                                      BigDecimal basePrice, BigDecimal platformFee,
                                      int estimatedDays, DocumentType outputDocumentType) {
        return procedureRepository.findByCode(code).orElseGet(() ->
                procedureRepository.save(Procedure.builder()
                        .institution(institution).name(name).code(code)
                        .basePrice(basePrice).platformFee(platformFee)
                        .estimatedDays(estimatedDays).outputDocumentType(outputDocumentType)
                        .isActive(true).build())
        );
    }

    private void saveProcReqIfAbsent(Procedure procedure, DocumentType docType,
                                     boolean mandatory, String notes, int order) {
        if (procedure == null || docType == null) return;
        boolean exists = procedureDocumentRequirementRepository
                .findByProcedureIdAndDocumentTypeId(procedure.getId(), docType.getId())
                .isPresent();
        if (!exists) {
            procedureDocumentRequirementRepository.save(ProcedureDocumentRequirement.builder()
                    .procedure(procedure).documentType(docType)
                    .isMandatory(mandatory).notes(notes).displayOrder(order).build());
        }
    }

    private void createUserDocIfAbsent(User user, String docTypeCode,
                                       DocumentStatus status, VerificationStatus verStatus,
                                       DocumentSource source, String docNumber,
                                       LocalDate issueDate, LocalDate expiryDate) {
        DocumentType docType = documentTypeRepository.findByCode(docTypeCode).orElse(null);
        if (docType == null) {
            // FIX #3: ahora loguea el problema en vez de fallar silenciosamente
            log.warn("No se encontró DocumentType con código '{}' — documento omitido para usuario {}",
                    docTypeCode, user.getIdentification());
            return;
        }
        boolean exists = userDocumentRepository
                .findByUserIdAndDocumentTypeCode(user.getId(), docTypeCode)
                .isPresent();
        if (!exists) {
            userDocumentRepository.save(UserDocument.builder()
                    .user(user).documentType(docType).status(status)
                    .verificationStatus(verStatus).source(source)
                    .documentNumber(docNumber).issueDate(issueDate).expiryDate(expiryDate)
                    .build());
        }
    }

    private void createAppointmentSlots(Branch branch, int days) {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalTime[] slotTimes = {
                LocalTime.of(8, 0),  LocalTime.of(8, 30),
                LocalTime.of(9, 0),  LocalTime.of(9, 30),
                LocalTime.of(10, 0), LocalTime.of(10, 30),
                LocalTime.of(14, 0), LocalTime.of(14, 30),
                LocalTime.of(15, 0), LocalTime.of(15, 30),
        };
        for (int i = 0; i < days; i++) {
            LocalDate slotDate = startDate.plusDays(i);
            if (slotDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    slotDate.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
            for (LocalTime slotTime : slotTimes) {
                if (appointmentSlotRepository.findByBranchIdAndSlotDateAndSlotTime(
                        branch.getId(), slotDate, slotTime).isEmpty()) {
                    appointmentSlotRepository.save(AppointmentSlot.builder()
                            .branch(branch).slotDate(slotDate).slotTime(slotTime)
                            .maxCapacity(5).reservedCount(0).isBlocked(false).build());
                }
            }
        }
        log.info("Slots creados para: {}", branch.getName());
    }
}