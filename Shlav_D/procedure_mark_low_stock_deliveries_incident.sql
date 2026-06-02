-- ============================================================
-- Procedure 2: marks deliveries as incidents when their Shoppy order contains low-stock products
-- Demonstrates remote schema reads, implicit cursor, DML, branching and exceptions.
-- ============================================================

CREATE OR REPLACE PROCEDURE prc_mark_low_stock_deliveries_incident(p_stock_threshold INT DEFAULT 5)
LANGUAGE plpgsql
AS $$
DECLARE
    v_row RECORD;
    v_count INT := 0;
BEGIN
    IF p_stock_threshold < 0 THEN
        RAISE EXCEPTION 'p_stock_threshold must be non-negative. Given: %', p_stock_threshold;
    END IF;

    FOR v_row IN
        SELECT DISTINCT
            d.deliveryid,
            d.externalorderid,
            d.externalprimarylivreurid,
            MIN(p.stockquantity) AS min_stock
        FROM deliveries d
             JOIN remote_shoppy.order_items oi ON oi.orderid = d.externalorderid
             JOIN remote_shoppy.products p ON p.productid = oi.productid
        WHERE d.status IN ('En attente', 'En transit')
          AND p.stockquantity <= p_stock_threshold
        GROUP BY d.deliveryid, d.externalorderid, d.externalprimarylivreurid
        ORDER BY d.deliveryid
    LOOP
        UPDATE deliveries
        SET status = 'Incident'
        WHERE deliveryid = v_row.deliveryid;

        INSERT INTO delivery_incidents(
            incidentid,
            deliveryid,
            externallivreurid,
            incidenttype,
            incidentdate,
            description
        )
        VALUES (
            COALESCE((SELECT MAX(incidentid) FROM delivery_incidents), 0) + 1,
            v_row.deliveryid,
            v_row.externalprimarylivreurid,
            'Low Shoppy stock',
            CURRENT_DATE,
            'External order ' || v_row.externalorderid ||
            ' contains product with stock ' || v_row.min_stock ||
            ', threshold ' || p_stock_threshold
        );

        v_count := v_count + 1;
    END LOOP;

    RAISE NOTICE 'Deliveries marked as Incident=%, threshold=%', v_count, p_stock_threshold;

EXCEPTION
    WHEN OTHERS THEN
        RAISE;
END;
$$;
