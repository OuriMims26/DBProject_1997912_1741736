-- ============================================================
-- Procedure 1: closes a route and marks all its deliveries as delivered
-- Demonstrates explicit cursor, records, loops, DML, branching and exceptions.
-- ============================================================

CREATE OR REPLACE PROCEDURE prc_close_route(p_route_id INT)
LANGUAGE plpgsql
AS $$
DECLARE
    v_route RECORD;
    v_stop RECORD;
    v_updated_count INT := 0;
    v_cursor_open BOOLEAN := false;

    cur_stops CURSOR FOR
        SELECT rs.stopid, rs.stopsequence, d.deliveryid, d.status
        FROM route_stops rs
             JOIN deliveries d ON d.deliveryid = rs.deliveryid
        WHERE rs.routeid = p_route_id
        ORDER BY rs.stopsequence;
BEGIN
    SELECT *
    INTO v_route
    FROM delivery_routes
    WHERE routeid = p_route_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Route % does not exist', p_route_id;
    END IF;

    IF v_route.status NOT IN (U&'Planifi\00E9e', 'Active') THEN
        RAISE EXCEPTION 'Route % cannot be closed because its status is %', p_route_id, v_route.status;
    END IF;

    OPEN cur_stops;
    v_cursor_open := true;

    LOOP
        FETCH cur_stops INTO v_stop;
        EXIT WHEN NOT FOUND;

        IF v_stop.status <> U&'Livr\00E9e' THEN
            UPDATE deliveries
            SET status = U&'Livr\00E9e',
                actualdeliverydate = CURRENT_DATE
            WHERE deliveryid = v_stop.deliveryid;

            v_updated_count := v_updated_count + 1;
        END IF;
    END LOOP;

    CLOSE cur_stops;
    v_cursor_open := false;

    UPDATE delivery_routes
    SET status = U&'Termin\00E9e'
    WHERE routeid = p_route_id;

    RAISE NOTICE 'Route % closed. Deliveries updated=%', p_route_id, v_updated_count;

EXCEPTION
    WHEN OTHERS THEN
        IF v_cursor_open THEN
            BEGIN
                CLOSE cur_stops;
            EXCEPTION
                WHEN invalid_cursor_name THEN
                    NULL;
            END;
        END IF;
        RAISE;
END;
$$;
