CREATE TABLE DEPOTS
(
  DepotID INT NOT NULL,
  DepotName VARCHAR(100) NOT NULL,
  LocationAddress VARCHAR(255) NOT NULL,
  StorageCapacity NUMERIC(12,2) CHECK (StorageCapacity >= 0), -- Contrainte: Capacité positive
  PRIMARY KEY (DepotID)
);

CREATE TABLE DELIVERY_ZONES
(
  ZoneID INT NOT NULL,
  ZoneName VARCHAR(100) NOT NULL,
  PostalCodesCovered VARCHAR(4000) NOT NULL,
  PRIMARY KEY (ZoneID)
);

CREATE TABLE DELIVERY_RATES
(
  RateID INT NOT NULL,
  ZoneID INT NOT NULL,
  WeightClass VARCHAR(50) NOT NULL,
  RateAmount NUMERIC(10,2) NOT NULL CHECK (RateAmount >= 0), -- Contrainte: Prix positif
  EffectiveDate DATE NOT NULL,
  PRIMARY KEY (RateID),
  FOREIGN KEY (ZoneID) REFERENCES DELIVERY_ZONES(ZoneID)
);

CREATE TABLE VEHICLES
(
  VehicleID INT NOT NULL,
  DepotID INT NOT NULL,
  VehicleType VARCHAR(100) NOT NULL CHECK (VehicleType IN ('Camionnette', 'Scooter', 'Velo', 'Camion')), -- Contrainte: Types valides
  LicensePlate VARCHAR(20) NOT NULL UNIQUE,
  CapacityVolume NUMERIC(10,2) CHECK (CapacityVolume >= 0),
  CapacityWeight NUMERIC(10,2) CHECK (CapacityWeight >= 0),
  LastMaintenanceDate DATE,
  PRIMARY KEY (VehicleID),
  FOREIGN KEY (DepotID) REFERENCES DEPOTS(DepotID)
);

CREATE TABLE VEHICLE_ASSIGNMENTS
(
  AssignmentID INT NOT NULL,
  ExternalLivreurID INT NOT NULL,
  VehicleID INT NOT NULL,
  StartDate DATE NOT NULL,
  EndDate DATE CHECK (EndDate IS NULL OR EndDate >= StartDate), -- Contrainte: Fin après le début
  PRIMARY KEY (AssignmentID),
  FOREIGN KEY (VehicleID) REFERENCES VEHICLES(VehicleID)
);

CREATE TABLE DELIVERY_ROUTES
(
  RouteID INT NOT NULL,
  DepotID INT NOT NULL,
  RouteName VARCHAR(100) NOT NULL,
  ScheduledStartDate DATE NOT NULL,
  Status VARCHAR(50) NOT NULL CHECK (Status IN ('Planifiée', 'Active', 'Terminée', 'Annulée')), -- Contrainte: Statuts logiques
  PRIMARY KEY (RouteID),
  FOREIGN KEY (DepotID) REFERENCES DEPOTS(DepotID)
);

CREATE TABLE DELIVERIES
(
  DeliveryID INT NOT NULL,
  ExternalOrderID INT NOT NULL,
  ExternalPrimaryLivreurID INT,
  DepotID INT NOT NULL,
  RateID INT NOT NULL,
  ActualDeliveryDate DATE,
  Status VARCHAR(50) NOT NULL CHECK (Status IN ('En attente', 'En transit', 'Livrée', 'Échouée', 'Incident')),
  PRIMARY KEY (DeliveryID),
  FOREIGN KEY (DepotID) REFERENCES DEPOTS(DepotID),
  FOREIGN KEY (RateID) REFERENCES DELIVERY_RATES(RateID)
);

CREATE TABLE ROUTE_STOPS
(
  StopID INT NOT NULL,
  RouteID INT NOT NULL,
  DeliveryID INT NOT NULL UNIQUE,
  StopSequence INT NOT NULL CHECK (StopSequence > 0), -- Contrainte: L'ordre doit être un entier positif
  PRIMARY KEY (StopID),
  FOREIGN KEY (RouteID) REFERENCES DELIVERY_ROUTES(RouteID),
  FOREIGN KEY (DeliveryID) REFERENCES DELIVERIES(DeliveryID)
);

CREATE TABLE DELIVERY_STATUS_HISTORY
(
  StatusHistoryID INT NOT NULL,
  DeliveryID INT NOT NULL,
  Status VARCHAR(50) NOT NULL CHECK (Status IN ('En attente', 'En transit', 'Livrée', 'Échouée', 'Incident')),
  ChangedDate TIMESTAMP NOT NULL,
  PRIMARY KEY (StatusHistoryID),
  FOREIGN KEY (DeliveryID) REFERENCES DELIVERIES(DeliveryID)
);

CREATE TABLE DELIVERY_INCIDENTS
(
  IncidentID INT NOT NULL,
  DeliveryID INT NOT NULL,
  ExternalLivreurID INT,
  IncidentType VARCHAR(100) NOT NULL,
  IncidentDate DATE NOT NULL,
  Description VARCHAR(4000) NOT NULL,
  PRIMARY KEY (IncidentID),
  FOREIGN KEY (DeliveryID) REFERENCES DELIVERIES(DeliveryID)
);