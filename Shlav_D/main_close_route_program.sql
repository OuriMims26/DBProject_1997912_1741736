-- ============================================================
-- Main program 1
-- Calls one function and one procedure:
--   1. fn_depot_workload
--   2. prc_close_route
-- ============================================================

DO $$
DECLARE
    v_route_id INT;
    v_depot_id INT;
    v_workload RECORD;
BEGIN
    SELECT routeid, depotid
    INTO v_route_id, v_depot_id
    FROM delivery_routes
    WHERE status IN (U&'Planifi\00E9e', 'Active')
    ORDER BY scheduledstartdate, routeid
    LIMIT 1;

    IF v_route_id IS NULL THEN
        RAISE EXCEPTION 'No planned or active route was found for this main program';
    END IF;

    SELECT *
    INTO v_workload
    FROM fn_depot_workload(v_depot_id);

    RAISE NOTICE 'Before closing route %, depot % workload is % with % open deliveries',
        v_route_id, v_depot_id, v_workload.workload_level, v_workload.open_deliveries;

    CALL prc_close_route(v_route_id);

    RAISE NOTICE 'Route % was closed successfully', v_route_id;
END;
$$;

SELECT *
FROM delivery_status_history
ORDER BY statushistoryid DESC
LIMIT 5;
