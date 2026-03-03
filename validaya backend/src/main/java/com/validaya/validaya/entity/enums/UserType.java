package com.validaya.validaya.entity.enums;

public enum UserType {
    citizen,              // Regular user - acceso solo a sus propios datos
    staff,                // Staff de institución - acceso a datos de su institución
    institution_admin,    // Admin de institución - acceso total a su institución y puede administrar staff
    admin                 // Admin del sistema - acceso total
}
