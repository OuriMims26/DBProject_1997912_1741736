# דוח הפרויקט - שלב ב: שאילתות ואילוצים

## הקדמה

בשלב זה ביצענו תשאול של בסיס הנתונים של מערכת הלוגיסטיקה והמשלוחים. המטרה היא להציג שאילתות שימושיות למסכי המערכת, לבדוק דרכי כתיבה שונות לאותה תוצאה, להבין יעילות של שאילתות, ולהוסיף אילוצים ואינדקסים לשיפור אמינות ויעילות בסיס הנתונים.

הקבצים של שלב ב:

- `Queries.sql` - שמונה שאילתות `SELECT`, שלוש שאילתות `UPDATE`, ושלוש שאילתות `DELETE`.
- `Constraints.sql` - שלושה אילוצים חדשים בעזרת `ALTER TABLE`.
- `Index.sql` - שלושה אינדקסים חדשים.
- `RollbackCommit.sql` - דוגמאות ל-`ROLLBACK` ול-`COMMIT`.
- `backup2` - גיבוי מעודכן של בסיס הנתונים.

כל שאילתות ה-`SELECT` מחזירות יותר משתי עמודות, משתמשות בכמה טבלאות או בתתי שאילתות, ומשלבות שדות תאריך בעזרת `EXTRACT`.

## שאילתות SELECT כפולות

### שאילתה 1 - אירועים בחודש אפריל 2026 עבור משלוחים בסטטוס "En transit"

מטרת השאילתה היא להציג למסך התראות אירועים: מזהה אירוע, סוג אירוע, תאריך, יום/חודש/שנה, מספר הזמנה, מחסן, אזור יעד וקטגוריית משקל. השאילתה מתאימה למסך ניהול תקלות בזמן משלוח.

#### גרסה A - שימוש ב-IN

```sql
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
```

#### גרסה B - שימוש ב-EXISTS

```sql
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
```

#### צילום הרצה ותוצאה

![Query 1 version A](img_5.png)

![Query 1 version B](img_7.png)

#### הבדל יעילות

בגרסה A, `IN` בונה רשימת מזהי משלוחים ואז בודק אם המשלוח של האירוע נמצא ברשימה. בגרסה B, `EXISTS` בודק עבור כל אירוע אם קיימת רשומה מתאימה בטבלת `DELIVERIES`. כאשר יש אינדקסים על `DeliveryID` ו-`Status`, `EXISTS` יכול להיות יעיל יותר כי הוא יכול לעצור מיד כשהוא מוצא התאמה.

### שאילתה 2 - תכנון עצירות במסלול

השאילתה מציגה את עצירות המסלול לפי סדר: מספר עצירה, מספר הזמנה, סטטוס משלוח, קטגוריית משקל, מחיר, אזור יעד, מחסן ותאריך המסלול מפורק ליום/חודש/שנה. השאילתה מתאימה למסך תכנון מסלול.

#### גרסה A - תתי שאילתות בתוך SELECT

```sql
SELECT
    rs.StopSequence AS "Stop Number",
    d.ExternalOrderID AS "Order Ref",
    d.Status AS "Delivery Status",
    (SELECT dr.WeightClass FROM DELIVERY_RATES dr WHERE dr.RateID = d.RateID) AS "Weight Class",
    (SELECT dr.RateAmount FROM DELIVERY_RATES dr WHERE dr.RateID = d.RateID) AS "Rate Amount",
    (SELECT dz.ZoneName
     FROM DELIVERY_RATES dr
     JOIN DELIVERY_ZONES dz ON dz.ZoneID = dr.ZoneID
     WHERE dr.RateID = d.RateID) AS "Destination Zone",
    (SELECT dep.DepotName
     FROM DELIVERY_ROUTES r
     JOIN DEPOTS dep ON dep.DepotID = r.DepotID
     WHERE r.RouteID = rs.RouteID) AS "Route Depot",
    (SELECT r.ScheduledStartDate FROM DELIVERY_ROUTES r WHERE r.RouteID = rs.RouteID) AS "Route Date",
    (SELECT EXTRACT(DAY FROM r.ScheduledStartDate) FROM DELIVERY_ROUTES r WHERE r.RouteID = rs.RouteID) AS "Route Day",
    (SELECT EXTRACT(MONTH FROM r.ScheduledStartDate) FROM DELIVERY_ROUTES r WHERE r.RouteID = rs.RouteID) AS "Route Month",
    (SELECT EXTRACT(YEAR FROM r.ScheduledStartDate) FROM DELIVERY_ROUTES r WHERE r.RouteID = rs.RouteID) AS "Route Year"
FROM ROUTE_STOPS rs
JOIN DELIVERIES d ON d.DeliveryID = rs.DeliveryID
WHERE rs.RouteID = 1
ORDER BY rs.StopSequence ASC;
```

