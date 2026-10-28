package com.autoclean.model;

/**
 * Representa una línea o ítem de costo en una estimación (ej. Champú, Cera, Horas operario).
 */
public class CostLine {

    private String concept;
    private double amount;

    /**
     * Constructor por defecto.
     */
    public CostLine() {
    }

    /**
     * Constructor completo.
     *
     * @param concept descripción o concepto del costo
     * @param amount monto o valor del costo
     */
    public CostLine(String concept, double amount) {
        setConcept(concept);
        setAmount(amount);
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        if (concept == null || concept.trim().isEmpty()) {
            throw new IllegalArgumentException("El concepto no puede estar vacío");
        }
        this.concept = concept.trim();
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
        this.amount = amount;
    }

    @Override
    public String toString() {
        return concept + ": $" + amount;
    }
}
