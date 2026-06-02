-- ============================================================
-- Main program 2
-- Calls one function and one procedure:
--   1. fn_open_problem_deliveries
--   2. prc_mark_low_stock_deliveries_incident
-- ============================================================

BEGIN;

DO $$
DECLARE
    v_cursor REFCURSOR;
    v_problem RECORD;
    v_preview_count INT := 0;
BEGIN
    v_cursor := fn_open_problem_deliveries(7);
            

    LOOP
        FETCH v_cursor INTO v_problem;
        EXIT WHEN NOT FOUND OR v_preview_count >= 5;

        RAISE NOTICE 'Problem delivery preview: id=%, status=%',
            v_problem.deliveryid, v_problem.status;

        v_preview_count := v_preview_count + 1;
    END LOOP;

    CLOSE v_cursor;

    CALL prc_mark_low_stock_deliveries_incident(5);
END;
$$;

COMMIT;

SELECT deliveryid, status
FROM deliveries
WHERE status = 'Incident'
ORDER BY deliveryid
LIMIT 10;

SELECT *
FROM delivery_incidents
ORDER BY incidentid DESC
LIMIT 5;
