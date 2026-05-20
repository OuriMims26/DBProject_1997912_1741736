-- ==========================================
-- VIEW 1: Local Perspective (Logistics Only)
-- ==========================================
-- This view joins Depots and Routes to see active delivery routes per depot.
CREATE OR REPLACE VIEW v_depot_routes AS
SELECT
    dp.DepotID,
    dp.DepotName,
    r.RouteID,
    r.RouteName,
    r.Status AS RouteStatus
FROM DEPOTS dp
         JOIN DELIVERY_ROUTES r ON dp.DepotID = r.DepotID;

-- Query 1.1: See all active routes
SELECT * FROM v_depot_routes WHERE RouteStatus = 'Active';

-- Query 1.2: Count how many routes each depot manages
SELECT DepotName, COUNT(RouteID) AS TotalRoutes
FROM v_depot_routes
GROUP BY DepotName;


-- ==========================================
-- VIEW 2: Foreign Perspective (Shoppy Only)
-- ==========================================
-- This view looks at your Shoppy system to see warehouse inventory levels.
CREATE OR REPLACE VIEW v_warehouse_inventory AS
SELECT
    w.WarehouseID,
    w.Location,
    p.ProductID,
    p.ProductName,
    p.StockQuantity
FROM remote_shoppy.WAREHOUSES w
         JOIN remote_shoppy.PRODUCTS p ON w.WarehouseID = p.WarehouseID;

-- Query 2.1: Find items that are dangerously low in stock (under 10 items)
SELECT * FROM v_warehouse_inventory WHERE StockQuantity < 10;

-- Query 2.2: Calculate the total number of items stored in each warehouse
SELECT Location, SUM(StockQuantity) AS TotalItemsStored
FROM v_warehouse_inventory
GROUP BY Location;


-- ==========================================
-- VIEW 3: Combined Perspective (Logistics + Shoppy)
-- ==========================================
-- This view gets the delivery info and connects it to Shoppy to get the Customer's Name and Phone for the driver.
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

-- Query 3.1: Get the contact phone numbers for deliveries currently in transit
SELECT FirstName, CustomerPhone, ShippingAddress FROM v_delivery_customer_info WHERE DeliveryStatus = 'En transit';

-- Query 3.2: Find deliveries associated with a specific customer last name
SELECT DeliveryID, DeliveryStatus FROM v_delivery_customer_info WHERE LastName = 'Vincent';
