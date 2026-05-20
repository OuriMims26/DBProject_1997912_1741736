# Project Report: Stage C - Integration and Views

**Original project name:** LogisticDB - logistics and delivery management  
**Integrated project name:** Shoppy - inventory, customers and orders management

## 1. Integration Decisions

In this stage, we had to integrate our logistics system, `LogisticDB`, managed locally with PostgreSQL/PgAdmin, with the other group's system, `Shoppy`, hosted in the cloud on Supabase.

We chose integration option 2: **foreign tables**. This approach keeps the two databases physically separate while allowing our local database to access the external system's tables.

Our side of the integration starts from our `LogisticDB` database and imports the `Shoppy` project tables as foreign tables into a separate local schema. On the other side, the other team performed the reverse integration: from their Supabase database, they accessed our local logistics database. Since our database was local, they used `ngrok` to temporarily expose TCP access and allow the remote connection.

### 1.1 Choice of Option 2 - Foreign Tables

We decided not to perform a full migration and not to copy all Shoppy data into our database. Both systems therefore keep their autonomy:

- `LogisticDB` remains responsible for depots, vehicles, routes, deliveries, stops, rates and incidents.
- `Shoppy` remains responsible for customers, orders, products, categories and business data.

This decision avoids data duplication and allows real-time consultation of remote data.

### 1.2 Use of postgres_fdw

To connect our local database to the Shoppy team's Supabase database, we used the PostgreSQL extension `postgres_fdw`. This extension allows a PostgreSQL database to read tables located in another PostgreSQL database.

Unlike the other team's side, our connection does not require `ngrok`, because we connect from our local database to Supabase, which already has an accessible network address.

### 1.3 Separation Through a Dedicated Schema

To avoid name conflicts between local tables and imported tables, we created a separate schema named `remote_shoppy`.

Our project tables remain in the usual local schema, for example:

- `DEPOTS`
- `DELIVERIES`
- `DELIVERY_ROUTES`
- `DELIVERY_INCIDENTS`

The Shoppy project tables are accessed through the remote schema:

- `remote_shoppy.ORDERS`
- `remote_shoppy.CUSTOMERS`
- `remote_shoppy.PRODUCTS`
- `remote_shoppy.CATEGORIES`

This separation makes the integration clearer and avoids modifying the original tables.

### 1.4 Logical Link Between the Two Systems

The main link between the two projects is:

- `DELIVERIES.ExternalOrderID` in our logistics database;
- `ORDERS.OrderID` in the Shoppy database.

This link allows us to associate a local delivery with an order from the Shoppy system.

## 2. Process and Commands in Integrate.sql

The `Integrate.sql` file contains the commands used to connect our local database to the Shoppy project hosted on Supabase.

### 2.1 Cleaning a Previous Integration

```sql
DROP SERVER IF EXISTS supabase_server CASCADE;
DROP SCHEMA IF EXISTS remote_shoppy CASCADE;
```

These commands remove the previous remote server and the previous `remote_shoppy` schema if they already exist. This allows the script to be rerun cleanly without conflicts.

### 2.2 Enabling the postgres_fdw Extension

```sql
CREATE EXTENSION IF NOT EXISTS postgres_fdw;
```

This command enables the PostgreSQL component required to work with foreign tables.

### 2.3 Creating the Remote Supabase Server

```sql
CREATE SERVER supabase_server
FOREIGN DATA WRAPPER postgres_fdw
OPTIONS (
        host 'aws-1-eu-west-1.pooler.supabase.com',
        port '5432',
        dbname 'postgres'
);
```

This command defines the remote server. It contains the Supabase server address, the port and the remote database name.

### 2.4 Creating the User Mapping

```sql
CREATE USER MAPPING FOR postgres
SERVER supabase_server
OPTIONS (
        user 'postgres.jcqlmkuusqdxplazmfgz',
        password 'DBProject_1935279_214631426'
);
```

This command defines the credentials used by our local `postgres` user to connect to the remote Supabase server.

### 2.5 Creating the Remote Schema

```sql
CREATE SCHEMA remote_shoppy;
```

This schema is used as a logical folder for the foreign tables imported from Shoppy.

### 2.6 Importing the Remote Tables

```sql
IMPORT FOREIGN SCHEMA public
FROM SERVER supabase_server
INTO remote_shoppy;
```

This command reads the structure of the tables from Supabase's `public` schema and creates foreign tables inside `remote_shoppy`. The data remains physically stored in Supabase, but we can query it from our local database.

### 2.7 Connection Test

```sql
SELECT * FROM remote_shoppy.categories LIMIT 5;
```

This query verifies that the import worked and that the remote tables are accessible.

## 3. Views and Queries

The `Views.sql` file contains three views:

1. a local view from the `LogisticDB` perspective;
2. a remote view from the `Shoppy` perspective;
3. an integrated view combining both systems.

Each view is followed by two meaningful queries.

## 4. View 1 - LogisticDB Perspective

**View name:** `v_depot_routes`

### Description

This view represents the perspective of our logistics system. It combines depots with delivery routes in order to show which routes are managed by each depot and what the status of each route is.

### View Creation Code

