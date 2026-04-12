import random

def generate_sql():
    with open('insert_base_data.sql', 'w', encoding='utf-8') as f:
        # 1. Génération des DEPOTS (500 lignes)
        f.write("-- INSERT FOR DEPOTS\n")
        for i in range(1, 501):
            name = f"Depot_{i}"
            addr = f"{random.randint(1, 999)} Rue de la Logistique, Ville_{i}"
            cap = random.randint(10000, 50000)
            f.write(f"INSERT INTO DEPOTS (DepotID, DepotName, LocationAddress, StorageCapacity) VALUES ({i}, '{name}', '{addr}', {cap});\n")

        # 2. Génération des DELIVERY_ZONES (500 lignes)
        f.write("\n-- INSERT FOR DELIVERY_ZONES\n")
        for i in range(1, 501):
            name = f"Zone_{i}"
            codes = f"{random.randint(10000, 99000)}"
            f.write(f"INSERT INTO DELIVERY_ZONES (ZoneID, ZoneName, PostalCodesCovered) VALUES ({i}, '{name}', '{codes}');\n")

        # 3. Génération des VEHICLES (500 lignes)
        f.write("\n-- INSERT FOR VEHICLES\n")
        types = ['Camionnette', 'Scooter', 'Velo', 'Camion']
        for i in range(1, 501):
            v_type = random.choice(types)
            plate = f"{random.randint(100, 999)}-{random.randint(10, 99)}-{random.randint(100, 999)}"
            depot_id = random.randint(1, 500)
            vol = random.randint(5, 50)
            weight = random.randint(100, 2000)
            f.write(f"INSERT INTO VEHICLES (VehicleID, DepotID, VehicleType, LicensePlate, CapacityVolume, CapacityWeight) VALUES ({i}, {depot_id}, '{v_type}', '{plate}', {vol}, {weight});\n")

    print("Fichier insert_base_data.sql généré avec succès !")

if __name__ == "__main__":
    generate_sql()