#### גרסה B - JOIN רגיל

```sql
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
```

#### צילום הרצה ותוצאה

![Query 2 version A](img_8.png)

![Query 2 version B](img_9.png)

#### הבדל יעילות

גרסה A מריצה כמה תתי שאילתות עבור כל שורה בתוצאה, ולכן עלולה להיות איטית יותר במסלול עם הרבה עצירות. גרסה B משתמשת ב-`JOIN`, ולכן PostgreSQL יכול לבנות תוכנית ביצוע יעילה יותר ולנצל אינדקסים על מפתחות זרים. לכן גרסה B עדיפה בדרך כלל.

### שאילתה 3 - רכבים שדורשים תשומת לב תחזוקתית במחסנים גדולים

השאילתה מציגה רכבים שהתחזוקה האחרונה שלהם לפני 2026, ונמצאים במחסנים שקיבולת האחסון שלהם גבוהה מהממוצע. השאילתה מתאימה למסך ניהול צי רכבים ותחזוקה.

#### גרסה A - IN ותתי שאילתות

```sql
SELECT
    v.VehicleID AS "Vehicle ID",
    v.LicensePlate AS "License Plate",
    v.VehicleType AS "Vehicle Type",
    (SELECT dep.DepotName FROM DEPOTS dep WHERE dep.DepotID = v.DepotID) AS "Depot",
    (SELECT dep.StorageCapacity FROM DEPOTS dep WHERE dep.DepotID = v.DepotID) AS "Depot Capacity",
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
```

#### גרסה B - JOIN ותת שאילתה לחישוב ממוצע

```sql
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
```

#### צילום הרצה ותוצאה

![Query 3 version A](img_16.png)

![Query 3 version B](img_15.png)

#### הבדל יעילות

גרסה A משתמשת ב-`IN` ובתתי שאילתות כדי להביא את שם וקיבולת המחסן. גרסה B מחברת את `VEHICLES` ל-`DEPOTS` בעזרת `JOIN`, ולכן היא ברורה יותר ויעילה יותר כאשר יש אינדקס על `DepotID`. שתי הגרסאות מחזירות את אותה תוצאה, אבל גרסה B מתאימה יותר לבסיס נתונים גדול.

### שאילתה 4 - דוח אירועים חודשי לפי מחסן וסוג אירוע

השאילתה מסכמת אירועים לפי מחסן, סוג אירוע, שנה וחודש. היא מציגה מספר אירועים, מספר הזמנות מושפעות, תאריך אירוע ראשון ותאריך אירוע אחרון. השאילתה מתאימה למסך דוחות ניהול.

#### גרסה A - אגרגציה בתת שאילתה

```sql
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
    GROUP BY d.DepotID, di.IncidentType, EXTRACT(YEAR FROM di.IncidentDate), EXTRACT(MONTH FROM di.IncidentDate)
) monthly_stats
JOIN DEPOTS dep ON dep.DepotID = monthly_stats.DepotID
ORDER BY monthly_stats.IncidentYear, monthly_stats.IncidentMonth, monthly_stats.DepotID, monthly_stats.IncidentType;
```

#### גרסה B - אגרגציה עם JOIN ישיר

```sql
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
GROUP BY d.DepotID, dep.DepotName, di.IncidentType, EXTRACT(YEAR FROM di.IncidentDate), EXTRACT(MONTH FROM di.IncidentDate)
ORDER BY "Incident Year", "Incident Month", "Depot ID", "Incident Type";
```

#### צילום הרצה ותוצאה

![Query 4 version A](img_14.png)

![Query 4 version B](img_13.png)

#### הבדל יעילות

גרסה A מבצעת קודם אגרגציה בתת שאילתה ורק לאחר מכן מחברת את שם המחסן. גרסה B מבצעת את החיבור והאגרגציה באותה שאילתה. PostgreSQL יכול לעיתים לבצע אופטימיזציה לשתי הצורות, אבל גרסה B פשוטה יותר לקריאה, וגרסה A יכולה להיות שימושית כאשר רוצים לבודד קודם את החישוב הכבד ואז להוסיף מידע תיאורי.



