-- =========================================================================
-- INDEXES (אינדקסים)
-- =========================================================================

-- 1. אינדקס על סטטוס משלוחים (מאיץ שאילתות שמחפשות משלוחים 'In Transit')
CREATE INDEX idx_deliveries_status ON DELIVERIES(Status);

-- 2. אינדקס על תאריכי תקלות (מאיץ דוחות חודשיים כמו השאילתא ה-1 שלנו)
CREATE INDEX idx_incidents_date ON DELIVERY_INCIDENTS(IncidentDate);

-- 3. אינדקס מרוכב על תאריכי הקצאות רכבים (מאיץ בדיקות זמינות רכבים)
CREATE INDEX idx_assignments_dates ON VEHICLE_ASSIGNMENTS(StartDate, EndDate);