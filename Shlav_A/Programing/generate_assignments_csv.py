import csv
import random
from datetime import datetime, timedelta

def generate_assignments_csv():
    with open('assignments.csv', 'w', newline='', encoding='utf-8') as file:
        writer = csv.writer(file)
        # Création de la ligne des titres (Header)
        writer.writerow(['AssignmentID', 'ExternalLivreurID', 'VehicleID', 'StartDate', 'EndDate'])

        for i in range(1, 501):
            livreur_id = random.randint(1, 200)
            vehicle_id = random.randint(1, 500)
            start_date = datetime(2025, 1, 1) + timedelta(days=random.randint(0, 100))
            end_date = start_date + timedelta(days=random.randint(1, 30))

            writer.writerow([i, livreur_id, vehicle_id, start_date.strftime('%Y-%m-%d'), end_date.strftime('%Y-%m-%d')])

    print("✅ Fichier 'assignments.csv' généré avec succès !")

if __name__ == "__main__":
    generate_assignments_csv()