## שאילתות UPDATE

### UPDATE 1 - עדכון סטטוס משלוח

מטרת השאילתה היא לעדכן משלוח מסוים לפי מספר הזמנה חיצוני לסטטוס "Livrée" ולשמור את תאריך המסירה בפועל.

```sql
UPDATE DELIVERIES
SET
    Status = 'Livrée',
    ActualDeliveryDate = CURRENT_DATE
WHERE ExternalOrderID = 100001;
```

צילום מצב לפני/אחרי:

![img_37.png](img_37.png)
![img_38.png](img_38.png)
![img_39.png](img_39.png)


### UPDATE 2 - העלאת מחיר לפי קטגוריית משקל

מטרת השאילתה היא להעלות את מחיר המשלוח ב-5% לכל התעריפים בקטגוריית `Standard`.

```sql
UPDATE DELIVERY_RATES
SET RateAmount = RateAmount * 1.05
WHERE WeightClass = 'Standard';
```

צילום מצב לפני/אחרי:

![img_21.png](img_21.png)
![img_22.png](img_22.png)
![img_23.png](img_23.png)

### UPDATE 3 - שינוי מחסן לרכב

מטרת השאילתה היא להעביר רכב למחסן אחר.

```sql
UPDATE VEHICLES
SET DepotID = 2
WHERE VehicleID = 1;
```

צילום מצב לפני/אחרי:

![img_33.png](img_33.png)
![img_34.png](img_34.png)
![img_35.png](img_35.png)

## שאילתות DELETE

### DELETE 1 - מחיקת התראת אירוע שגויה

```sql
DELETE FROM DELIVERY_INCIDENTS
WHERE IncidentID = 1;
```

צילום לפני/אחרי:

![img_30.png](img_30.png)
![img_31.png](img_31.png)
![img_32.png](img_32.png)




### DELETE 2 - מחיקת הקצאת רכב

```sql
DELETE FROM VEHICLE_ASSIGNMENTS
WHERE AssignmentID = 2;
```

צילום לפני/אחרי:


![img_24.png](img_24.png)
![img_25.png](img_25.png)
![img_26.png](img_26.png)
### DELETE 3 - מחיקת הקצאת רכב נוספת

```sql
DELETE FROM VEHICLE_ASSIGNMENTS
WHERE AssignmentID = 4;
```

צילום לפני/אחרי:

![img_27.png](img_27.png)
![img_28.png](img_28.png)
![img_29.png](img_29.png)

## Rollback ו-Commit

### דוגמת ROLLBACK

בדוגמה זו מבוצע עדכון שגוי שמאפס את כל מחירי המשלוחים. לאחר בדיקת המצב מבוצעת פקודת `ROLLBACK`, ולכן בסיס הנתונים חוזר למצב המקורי.

```sql
BEGIN;

UPDATE DELIVERY_RATES
SET RateAmount = 0;

ROLLBACK;
```

המצב לפני, אחרי העדכון, ואחרי `ROLLBACK`:

![img_41.png](img_41.png)
![img_42.png](img_42.png)
![img_43.png](img_43.png)
![img_44.png](img_44.png)


### דוגמת COMMIT

בדוגמה זו מתבצע עדכון תקין של תאריך התחזוקה לרכב, ולאחר בדיקה מבוצע `COMMIT`. לאחר ה-`COMMIT`, השינוי נשמר גם לאחר בדיקה חוזרת.

```sql
BEGIN;

UPDATE VEHICLES
SET LastMaintenanceDate = CURRENT_DATE
WHERE VehicleID = 2;

COMMIT;
```

המצב לפני, אחרי העדכון, ואחרי `COMMIT`:

![img_45.png](img_45.png)
![img_46.png](img_46.png)
![img_47.png](img_47.png)



## אילוצים

האילוצים נוספו כדי להגן על תקינות הנתונים ולמנוע הכנסת ערכים לא הגיוניים.

### Contrainte 1 - plaque d'immatriculation non vide

```sql
ALTER TABLE VEHICLES
    ADD CONSTRAINT chk_vehicle_licenseplate_not_blank
        CHECK (LENGTH(TRIM(LicensePlate)) > 0);
```

Motivation: une plaque vide n'a pas de sens dans le systeme. Cette contrainte empeche d'ajouter un vehicule sans identification utilisable.

Test invalide qui doit retourner une erreur:

