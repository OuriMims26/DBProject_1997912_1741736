-- =========================================================================
-- REQUETE 1 - VERSION A : Utilisation de IN
-- Incidents d'avril 2026 pour les livraisons en transit
-- =========================================================================

SELECT
    di.IncidentID AS "Incident ID",
    di.IncidentType AS "Incident Type",
    di.IncidentDate AS "Incident Date",
    EXTRACT(DAY FROM di.IncidentDate) AS "Incident Day",
    EXTRACT(MONTH FROM di.IncidentDate) AS "Incident Month",
    EXTRACT(YEAR FROM di.IncidentDate) AS "Incident Year",
    d.ExternalOrderID AS "Order Ref",
    dep.DepotName AS "Depot",
    dz.ZoneName AS "Destination Zone",
    dr.WeightClass AS "Weight Class"
FROM DELIVERY_INCIDENTS di
JOIN DELIVERIES d ON d.DeliveryID = di.DeliveryID
JOIN DEPOTS dep ON dep.DepotID = d.DepotID
JOIN DELIVERY_RATES dr ON dr.RateID = d.RateID
JOIN DELIVERY_ZONES dz ON dz.ZoneID = dr.ZoneID
WHERE EXTRACT(MONTH FROM di.IncidentDate) = 4
  AND EXTRACT(YEAR FROM di.IncidentDate) = 2026
  AND di.DeliveryID IN (
      SELECT d_sub.DeliveryID
      FROM DELIVERIES d_sub
      WHERE d_sub.Status = 'En transit'
  )
ORDER BY di.IncidentDate DESC, di.IncidentID ASC;


-- =========================================================================
-- REQUETE 1 - VERSION B : Utilisation de EXISTS
-- Incidents d'avril 2026 pour les livraisons en transit
-- =========================================================================

SELECT
    di.IncidentID AS "Incident ID",
    di.IncidentType AS "Incident Type",
    di.IncidentDate AS "Incident Date",
    EXTRACT(DAY FROM di.IncidentDate) AS "Incident Day",
    EXTRACT(MONTH FROM di.IncidentDate) AS "Incident Month",
    EXTRACT(YEAR FROM di.IncidentDate) AS "Incident Year",
    d.ExternalOrderID AS "Order Ref",
    dep.DepotName AS "Depot",
    dz.ZoneName AS "Destination Zone",
    dr.WeightClass AS "Weight Class"
FROM DELIVERY_INCIDENTS di
JOIN DELIVERIES d ON d.DeliveryID = di.DeliveryID
JOIN DEPOTS dep ON dep.DepotID = d.DepotID
JOIN DELIVERY_RATES dr ON dr.RateID = d.RateID
JOIN DELIVERY_ZONES dz ON dz.ZoneID = dr.ZoneID
WHERE EXTRACT(MONTH FROM di.IncidentDate) = 4
  AND EXTRACT(YEAR FROM di.IncidentDate) = 2026
  AND EXISTS (
      SELECT 1
      FROM DELIVERIES d_sub
      WHERE d_sub.DeliveryID = di.DeliveryID
        AND d_sub.Status = 'En transit'
  )
ORDER BY di.IncidentDate DESC, di.IncidentID ASC;


-- =========================================================================
-- REQUETE 2 - VERSION A : Sous-requetes dans le SELECT
-- Planning detaille des stops d'une route
-- =========================================================================

SELECT
    rs.StopSequence AS "Stop Number",
    d.ExternalOrderID AS "Order Ref",
    d.Status AS "Delivery Status",
    (
        SELECT dr.WeightClass
        FROM DELIVERY_RATES dr
        WHERE dr.RateID = d.RateID
    ) AS "Weight Class",
    (
        SELECT dr.RateAmount
        FROM DELIVERY_RATES dr
        WHERE dr.RateID = d.RateID
    ) AS "Rate Amount",
    (
        SELECT dz.ZoneName
        FROM DELIVERY_RATES dr
        JOIN DELIVERY_ZONES dz ON dz.ZoneID = dr.ZoneID
        WHERE dr.RateID = d.RateID
    ) AS "Destination Zone",
    (
        SELECT dep.DepotName
        FROM DELIVERY_ROUTES r
        JOIN DEPOTS dep ON dep.DepotID = r.DepotID
        WHERE r.RouteID = rs.RouteID
    ) AS "Route Depot",
    (
        SELECT r.ScheduledStartDate
        FROM DELIVERY_ROUTES r
        WHERE r.RouteID = rs.RouteID
    ) AS "Route Date",
    (
        SELECT EXTRACT(DAY FROM r.ScheduledStartDate)
        FROM DELIVERY_ROUTES r
        WHERE r.RouteID = rs.RouteID
    ) AS "Route Day",
    (
        SELECT EXTRACT(MONTH FROM r.ScheduledStartDate)
        FROM DELIVERY_ROUTES r
        WHERE r.RouteID = rs.RouteID
    ) AS "Route Month",
    (
        SELECT EXTRACT(YEAR FROM r.ScheduledStartDate)
        FROM DELIVERY_ROUTES r
        WHERE r.RouteID = rs.RouteID
    ) AS "Route Year"
