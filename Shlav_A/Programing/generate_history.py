import random
from datetime import datetime, timedelta

def generate_history():
    statuses = ['En attente', 'En transit', 'Livrée', 'Échouée', 'Incident']
    with open('insert_part2.sql', 'w', encoding='utf-8') as f:
        f.write("-- METHODE 3 : PROGRAMMATION (HISTORIQUE)\n")
        for i in range(1, 501):
            delivery_id = random.randint(1, 20000)
            status = random.choice(statuses)
            date_val = datetime(2026, 1, 1) + timedelta(days=random.randint(0, 60))
            f.write(f"INSERT INTO DELIVERY_STATUS_HISTORY (statushistoryid, deliveryid, status, changeddate) VALUES ({i}, {delivery_id}, '{status}', '{date_val.strftime('%Y-%m-%d %H:%M:%S')}');\n")
    print("✅ Fichier 'insert_part2.sql' généré !")

if __name__ == "__main__":
    generate_history()