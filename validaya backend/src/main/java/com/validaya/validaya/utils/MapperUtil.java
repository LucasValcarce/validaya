package com.validaya.validaya.utils;

import com.validaya.validaya.entity.*;
import com.validaya.validaya.entity.dto.*;

import java.math.BigDecimal;

/**
 * Utilidad de mapeo entre entidades JPA y DTOs.
 * Se usa método estático para mantener la lógica centralizada.
 */
public class MapperUtil {

    // ── User ──────────────────────────────────────────────────────────────
    public static UserDto.Response toUserResponse(User user) {
        UserDto.Response dto = new UserDto.Response();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setIdentification(user.getIdentification());
        dto.setPhone(user.getPhone());
        dto.setBirthDate(user.getBirthDate());
        dto.setUserType(user.getUserType());
        dto.setFaceVerified(user.getFaceVerified());
        dto.setFaceRegisteredAt(user.getFaceRegisteredAt());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    // ── ApplicationDocument ───────────────────────────────────────────────
    public static ApplicationDocumentDto.Response toAppDocResponse(ApplicationDocument doc) {
        ApplicationDocumentDto.Response dto = new ApplicationDocumentDto.Response();
        dto.setId(doc.getId());
        dto.setApplicationId(doc.getApplication().getId());
        dto.setUserDocumentId(doc.getUserDocument().getId());
        dto.setDocumentTypeName(doc.getUserDocument().getDocumentType().getName());
        dto.setVerificationStatus(doc.getVerificationStatus());

        // If your DTO has these fields, map them from entity equivalents:
        dto.setRejectionReason(doc.getComments());
        dto.setReviewedAt(doc.getVerifiedAt());

        return dto;
    }

    // ── UserDocument ──────────────────────────────────────────────────────
    public static UserDocumentDto.Response toUserDocumentResponse(UserDocument doc) {
        UserDocumentDto.Response dto = new UserDocumentDto.Response();
        dto.setId(doc.getId());
        dto.setUserId(doc.getUser().getId());
        dto.setDocumentTypeId(doc.getDocumentType().getId());
        dto.setDocumentTypeName(doc.getDocumentType().getName());
        dto.setDocumentNumber(doc.getDocumentNumber());
        dto.setIssueDate(doc.getIssueDate());
        dto.setExpiryDate(doc.getExpiryDate());
        dto.setStatus(doc.getStatus());
        dto.setVerificationStatus(doc.getVerificationStatus());
        dto.setSource(doc.getSource());
        dto.setCreatedAt(doc.getCreatedAt());
        return dto;
    }

    // ── Institution ───────────────────────────────────────────────────────
    public static InstitutionDto.Response toInstitutionResponse(Institution inst) {
        InstitutionDto.Response dto = new InstitutionDto.Response();
        dto.setId(inst.getId());
        dto.setName(inst.getName());
        dto.setSlug(inst.getSlug());
        dto.setType(inst.getInstitutionType());
        dto.setDescription(inst.getDescription());
        dto.setLogoUrl(inst.getLogoPath());
        dto.setWebsite(null); // no field in entity
        dto.setContactEmail(inst.getContactEmail());
        dto.setContactPhone(inst.getContactPhone());
        dto.setIsActive(inst.getIsActive());
        return dto;
    }

    public static InstitutionDto.Summary toInstitutionSummary(Institution inst) {
        InstitutionDto.Summary dto = new InstitutionDto.Summary();
        dto.setId(inst.getId());
        dto.setName(inst.getName());
        dto.setSlug(inst.getSlug());
        dto.setType(inst.getInstitutionType());
        dto.setLogoUrl(inst.getLogoPath());
        return dto;
    }

    // ── Branch ────────────────────────────────────────────────────────────
    public static BranchDto.Response toBranchResponse(Branch branch) {
        BranchDto.Response dto = new BranchDto.Response();
        dto.setId(branch.getId());
        dto.setInstitutionId(branch.getInstitution().getId());
        dto.setInstitutionName(branch.getInstitution().getName());
        dto.setName(branch.getName());
        dto.setAddress(branch.getAddress());
        dto.setCity(branch.getCity());
        dto.setLatitude(branch.getLat());
        dto.setLongitude(branch.getLng());
        dto.setPhone(null); // no field in entity
        dto.setMaxDailyAppointments(branch.getMaxDailyAppointments());
        dto.setIsActive(branch.getIsActive());
        return dto;
    }

    // ── Procedure ─────────────────────────────────────────────────────────
    public static ProcedureDto.Response toProcedureResponse(Procedure proc) {
        ProcedureDto.Response dto = new ProcedureDto.Response();
        dto.setId(proc.getId());
        dto.setInstitutionId(proc.getInstitution().getId());
        dto.setInstitutionName(proc.getInstitution().getName());
        dto.setName(proc.getName());
        dto.setSlug(proc.getCode()); // DTO uses "slug", entity uses "code"
        dto.setDescription(proc.getDescription());
        dto.setEstimatedDays(proc.getEstimatedDays());
        dto.setBasePrice(proc.getBasePrice());
        dto.setPlatformFee(proc.getPlatformFee());

        if (proc.getBasePrice() != null && proc.getPlatformFee() != null) {
            dto.setTotalPrice(proc.getBasePrice().add(proc.getPlatformFee()));
        } else if (proc.getBasePrice() != null) {
            dto.setTotalPrice(proc.getBasePrice());
        } else if (proc.getPlatformFee() != null) {
            dto.setTotalPrice(proc.getPlatformFee());
        }

        dto.setIsActive(proc.getIsActive());

        if (proc.getOutputDocumentType() != null) {
            dto.setOutputDocumentTypeId(proc.getOutputDocumentType().getId());
            dto.setOutputDocumentTypeName(proc.getOutputDocumentType().getName());
        }

        return dto;
    }

    public static ProcedureDto.Summary toProcedureSummary(Procedure proc) {
        ProcedureDto.Summary dto = new ProcedureDto.Summary();
        dto.setId(proc.getId());
        dto.setName(proc.getName());
        dto.setSlug(proc.getCode());
        dto.setInstitutionName(proc.getInstitution().getName());
        dto.setEstimatedDays(proc.getEstimatedDays());

        BigDecimal total = proc.getBasePrice() != null ? proc.getBasePrice() : BigDecimal.ZERO;
        if (proc.getPlatformFee() != null) total = total.add(proc.getPlatformFee());
        dto.setTotalPrice(total);

        return dto;
    }

    // ── Application ───────────────────────────────────────────────────────
    public static ApplicationDto.Response toApplicationResponse(Application app) {
        ApplicationDto.Response dto = new ApplicationDto.Response();
        dto.setId(app.getId());
        dto.setApplicationNumber("APP-" + app.getId()); // computed (entity has no applicationNumber column)
        dto.setUserId(app.getUser().getId());
        dto.setUserName(app.getUser().getFullName());
        dto.setProcedureId(app.getProcedure().getId());
        dto.setProcedureName(app.getProcedure().getName());

        // derived
        dto.setInstitutionId(app.getProcedure().getInstitution().getId());
        dto.setInstitutionName(app.getProcedure().getInstitution().getName());

        if (app.getBranch() != null) {
            dto.setBranchId(app.getBranch().getId());
            dto.setBranchName(app.getBranch().getName());
        }

        dto.setStatus(app.getStatus());
        dto.setTotalAmount(app.getTotalAmount());

        // fields not present in entity -> keep null
        dto.setNotes(null);
        dto.setRejectionReason(app.getRejectionReason());
        dto.setSubmittedAt(null);
        dto.setCompletedAt(app.getCompletedAt());
        dto.setCreatedAt(app.getCreatedAt());

        return dto;
    }

    public static ApplicationDto.Summary toApplicationSummary(Application app) {
        ApplicationDto.Summary dto = new ApplicationDto.Summary();
        dto.setId(app.getId());
        dto.setApplicationNumber("APP-" + app.getId());
        dto.setProcedureName(app.getProcedure().getName());
        dto.setInstitutionName(app.getProcedure().getInstitution().getName());
        dto.setStatus(app.getStatus());
        dto.setCreatedAt(app.getCreatedAt());
        return dto;
    }

    // ── Payment ───────────────────────────────────────────────────────────
    public static PaymentDto.Response toPaymentResponse(Payment payment) {
        PaymentDto.Response dto = new PaymentDto.Response();
        dto.setId(payment.getId());
        dto.setApplicationId(payment.getApplication().getId());
        dto.setApplicationNumber("APP-" + payment.getApplication().getId());
        dto.setTransactionId(payment.getTransactionId());
        dto.setAmount(payment.getAmount());
        dto.setPlatformFee(payment.getPlatformFee());
        dto.setInstitutionAmount(payment.getInstitutionAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getPaymentStatus());
        dto.setPaidAt(payment.getPaidAt());
        dto.setCreatedAt(payment.getCreatedAt());
        
        // Extraer URL de pago desde la respuesta del gateway
        if (payment.getGatewayResponse() != null && payment.getGatewayResponse().containsKey("payment_url")) {
            dto.setGatewayUrl((String) payment.getGatewayResponse().get("payment_url"));
        }
        
        return dto;
    }

    // ── Ticket ────────────────────────────────────────────────────────────
    public static TicketDto.Response toTicketResponse(Ticket ticket) {
        TicketDto.Response dto = new TicketDto.Response();
        dto.setId(ticket.getId());
        dto.setCode(ticket.getTicketCode());
        dto.setApplicationId(ticket.getApplication().getId());
        dto.setApplicationNumber("APP-" + ticket.getApplication().getId());
        dto.setStatus(ticket.getStatus());
        dto.setQrPayload(ticket.getQrPayload());
        dto.setExpiryAt(ticket.getExpiryAt());
        dto.setUsedAt(ticket.getUsedAt());
        dto.setCreatedAt(ticket.getIssuedAt());
        return dto;
    }

    // ── Appointment ───────────────────────────────────────────────────────
    public static AppointmentDto.Response toAppointmentResponse(Appointment appointment) {
        AppointmentDto.Response dto = new AppointmentDto.Response();
        dto.setId(appointment.getId());
        dto.setTicketId(appointment.getTicket().getId());
        dto.setTicketCode(appointment.getTicket().getTicketCode());
        dto.setBranchId(appointment.getBranch().getId());
        dto.setBranchName(appointment.getBranch().getName());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setStaffNotes(null);       // not present in entity
        dto.setConfirmedAt(null);      // not present in entity
        dto.setCreatedAt(appointment.getCreatedAt());
        return dto;
    }

    public static AppointmentDto.SlotResponse toSlotResponse(AppointmentSlot slot) {
        AppointmentDto.SlotResponse dto = new AppointmentDto.SlotResponse();
        dto.setId(slot.getId());
        dto.setDate(slot.getSlotDate());
        dto.setStartTime(slot.getSlotTime());
        dto.setEndTime(null); // entity does not have end time
        dto.setCapacity(slot.getMaxCapacity());
        dto.setBooked(slot.getReservedCount());
        dto.setAvailable(slot.getReservedCount() < slot.getMaxCapacity() && !Boolean.TRUE.equals(slot.getIsBlocked()));
        return dto;
    }

    // ── InstitutionStaff ──────────────────────────────────────────────────
    public static InstitutionStaffDto.Response toInstitutionStaffResponse(InstitutionStaff staff) {
        InstitutionStaffDto.Response dto = new InstitutionStaffDto.Response();
        dto.setId(staff.getId());
        dto.setUserId(staff.getUser().getId());
        dto.setUserEmail(staff.getUser().getEmail());
        dto.setUserFullName(staff.getUser().getFullName());
        dto.setUserIdentification(staff.getUser().getIdentification());
        dto.setUserType(staff.getUser().getUserType() != null ? staff.getUser().getUserType().name() : null);
        dto.setInstitutionId(staff.getInstitution().getId());
        dto.setInstitutionName(staff.getInstitution().getName());
        dto.setEmployeeCode(staff.getEmployeeCode());
        dto.setBranchId(staff.getBranch().getId());
        dto.setIsActive(staff.getIsActive());
        dto.setAssignedAt(staff.getAssignedAt());
        return dto;
    }

    private MapperUtil() {}
}