FROM ROUTE_STOPS rs
JOIN DELIVERIES d ON d.DeliveryID = rs.DeliveryID
WHERE rs.RouteID = 1
ORDER BY rs.StopSequence ASC;


-- =========================================================================
-- REQUETE 2 - VERSION B : Jointures standards
-- Planning detaille des stops d'une route
-- =========================================================================

SELECT
    rs.StopSequence AS "Stop Number",
    d.ExternalOrderID AS "Order Ref",
    d.Status AS "Delivery Status",
    dr.WeightClass AS "Weight Class",
    dr.RateAmount AS "Rate Amount",
    dz.ZoneName AS "Destination Zone",
    dep.DepotName AS "Route Depot",
    r.ScheduledStartDate AS "Route Date",
    EXTRACT(DAY FROM r.ScheduledStartDate) AS "Route Day",
    EXTRACT(MONTH FROM r.ScheduledStartDate) AS "Route Month",
    EXTRACT(YEAR FROM r.ScheduledStartDate) AS "Route Year"
FROM ROUTE_STOPS rs
JOIN DELIVERY_ROUTES r ON r.RouteID = rs.RouteID
JOIN DEPOTS dep ON dep.DepotID = r.DepotID
JOIN DELIVERIES d ON d.DeliveryID = rs.DeliveryID
JOIN DELIVERY_RATES dr ON dr.RateID = d.RateID
JOIN DELIVERY_ZONES dz ON dz.ZoneID = dr.ZoneID
WHERE rs.RouteID = 1
ORDER BY rs.StopSequence ASC;


-- =========================================================================
-- REQUETE 3 - VERSION A : Sous-requetes avec IN
-- Vehicules avec maintenance ancienne dans des depots de capacite superieure a la moyenne
-- =========================================================================

SELECT
    v.VehicleID AS "Vehicle ID",
    v.LicensePlate AS "License Plate",
    v.VehicleType AS "Vehicle Type",
    (
        SELECT dep.DepotName
        FROM DEPOTS dep
        WHERE dep.DepotID = v.DepotID
    ) AS "Depot",
    (
        SELECT dep.StorageCapacity
        FROM DEPOTS dep
        WHERE dep.DepotID = v.DepotID
    ) AS "Depot Capacity",
    v.LastMaintenanceDate AS "Last Maintenance",
    EXTRACT(DAY FROM v.LastMaintenanceDate) AS "Maintenance Day",
    EXTRACT(MONTH FROM v.LastMaintenanceDate) AS "Maintenance Month",
    EXTRACT(YEAR FROM v.LastMaintenanceDate) AS "Maintenance Year",
    CURRENT_DATE - v.LastMaintenanceDate AS "Days Since Maintenance"
FROM VEHICLES v
WHERE v.LastMaintenanceDate < DATE '2026-01-01'
  AND v.DepotID IN (
      SELECT dep.DepotID
      FROM DEPOTS dep
      WHERE dep.StorageCapacity > (
          SELECT AVG(dep_avg.StorageCapacity)
          FROM DEPOTS dep_avg
      )
  )
ORDER BY v.LastMaintenanceDate ASC, v.VehicleID ASC;


-- =========================================================================
-- REQUETE 3 - VERSION B : Jointure avec sous-requete scalaire
-- Vehicules avec maintenance ancienne dans des depots de capacite superieure a la moyenne
-- =========================================================================

SELECT
    v.VehicleID AS "Vehicle ID",
    v.LicensePlate AS "License Plate",
    v.VehicleType AS "Vehicle Type",
    dep.DepotName AS "Depot",
    dep.StorageCapacity AS "Depot Capacity",
    v.LastMaintenanceDate AS "Last Maintenance",
    EXTRACT(DAY FROM v.LastMaintenanceDate) AS "Maintenance Day",
    EXTRACT(MONTH FROM v.LastMaintenanceDate) AS "Maintenance Month",
    EXTRACT(YEAR FROM v.LastMaintenanceDate) AS "Maintenance Year",
    CURRENT_DATE - v.LastMaintenanceDate AS "Days Since Maintenance"
FROM VEHICLES v
JOIN DEPOTS dep ON dep.DepotID = v.DepotID
WHERE v.LastMaintenanceDate < DATE '2026-01-01'
  AND dep.StorageCapacity > (
      SELECT AVG(dep_avg.StorageCapacity)
      FROM DEPOTS dep_avg
  )
