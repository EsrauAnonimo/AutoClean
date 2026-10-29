# AutoClean - Sistema de Gestión para Centros de Embellecimiento Automotriz

Entrega académica funcional de la capa de presentación (UI) y definición de contratos de arquitectura.

## ⚠️ Advertencia Importante sobre OneDrive
> **ADVERTENCIA TÉCNICA:**
> Este proyecto se encuentra alojado dentro de una carpeta sincronizada por **OneDrive** (`C:\Users\Esteb\OneDrive\Documentos\AutoClean`).
> Para evitar bloqueos de archivos (*file lock*) durante las fases de compilación y empaquetado de Maven:
> 1. El directorio `target/` se encuentra estrictamente ignorado en `.gitignore`.
> 2. Se recomienda pausar temporalmente la sincronización de OneDrive al ejecutar comandos intensivos como `mvn clean compile` o `mvn clean package`.

---

## 📌 Arquitectura y Responsabilidades

El desarrollo se divide bajo un esquema de separación estricta de responsabilidades entre dos desarrolladores:
- **Líder Técnico (Rama `leader`):** Responsable del 100% de la **Capa de Presentación (UI)** construida en Java Swing, definición de contratos (`com.autoclean.contracts`) y modelos compartidos (`com.autoclean.model`). Consume los servicios únicamente a través de interfaces provistas por `ServiceFactory`.
- **Desarrollador 1 (Rama `dev1`):** Responsable del dominio, lógica de negocio, reglas de cálculo y persistencia de datos.

### Nota Crítica para la Fusión de Ramas:
Al fusionar la rama `leader` con la rama `dev1`:
- Se **conservará** la versión de dev1 de `com/autoclean/service/ServiceFactory` y `com/autoclean/service/impl`.
- Se **eliminará** el paquete temporal de stubs: `com/autoclean/service/stub`.
- Los paquetes `com/autoclean/contracts` y `com/autoclean/model` deben permanecer **idénticos** y no ser modificados unilateralmente.

---

## 🚀 Compilación y Ejecución

### Requisitos:
- Java JDK 17 o superior.
- Apache Maven 3.8+.

### Compilación del proyecto:
```bash
mvn compile
```

### Ejecución de la aplicación Swing:
```bash
mvn exec:java
```
