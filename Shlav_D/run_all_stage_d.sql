-- ============================================================
-- Stage D full loader
-- No table changes are required.
-- Run this file first, then run each main program separately.
-- ============================================================

\i function_depot_workload.sql
\i function_open_problem_deliveries.sql
\i procedure_close_route.sql
\i procedure_mark_low_stock_deliveries_incident.sql
\i trigger_delivery_status_history.sql
\i trigger_depot_capacity_guard.sql
