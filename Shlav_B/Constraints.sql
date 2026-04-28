-- =========================================================================
-- CONSTRAINTS - contraintes ajoutees pour le stage B
-- =========================================================================

-- 1. Contrainte metier: une plaque d'immatriculation ne doit pas etre vide.
-- Cette contrainte n'existait pas dans createTables.sql.
ALTER TABLE VEHICLES
    ADD CONSTRAINT chk_vehicle_licenseplate_not_blank
        CHECK (LENGTH(TRIM(LicensePlate)) > 0);

-- 2. Contrainte metier: la capacite de stockage d'un depot doit etre strictement positive.
-- Le schema initial verifiait seulement StorageCapacity >= 0.
ALTER TABLE DEPOTS
    ADD CONSTRAINT chk_depot_storage_capacity_strictly_positive
        CHECK (StorageCapacity > 0);

-- 3. Contrainte metier: le type d'incident ne doit pas etre vide.
-- Cette contrainte passe sur les donnees existantes et bloque les incidents sans type exploitable.
ALTER TABLE DELIVERY_INCIDENTS
    ADD CONSTRAINT chk_incident_type_not_blank
        CHECK (LENGTH(TRIM(IncidentType)) > 0);

