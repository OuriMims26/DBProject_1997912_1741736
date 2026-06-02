-- ============================================================
-- Function 2: returns a refcursor with old/problematic deliveries
-- Demonstrates refcursor, grouping, branching and exception handling.
-- ============================================================

CREATE OR REPLACE FUNCTION fn_open_problem_deliveries(p_min_days INT DEFAULT 7)
RETURNS REFCURSOR
LANGUAGE plpgsql
AS $$
DECLARE
    v_cursor REFCURSOR := 'problem_deliveries_cursor';
BEGIN
    IF p_min_days < 0 THEN
        RAISE EXCEPTION 'p_min_days must be non-negative. Given: %', p_min_days;
    END IF;

    OPEN v_cursor FOR
        SELECT
            d.deliveryid,
            d.externalorderid,
            d.depotid,
            d.status,
            COALESCE(MAX(h.changeddate), CURRENT_TIMESTAMP - (p_min_days + 1) * INTERVAL '1 day') AS last_status_change
        FROM deliveries d
             LEFT JOIN delivery_status_history h ON h.deliveryid = d.deliveryid
        WHERE d.status IN ('En attente', 'En transit', 'Incident', U&'\00C9chou\00E9e')
        GROUP BY d.deliveryid, d.externalorderid, d.depotid, d.status
        HAVING COALESCE(MAX(h.changeddate), CURRENT_TIMESTAMP - (p_min_days + 1) * INTERVAL '1 day')
               <= CURRENT_TIMESTAMP - p_min_days * INTERVAL '1 day'
        ORDER BY last_status_change, d.deliveryid;

    RETURN v_cursor;

EXCEPTION
    WHEN OTHERS THEN
        RAISE;
END;
$$;
