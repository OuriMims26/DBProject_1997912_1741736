-- =========================================================================
-- השאילתות של TRANSACTIONS (BEGIN, COMMIT, ROLLBACK)
-- =========================================================================

-- =========================================================================
-- דוגמה 1 : טעות נוראית וביטולה (Le ROLLBACK)
-- =========================================================================
-- Contexte : Un employé fait une erreur catastrophique et rend toutes les livraisons gratuites !
-- Heureusement, il avait ouvert une transaction et utilise ROLLBACK pour annuler.

BEGIN; -- 1. פתיחת טרנזקציה (Ouverture du brouillon)

-- 2. הפעולה השגויה (L'erreur : On met tous les prix à 0)
UPDATE DELIVERY_RATES
SET RateAmount = 0;

-- (C'est ici qu'on fait un SELECT pour voir la catastrophe avant d'annuler)

-- 3. ביטול הפעולה (L'annulation magique)
ROLLBACK;

-- (Si on refait un SELECT ici, les prix normaux sont revenus)


-- =========================================================================
-- דוגמה 2 : פעולה תקינה ושמירתה (Le COMMIT)
-- =========================================================================
-- Contexte : Le manager change officiellement le statut d'un véhicule.
-- Après vérification, il sauvegarde définitivement l'action.

-- =========================================================================
-- דוגמה 2 : פעולה תקינה ושמירתה (Le COMMIT)
-- =========================================================================
-- Contexte : Le manager vient de faire réviser un véhicule aujourd'hui.
-- Il met à jour la date de maintenance et sauvegarde définitivement.

BEGIN; -- 1. פתיחת טרנזקציה (Ouverture du brouillon)

-- 2. הפעולה התקינה (La modification de la date)
UPDATE VEHICLES
SET lastmaintenancedate = CURRENT_DATE
WHERE VehicleID = 2;

-- 3. שמירה סופית (La validation définitive)
COMMIT;