-- ============================================================
-- Trigger 1: keep delivery_status_history synchronized with status updates
-- Required UPDATE trigger.
-- Based only on existing tables.
-- ============================================================

CREATE OR REPLACE FUNCTION trg_delivery_status_history_fn()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.status IS DISTINCT FROM OLD.status THEN
        INSERT INTO delivery_status_history(statushistoryid, deliveryid, status, changeddate)
        VALUES (
            COALESCE((SELECT MAX(statushistoryid) FROM delivery_status_history), 0) + 1,
            NEW.deliveryid,
            NEW.status,
            CURRENT_TIMESTAMP
        );
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_delivery_status_history ON deliveries;

CREATE TRIGGER trg_delivery_status_history
AFTER UPDATE OF status ON deliveries
FOR EACH ROW
EXECUTE FUNCTION trg_delivery_status_history_fn();
