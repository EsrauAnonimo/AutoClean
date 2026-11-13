package com.autoclean.rules;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.autoclean.rules.spec.LegacyMirrorModelValidationSpec;
import com.autoclean.rules.spec.ModelValidationSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Especificación de validaciones de dominio para Customer, Vehicle y ServiceType (AGENTS.md §8).
 */
class ModelValidationTest {

    private ModelValidationSpec validator;

    @BeforeEach
    void setUp() {
        validator = new LegacyMirrorModelValidationSpec();
    }

    @Test
    @DisplayName("Customer con nombre, teléfono y correo vacíos o nulos debe lanzar IllegalArgumentException")
    void shouldRejectEmptyCustomerFields() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateCustomer("", "3001234567", "ana@mail.com"));
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateCustomer("Ana Ruiz", "   ", "ana@mail.com"));
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateCustomer("Ana Ruiz", "3001234567", null));
    }

    @ParameterizedTest
    @ValueSource(ints = {1899, 1500, 2101, 2200})
    @DisplayName("Vehicle con año fuera de [1900, 2100] debe lanzar IllegalArgumentException")
    void shouldRejectVehicleYearOutOfRange(int invalidYear) {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateVehicle("cli-1", "ABC123", "Toyota", "Corolla", invalidYear));
    }

    @ParameterizedTest
    @ValueSource(ints = {1900, 2024, 2100})
    @DisplayName("Vehicle con año dentro de [1900, 2100] inclusive debe ser válido")
    void shouldAcceptVehicleYearWithinRange(int validYear) {
        assertDoesNotThrow(
                () -> validator.validateVehicle("cli-1", "ABC123", "Toyota", "Corolla", validYear));
    }

    @Test
    @DisplayName("Vehicle sin placa, marca, modelo o cliente debe lanzar IllegalArgumentException")
    void shouldRejectMissingVehicleRequiredFields() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateVehicle("cli-1", "", "Toyota", "Corolla", 2022));
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateVehicle("cli-1", "ABC123", "  ", "Corolla", 2022));
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateVehicle("cli-1", "ABC123", "Toyota", null, 2022));
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateVehicle("", "ABC123", "Toyota", "Corolla", 2022));
    }

    @Test
    @DisplayName("ServiceType con precio negativo o nombre vacío debe lanzar IllegalArgumentException")
    void shouldRejectNegativePriceOrBlankNameInServiceType() {
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateServiceType("Lavado Express", -0.01));
        assertThrows(IllegalArgumentException.class,
                () -> validator.validateServiceType("  ", 25000.0));
        assertDoesNotThrow(
                () -> validator.validateServiceType("Cortesía", 0.0));
    }
}
