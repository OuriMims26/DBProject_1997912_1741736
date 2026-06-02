-- ============================================================
-- Trigger 2: validate depot capacity updates
-- Based only on the existing DEPOTS table.
-- ============================================================

CREATE OR REPLACE FUNCTION trg_depot_capacity_guard_fn()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.storagecapacity <= 0 THEN
        RAISE EXCEPTION 'Depot capacity must be positive. Given: %', NEW.storagecapacity;
    END IF;

    IF NEW.storagecapacity < OLD.storagecapacity * 0.5 THEN
        RAISE NOTICE 'Depot % capacity was reduced by more than 50 percent', NEW.depotid;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_depot_capacity_guard ON depots;

CREATE TRIGGER trg_depot_capacity_guard
BEFORE UPDATE OF storagecapacity ON depots
FOR EACH ROW
EXECUTE FUNCTION trg_depot_capacity_guard_fn();
