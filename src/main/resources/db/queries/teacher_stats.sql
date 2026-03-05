-- A Common Table Expression (CTE) is a named,
-- temporary result set in SQL, defined using the WITH clause.
-- CTEs enhance code readability and maintainability by breaking
-- complex queries into smaller, modular, and reusable pieces.

--  Statistics summary per region about its teachers.

--For each region it displays:

--ΠΕΡΙΟΧΗ — the region's name
--ΣΥΝΟΛΟ ΕΚΠΑΙΔΕΥΤΩΝ — total number of teachers in that region
--ΝΕΟΙ — how many teachers were added after 1/1/2025
--ΠΑΛΙΟΙ — how many teachers were added before or on 1/1/2025
--ΧΑΡΑΚΤΗΡΙΣΜΟΣ — a label that says:

--ΚΥΡΙΩΣ ΝΕΟΙ if the region has more new teachers than old
--ΚΥΡΙΩΣ ΠΑΛΙΟΙ if the region has more old teachers than new
--ΙΣΟΡΡΟΠΙΑ if they are equal

--Example result:
--ΠΕΡΙΟΧΗ     ΣΥΝΟΛΟ ΕΚΠΑΙΔΕΥΤΩΝ  ΝΕΟΙ    ΠΑΛΙΟΙ      ΧΑΡΑΚΤΗΡΙΣΜΟΣ
--Αθήνα       45                  30      15          ΚΥΡΙΩΣ ΝΕΟΙ
--Θεσσαλονίκη 30                  10      20          ΚΥΡΙΩΣ ΠΑΛΙΟΙ
--Πάτρα       20                  10      10          ΙΣΟΡΡΟΠΙΑ

WITH teacher_stats AS (
    SELECT
        t.region_id,
        COUNT(*) AS total_teachers,
        SUM(CASE WHEN t.created_at > '2025-01-01' THEN 1 ELSE 0 END) AS new_teachers,
        SUM(CASE WHEN t.created_at <= '2025-01-01' THEN 1 ELSE 0 END) AS old_teachers
    FROM teachers t
    JOIN personal_information pi ON t.personal_info_id = pi.id
    GROUP BY t.region_id
)

SELECT
    r.name AS 'ΠΕΡΙΟΧΗ',
    ts.total_teachers AS 'ΣΥΝΟΛΟ ΕΚΠΑΙΔΕΥΤΩΝ',
    ts.new_teachers AS 'ΝΕΟΙ',
    ts.old_teachers AS 'ΠΑΛΙΟΙ',
    CASE
        WHEN ts.new_teachers > ts.old_teachers THEN 'ΚΥΡΙΩΣ ΝΕΟΙ'
        WHEN ts.new_teachers < ts.old_teachers THEN 'ΚΥΡΙΩΣ ΠΑΛΙΟΙ'
        ELSE 'ΙΣΟΡΡΟΠΙΑ'
    END AS 'ΧΑΡΑΚΤΗΡΙΣΜΟΣ'
FROM regions r
JOIN teacher_stats ts ON r.id = ts.region_id
ORDER BY ts.total_teachers DESC;