ORDER BY v.LastMaintenanceDate ASC, v.VehicleID ASC;


-- =========================================================================
-- REQUETE 4 - VERSION A : Agregation dans une sous-requete
-- Nombre d'incidents par depot, type et mois en 2026
-- =========================================================================

SELECT
    monthly_stats.DepotID AS "Depot ID",
    dep.DepotName AS "Depot",
    monthly_stats.IncidentType AS "Incident Type",
    monthly_stats.IncidentYear AS "Incident Year",
    monthly_stats.IncidentMonth AS "Incident Month",
    monthly_stats.IncidentCount AS "Incident Count",
    monthly_stats.AffectedOrders AS "Affected Orders",
    monthly_stats.FirstIncidentDate AS "First Incident Date",
    monthly_stats.LastIncidentDate AS "Last Incident Date"
FROM (
    SELECT
        d.DepotID,
        di.IncidentType,
        EXTRACT(YEAR FROM di.IncidentDate) AS IncidentYear,
        EXTRACT(MONTH FROM di.IncidentDate) AS IncidentMonth,
        COUNT(*) AS IncidentCount,
        COUNT(DISTINCT d.ExternalOrderID) AS AffectedOrders,
        MIN(di.IncidentDate) AS FirstIncidentDate,
        MAX(di.IncidentDate) AS LastIncidentDate
    FROM DELIVERY_INCIDENTS di
    JOIN DELIVERIES d ON d.DeliveryID = di.DeliveryID
    WHERE EXTRACT(YEAR FROM di.IncidentDate) = 2026
    GROUP BY
        d.DepotID,
        di.IncidentType,
        EXTRACT(YEAR FROM di.IncidentDate),
        EXTRACT(MONTH FROM di.IncidentDate)
) monthly_stats
JOIN DEPOTS dep ON dep.DepotID = monthly_stats.DepotID
ORDER BY
    monthly_stats.IncidentYear,
    monthly_stats.IncidentMonth,
    monthly_stats.DepotID,
    monthly_stats.IncidentType;


-- =========================================================================
-- REQUETE 4 - VERSION B : Agregation avec jointures directes
-- Nombre d'incidents par depot, type et mois en 2026
-- =========================================================================

SELECT
    d.DepotID AS "Depot ID",
    dep.DepotName AS "Depot",
    di.IncidentType AS "Incident Type",
    EXTRACT(YEAR FROM di.IncidentDate) AS "Incident Year",
    EXTRACT(MONTH FROM di.IncidentDate) AS "Incident Month",
    COUNT(*) AS "Incident Count",
    COUNT(DISTINCT d.ExternalOrderID) AS "Affected Orders",
    MIN(di.IncidentDate) AS "First Incident Date",
    MAX(di.IncidentDate) AS "Last Incident Date"
FROM DELIVERY_INCIDENTS di
JOIN DELIVERIES d ON d.DeliveryID = di.DeliveryID
JOIN DEPOTS dep ON dep.DepotID = d.DepotID
WHERE EXTRACT(YEAR FROM di.IncidentDate) = 2026
GROUP BY
    d.DepotID,
    dep.DepotName,
    di.IncidentType,
    EXTRACT(YEAR FROM di.IncidentDate),
    EXTRACT(MONTH FROM di.IncidentDate)
ORDER BY
    "Incident Year",
    "Incident Month",
    "Depot ID",
    "Incident Type";


-- =========================================================================
-- UPDATES
-- =========================================================================

-- UPDATE 1 : Passage d'une livraison au statut "Livree"
UPDATE DELIVERIES
SET
    Status = 'LivrÃ©e',
    ActualDeliveryDate = CURRENT_DATE
WHERE ExternalOrderID = 100001;

-- UPDATE 2 : Augmentation de 5% des tarifs pour la classe de poids standard
UPDATE DELIVERY_RATES
SET RateAmount = RateAmount * 1.05
WHERE WeightClass = 'Standard';

-- UPDATE 3 : Changement de depot pour un vehicule
UPDATE VEHICLES
SET DepotID = 2
WHERE VehicleID = 1;


-- =========================================================================
-- DELETES
-- =========================================================================

-- DELETE 1 : Annulation d'une fausse alerte
DELETE FROM DELIVERY_INCIDENTS
WHERE IncidentID = 1;

-- DELETE 2 : Annulation d'une affectation
DELETE FROM VEHICLE_ASSIGNMENTS
WHERE AssignmentID = 2;

-- DELETE 3 : Annulation d'une affectation
DELETE FROM VEHICLE_ASSIGNMENTS
WHERE AssignmentID = 4;
