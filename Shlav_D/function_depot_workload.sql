-- ============================================================
-- Function 1: depot workload summary
-- Demonstrates implicit cursor, records, conditions and exception handling.
-- ============================================================

CREATE OR REPLACE FUNCTION fn_depot_workload(p_depot_id INT)
RETURNS TABLE
(
    depot_id INT,
    depot_name VARCHAR(100),
    total_deliveries INT,
    open_deliveries INT,
    incident_deliveries INT,
    delivered_deliveries INT,
    workload_level TEXT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_depot RECORD;
    v_delivery RECORD;
    v_total INT := 0;
    v_open INT := 0;
    v_incident INT := 0;
    v_delivered INT := 0;
    v_level TEXT;
BEGIN
    SELECT *
    INTO v_depot
    FROM depots
    WHERE depotid = p_depot_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Depot % does not exist', p_depot_id;
    END IF;

    FOR v_delivery IN
        SELECT deliveryid, status
        FROM deliveries
        WHERE depotid = p_depot_id
    LOOP
        v_total := v_total + 1;

        IF v_delivery.status IN ('En attente', 'En transit') THEN
            v_open := v_open + 1;
        ELSIF v_delivery.status = 'Incident' THEN
            v_incident := v_incident + 1;
        ELSIF v_delivery.status = U&'Livr\00E9e' THEN
            v_delivered := v_delivered + 1;
        END IF;
    END LOOP;

    IF v_incident >= 5 OR v_open >= 20 THEN
        v_level := 'Critical';
    ELSIF v_incident > 0 OR v_open >= 10 THEN
        v_level := 'Busy';
    ELSE
        v_level := 'Normal';
    END IF;

    depot_id := v_depot.depotid;
    depot_name := v_depot.depotname;
    total_deliveries := v_total;
    open_deliveries := v_open;
    incident_deliveries := v_incident;
    delivered_deliveries := v_delivered;
    workload_level := v_level;
    RETURN NEXT;

EXCEPTION
    WHEN OTHERS THEN
        RAISE;
END;
$$;
