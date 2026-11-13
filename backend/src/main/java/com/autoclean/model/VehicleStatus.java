package com.autoclean.model;

/**
 * Estado operativo en tiempo real del vehículo dentro del proceso de lavado y detailing.
 */
public enum VehicleStatus {
    RECEIVED,
    WASHING,
    DETAILING,
    READY,
    DELIVERED
}