```sql
CREATE OR REPLACE VIEW v_depot_routes AS
SELECT
    dp.DepotID,
    dp.DepotName,
    r.RouteID,
    r.RouteName,
    r.Status AS RouteStatus
FROM DEPOTS dp
         JOIN DELIVERY_ROUTES r ON dp.DepotID = r.DepotID;
```

### View Verification

```sql
SELECT * FROM v_depot_routes LIMIT 10;
```

![v_depot_routes result](img.png)

### Query 1.1 - Active Routes

**Description:** this query displays only the currently active routes. It can be used by the logistics manager to monitor routes that are currently running.

```sql
SELECT * FROM v_depot_routes
WHERE RouteStatus = 'Active';
```

![active routes result](img_1.png)

### Query 1.2 - Number of Routes per Depot

**Description:** this query counts the total number of routes managed by each depot.

```sql
SELECT DepotName, COUNT(RouteID) AS TotalRoutes
FROM v_depot_routes
GROUP BY DepotName;
```

![routes per depot result](img_2.png)

## 5. View 2 - Shoppy Perspective

**View name:** `v_warehouse_inventory`

### Description

This view represents the perspective of the Shoppy system. It displays product inventory by warehouse. It allows us to track available products and identify low stock.

### View Creation Code

```sql
CREATE OR REPLACE VIEW v_warehouse_inventory AS
SELECT
    w.WarehouseID,
    w.Location,
    p.ProductID,
    p.ProductName,
    p.StockQuantity
FROM remote_shoppy.WAREHOUSES w
         JOIN remote_shoppy.PRODUCTS p ON w.WarehouseID = p.WarehouseID;
```

### View Verification

```sql
SELECT * FROM v_warehouse_inventory LIMIT 10;
```

![v_warehouse_inventory result](img_3.png)

### Query 2.1 - Low Stock Products

**Description:** this query displays products whose available quantity is below 10. It can help trigger a restocking process.

```sql
SELECT * FROM v_warehouse_inventory
WHERE StockQuantity < 10;
```

![low stock result](img_4.png)

### Query 2.2 - Total Stock per Warehouse

**Description:** this query calculates the total number of items stored in each warehouse.

```sql
SELECT Location, SUM(StockQuantity) AS TotalItemsStored
FROM v_warehouse_inventory
GROUP BY Location;
```

![stock per warehouse result](img_5.png)

## 6. View 3 - Integrated View: LogisticDB + Shoppy

**View name:** `v_delivery_customer_info`

### Description

This view is the main integrated view. It links our local deliveries to orders and customers from the Shoppy system. The connection is made between `DELIVERIES.ExternalOrderID` and `remote_shoppy.ORDERS.OrderID`.

This view allows us to obtain, for a delivery, the logistics status, the shipping address and the customer's contact details.

### View Creation Code

```sql
CREATE OR REPLACE VIEW v_delivery_customer_info AS
SELECT
    d.DeliveryID,
    d.Status AS DeliveryStatus,
    o.ShippingAddress,
    c.FirstName,
    c.LastName,
    c.Phone AS CustomerPhone
FROM DELIVERIES d
         JOIN remote_shoppy.ORDERS o ON d.ExternalOrderID = o.OrderID
         JOIN remote_shoppy.CUSTOMERS c ON o.CustomerID = c.CustomerID;
```

### View Verification

```sql
SELECT * FROM v_delivery_customer_info LIMIT 10;
```

![v_delivery_customer_info result](img_6.png)

### Query 3.1 - Contacts for Deliveries in Transit

**Description:** this query displays the contact details of customers whose delivery is currently in transit. It is useful for a driver or a tracking service.

```sql
SELECT FirstName, CustomerPhone, ShippingAddress
FROM v_delivery_customer_info
WHERE DeliveryStatus = 'En transit';
```

![deliveries in transit result](img_7.png)

### Query 3.2 - Search by Customer Last Name

**Description:** this query finds deliveries associated with a specific customer. In this example, we search for the customer whose last name is `Vincent`.

```sql
SELECT DeliveryID, DeliveryStatus
FROM v_delivery_customer_info
WHERE LastName = 'Vincent';
```

![customer search result](img_8.png)

## 7. Verification of Imported Tables

Before taking the final screenshots, it is important to verify the exact names of the tables and columns imported from Supabase.

### List of Imported Foreign Tables

```sql
SELECT foreign_table_schema, foreign_table_name
FROM information_schema.foreign_tables
WHERE foreign_table_schema = 'remote_shoppy'
ORDER BY foreign_table_name;
```

### List of Imported Columns

```sql
SELECT table_name, column_name, data_type
FROM information_schema.columns
WHERE table_schema = 'remote_shoppy'
ORDER BY table_name, ordinal_position;
```

These queries confirm that the names used in `Views.sql` match the Shoppy schema exactly.

## 8. Conclusion

In this stage, we integrated our `LogisticDB` database with the remote `Shoppy` database using option 2, meaning foreign tables.

The integration was implemented with `postgres_fdw`, a remote server named `supabase_server`, a dedicated schema named `remote_shoppy`, and the import of Supabase's public schema. This solution keeps both systems independent while allowing us to write shared views.

The views created show three perspectives: the local logistics perspective, the remote commercial perspective, and an integrated perspective linking deliveries to orders and customers.

