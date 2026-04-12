import random
from datetime import datetime, timedelta

def generate_massive_sql():
    statuses = ['En attente', 'En transit', 'Livrée', 'Échouée', 'Incident']

    with open('insert_massive_data.sql', 'w', encoding='utf-8') as f:

        # 1. Génération des 20 000 Livraisons (DELIVERIES)
        print("Génération des 20 000 Livraisons en cours...")
        f.write("-- INSERT FOR DELIVERIES (20 000 lignes)\n")

        for i in range(1, 20001):
            order_id = random.randint(1, 50000)

            # 10% de chance de ne pas avoir de livreur (NULL)
            livreur_id = "NULL" if random.random() < 0.10 else random.randint(1, 200)

            depot_id = random.randint(1, 500)
            rate_id = random.randint(1, 500)
            status = random.choice(statuses)

            # 20% de chance de ne pas avoir de date réelle (NULL) si pas encore livré
            if status in ['En attente', 'En transit'] or random.random() < 0.20:
                actual_date = "NULL"
            else:
                # Génère une date aléatoire en 2026
                random_days = random.randint(0, 365)
                date_val = datetime(2026, 1, 1) + timedelta(days=random_days)
                actual_date = f"'{date_val.strftime('%Y-%m-%d')}'"

            f.write(f"INSERT INTO DELIVERIES (DeliveryID, ExternalOrderID, ExternalPrimaryLivreurID, DepotID, RateID, ActualDeliveryDate, Status) VALUES ({i}, {order_id}, {livreur_id}, {depot_id}, {rate_id}, {actual_date}, '{status}');\n")

        # 2. Génération des 20 000 Arrêts (ROUTE_STOPS)
        print("Génération des 20 000 Arrêts en cours...")
        f.write("\n-- INSERT FOR ROUTE_STOPS (20 000 lignes)\n")

        for i in range(1, 20001):
            route_id = random.randint(1, 500)
            delivery_id = i # Doit correspondre exactement de 1 à 20000 (Contrainte UNIQUE)
            stop_sequence = random.randint(1, 15)

            f.write(f"INSERT INTO ROUTE_STOPS (StopID, RouteID, DeliveryID, StopSequence) VALUES ({i}, {route_id}, {delivery_id}, {stop_sequence});\n")

    print("Fichier insert_massive_data.sql généré avec succès ! Tu viens d'économiser 60$.")

if __name__ == "__main__":
    generate_massive_sql()