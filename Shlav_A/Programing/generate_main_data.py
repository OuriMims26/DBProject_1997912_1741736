import random
from datetime import datetime, timedelta

def generate_sql_file():
    print("Génération en cours... (ça prend 2 secondes)")

    with open('insert_part1.sql', 'w', encoding='utf-8') as f:
        f.write("-- ==========================================\n")
        f.write("-- METHODE 1 : INSERTION SCRIPT PYTHON (7 TABLES)\n")
        f.write("-- ==========================================\n\n")

        # 1. DELIVERY_ZONES (500)
        f.write("-- Table : DELIVERY_ZONES\n")
        for i in range(1, 501):
            f.write(f"INSERT INTO DELIVERY_ZONES (ZoneID, ZoneName, PostalCodesCovered) VALUES ({i}, 'Zone_{i}', '7500{i%9}');\n")

        # 2. DELIVERY_RATES (500)
        f.write("\n-- Table : DELIVERY_RATES\n")
        for i in range(1, 501):
            f.write(f"INSERT INTO DELIVERY_RATES (RateID, ZoneID, WeightClass, RateAmount, EffectiveDate) VALUES ({i}, {i}, 'Standard', {random.randint(10, 100)}, '2025-01-01');\n")

        # 3. DEPOTS (500)
        f.write("\n-- Table : DEPOTS\n")
        for i in range(1, 501):
            f.write(f"INSERT INTO DEPOTS (DepotID, DepotName, LocationAddress, StorageCapacity) VALUES ({i}, 'Depot_{i}', 'Adresse_{i}', {random.randint(1000, 5000)});\n")

        # 4. VEHICLES (500) - AVEC TES MOTS EXACTS SANS ACCENT
        f.write("\n-- Table : VEHICLES\n")
        types_autorises = ['Camionnette', 'Scooter', 'Velo', 'Camion']
        for i in range(1, 501):
            v_type = random.choice(types_autorises)
            depot_id = random.randint(1, 500)
            f.write(f"INSERT INTO VEHICLES (VehicleID, DepotID, VehicleType, LicensePlate, CapacityVolume, CapacityWeight, LastMaintenanceDate) VALUES ({i}, {depot_id}, '{v_type}', 'PLQ-{i}', {random.randint(10, 100)}, {random.randint(500, 3000)}, '2025-06-01');\n")

        # 5. DELIVERY_ROUTES (500)
        f.write("\n-- Table : DELIVERY_ROUTES\n")
        route_status = ['Planifiée', 'Active', 'Terminée', 'Annulée']
        for i in range(1, 501):
            f.write(f"INSERT INTO DELIVERY_ROUTES (RouteID, DepotID, RouteName, ScheduledStartDate, Status) VALUES ({i}, {random.randint(1, 500)}, 'Route_{i}', '2026-01-10', '{random.choice(route_status)}');\n")

        # 6. DELIVERIES (20 000)
        f.write("\n-- Table : DELIVERIES (20 000 lignes)\n")
        deliv_status = ['En attente', 'En transit', 'Livrée', 'Échouée', 'Incident']
        for i in range(1, 20001):
            depot_id = random.randint(1, 500)
            rate_id = random.randint(1, 500)
            f.write(f"INSERT INTO DELIVERIES (DeliveryID, ExternalOrderID, ExternalPrimaryLivreurID, DepotID, RateID, ActualDeliveryDate, Status) VALUES ({i}, {100000+i}, {random.randint(1, 200)}, {depot_id}, {rate_id}, '2026-02-15', '{random.choice(deliv_status)}');\n")

        # 7. ROUTE_STOPS (20 000)
        f.write("\n-- Table : ROUTE_STOPS (20 000 lignes)\n")
        for i in range(1, 20001):
            route_id = random.randint(1, 500)
            f.write(f"INSERT INTO ROUTE_STOPS (StopID, RouteID, DeliveryID, StopSequence) VALUES ({i}, {route_id}, {i}, {random.randint(1, 10)});\n")

    print("✅ Succès absolu ! Fichier 'insert_part1.sql' généré et adapté à tes tables.")

if __name__ == "__main__":
    generate_sql_file()