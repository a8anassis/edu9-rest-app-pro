--This query shows a list of all teachers with their personal details, organized by region.
--For each teacher it displays:

--ΠΕΡΙΟΧΗ — the region the teacher belongs to
--ΌΝΟΜΑ — the teacher's first name
--ΕΠΩΝΥΜΟ — the teacher's last name
--ΑΜΚΑ — the teacher's social security number
--ΑΦΜ — the teacher's tax number
--ΚΑΤΑΣΤΑΣΗ — a label based on when the teacher was added:

--ΝΕΟΣ — added after 1/1/2025
--ΜΕΣΑΙΟΣ — added after 1/1/2023 (but before 2025)
--ΕΜΠΕΙΡΟΣ — added after 1/1/2020 (but before 2023)
--ΠΑΛΙΟΣ — added before 2020

--Example result:
--ΠΕΡΙΟΧΗ       ΌΝΟΜΑ   ΕΠΩΝΥΜΟ         ΑΜΚΑ        ΑΦΜ         ΚΑΤΑΣΤΑΣΗ
--Αθήνα         Γιώργος Παπαδόπουλος    12345678901 123456789   ΝΕΟΣ
--Αθήνα         Μαρία   Κωνσταντίνου    98765432101 987654321   ΠΑΛΙΟΣ
--Θεσσαλονίκη   Νίκος   Αλεξίου         11223344556 112233445   ΜΕΣΑΙΟΣ

SELECT
	r.name AS 'ΠΕΡΙΟΧΗ',
    t.firstname AS 'ΌΝΟΜΑ',
    t.lastname AS 'ΕΠΩΝΥΜΟ',
    pi.amka AS 'ΑΜΚΑ',
    t.vat AS 'ΑΦΜ',
    CASE 
        WHEN t.created_at > '2025-01-01' THEN 'ΝΕΟΣ'
		WHEN t.created_at > '2023-01-01' THEN 'ΜΕΣΑΙΟΣ'
		WHEN t.created_at > '2020-01-01' THEN 'ΕΜΠΕΙΡΟΣ'
		ELSE 'ΠΑΛΙΟΣ'
    END AS 'ΚΑΤΑΣΤΑΣΗ'
FROM teachers t
JOIN personal_information pi ON t.personal_info_id = pi.id
JOIN regions r ON t.region_id = r.id
ORDER BY r.name;