```sql
INSERT INTO VEHICLES
(VehicleID, DepotID, VehicleType, LicensePlate, CapacityVolume, CapacityWeight, LastMaintenanceDate)
VALUES (999999, 1, 'Camion', '   ', 10, 100, CURRENT_DATE);
```

### Contrainte 2 - capacite de depot strictement positive

```sql
ALTER TABLE DEPOTS
    ADD CONSTRAINT chk_depot_storage_capacity_strictly_positive
        CHECK (StorageCapacity > 0);
```

Motivation: le schema initial autorisait une capacite egale a 0. Dans le systeme, un depot actif doit avoir une capacite reelle, donc strictement positive.

Test invalide qui doit retourner une erreur:

```sql
INSERT INTO DEPOTS
(DepotID, DepotName, LocationAddress, StorageCapacity)
VALUES (999999, 'Depot_Test', 'Test address', 0);
```

### Contrainte 3 - description d'incident suffisamment detaillee

```sql
ALTER TABLE DELIVERY_INCIDENTS
    ADD CONSTRAINT chk_incident_description_min_length
        CHECK (LENGTH(TRIM(Description)) >= 10);
```

Motivation: une description trop courte ne donne pas assez d'information pour traiter l'incident dans l'interface de gestion.

Test invalide qui doit retourner une erreur:

```sql
INSERT INTO DELIVERY_INCIDENTS
(IncidentID, DeliveryID, ExternalLivreurID, IncidentType, IncidentDate, Description)
VALUES (999999, 1, 1, 'Retard', CURRENT_DATE, 'court');
```

צילום בדיקות אילוצים:

![img_56.png](img_56.png)
![img_57.png](img_57.png)

![img_58.png](img_58.png)
![img_59.png](img_59.png)

![img_60.png](img_60.png)
![img_61.png](img_61.png)


## אינדקסים

האינדקסים נוספו לשדות שמשמשים הרבה בסינון, חיפוש ודוחות. מטרתם היא לצמצם סריקות מלאות של טבלאות ולשפר זמני ריצה.

### אינדקס 1 - סטטוס משלוח

```sql
CREATE INDEX idx_deliveries_status ON DELIVERIES(Status);
```

מוטיבציה: שאילתות רבות מחפשות משלוחים לפי סטטוס כמו `En transit`, `Livrée`, או `Incident`. אינדקס על `Status` יכול לשפר סינון לפי סטטוס.

### אינדקס 2 - תאריך אירוע

```sql
CREATE INDEX idx_incidents_date ON DELIVERY_INCIDENTS(IncidentDate);
```

מוטיבציה: דוחות חודשיים ושנתיים משתמשים ב-`IncidentDate`. אינדקס זה עוזר במיוחד בשאילתות שמסננות לפי טווח תאריכים.

### אינדקס 3 - תאריכי הקצאות רכבים

```sql
CREATE INDEX idx_assignments_dates ON VEHICLE_ASSIGNMENTS(StartDate, EndDate);
```

מוטיבציה: בדיקות זמינות רכב מבוססות על תאריך התחלה ותאריך סיום. אינדקס משולב עוזר בחיפוש הקצאות בטווחי זמן.

### בדיקת זמני ריצה

כדי לבדוק את זמני הריצה השתמשנו ב-`EXPLAIN ANALYZE` לפני ואחרי הוספת האינדקסים. כאשר הטבלה גדולה והסינון משתמש בעמודה שעליה קיים אינדקס, זמן הריצה אמור להשתפר כי PostgreSQL יכול להשתמש ב-Index Scan במקום Sequential Scan.

צילום בדיקת אינדקסים:

![img_48.png](img_48.png)
![img_49.png](img_49.png)

## סיכום

בשלב ב נכתבו שמונה שאילתות `SELECT`, כאשר ארבע שאילתות נכתבו בשתי צורות שונות. בנוסף נכתבו שלוש שאילתות `UPDATE`, שלוש שאילתות `DELETE`, נוספו שלושה אילוצים ושלושה אינדקסים, והוצגו דוגמאות ל-`ROLLBACK` ול-`COMMIT`.

השאילתות נבנו כך שיתאימו למסכי המערכת: מסך תקלות משלוח, מסך תכנון מסלול, מסך תחזוקת רכבים, ומסך דוחות ניהול. השימוש ב-`JOIN`, תתי שאילתות, `GROUP BY`, `ORDER BY`, ופירוק תאריכים בעזרת `EXTRACT` עומד בדרישות של שלב ב.
