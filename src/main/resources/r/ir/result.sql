CREATE TEMP TABLE cohorts

AS
SELECT
cohort_id, is_outcome

FROM
(
  SELECT  305  as cohort_id, 0 as is_outcome UNION SELECT  305  as cohort_id, 0 as is_outcome
) C
;

CREATE TEMP TABLE time_at_risk

AS
WITH cteCohortCombos (target_id, outcome_id)  AS (
  select t.cohort_id as target_id, o.cohort_id as outcome_id
  FROM cohorts t
  CROSS JOIN cohorts o
  where t.is_outcome = 0 and o.is_outcome = 1 
),
cteCohortData(target_id, outcome_id, subject_id, cohort_start_date, cohort_end_date, adjusted_start_date, adjusted_end_date, op_start_date, op_end_date) as
(
  select combos.target_id, combos.outcome_id, t.subject_id, t.cohort_start_date, t.cohort_end_date, t.adjusted_start_date, t.adjusted_end_date, op.observation_period_start_date as op_start_date, op.observation_period_end_date as op_end_date
  from cteCohortCombos combos
  join (
    select cohort_definition_id, subject_id, cohort_start_date, cohort_end_date, (cohort_start_date  + 0 *INTERVAL'1 day') as adjusted_start_date, (cohort_start_date  + 0 *INTERVAL'1 day') as adjusted_end_date
    FROM webapi.cohort
  ) t on t.cohort_definition_id = combos.target_id
  join ohdsi.observation_period op on t.subject_id = op.person_id and t.cohort_start_date between op.observation_period_start_date and op.observation_period_end_date
  left join (
    select cohort_definition_id, subject_id, min(cohort_start_date) as cohort_start_date 
    from webapi.cohort
    GROUP BY cohort_definition_id, subject_id
  ) O on o.cohort_definition_id = combos.outcome_id
    and t.subject_id = o.subject_id
  where (o.cohort_start_date is null or o.cohort_start_date > t.adjusted_start_date)
    and t.adjusted_start_date < t.adjusted_end_date 
    and t.adjusted_start_date between op.observation_period_start_date and op.observation_period_end_date
    AND  t.cohort_start_date >= ' 1985-01-01 ' AND t.cohort_start_date <= ' 2015-01-01 '
),
cteEndDates (target_id, outcome_id, subject_id, cohort_start_date, followup_end, is_case) as
(
  select target_id, outcome_id, subject_id, cohort_start_date, followup_end, is_case
  FROM (
    select target_id, outcome_id, subject_id, cohort_start_date, followup_end, is_case, row_number() over (partition by target_id, outcome_id, subject_id, cohort_start_date order by followup_end asc, is_case desc) as RN 
    FROM ( 
      select combos.target_id, combos.outcome_id, t.subject_id, t.cohort_start_date, t.op_end_date as followup_end, 0 as is_case
      from cteCohortCombos combos
      join cteCohortData t on combos.target_id = t.target_id and combos.outcome_id = t.outcome_id

      UNION
      select combos.target_id, combos.outcome_id, t.subject_id, t.cohort_start_date, t.adjusted_end_date as followup_end, 0 as is_case
      from cteCohortCombos combos
      join cteCohortData t on combos.target_id = t.target_id and combos.outcome_id = t.outcome_id

      UNION
      select combos.target_id, combos.outcome_id, t.subject_id, t.cohort_start_date, o.cohort_start_date as followup_end, 1 as is_case
      from cteCohortCombos combos
      join cteCohortData t on combos.target_id = t.target_id and combos.outcome_id = t.outcome_id
      join (
        select cohort_definition_id, subject_id, min(cohort_start_date) as cohort_start_date 
        from webapi.cohort
        GROUP BY cohort_definition_id, subject_id
      ) O on o.cohort_definition_id = combos.outcome_id and t.subject_id = o.subject_id
      where o.cohort_start_date > t.adjusted_start_date

      UNION
select combos.target_id, combos.outcome_id, t.subject_id, t.cohort_start_date, ' 2015-01-01 ' as followup_end, 0 as is_case
 FROM cteCohortCombos combos
 JOIN  cteCohortData t on combos.target_id = t.target_id and combos.outcome_id = t.outcome_id

    ) RawData
  ) Result
  WHERE Result.RN = 1
),
cteRawData (target_id, outcome_id, subject_id, cohort_start_date, cohort_end_date, time_at_risk, is_case) as
(
  select t.target_id, t.outcome_id, t.subject_id, t.cohort_start_date, t.cohort_end_date, (CAST(e.followup_end AS DATE) - CAST(t.adjusted_start_date AS DATE)) as time_at_risk, e.is_case
  from cteCohortData t
  join cteEndDates e on t.target_id = e.target_id
    and t.outcome_id = e.outcome_id
    and t.subject_id = e.subject_id
    and t.cohort_start_date = e.cohort_start_date
)
 SELECT
target_id, outcome_id, subject_id, cohort_start_date, cohort_end_date, time_at_risk, is_case

FROM
cteRawData
;

-- from here, take all the people's person_id, start_date, end_date, create an 'events table' 
CREATE TEMP TABLE analysis_events

AS
SELECT
row_number() over (partition by P.person_id order by P.start_date) as event_id, P.person_id, P.start_date, P.end_date, P.op_start_date, P.op_end_date

FROM
(
  select distinct T.subject_id as person_id, T.cohort_start_date as start_date, T.cohort_end_date as end_date, OP.observation_period_start_date as op_start_date, OP.observation_period_end_date as op_end_date
  from time_at_risk T
  JOIN ohdsi.observation_period OP on T.subject_id = OP.person_id and T.cohort_start_date between OP.observation_period_start_date and OP.observation_period_end_date
) P
;

-- create the stratifiction set
CREATE TEMP TABLE Codesets  (codeset_id int NOT NULL,
  concept_id bigint NOT NULL
)
;

INSERT INTO Codesets (codeset_id, concept_id)
SELECT 0 as codeset_id, c.concept_id FROM (select distinct I.concept_id FROM
( 
  select concept_id from ohdsi.CONCEPT where concept_id in (43013024,43013025,43013028,43013026,43013027,43013029,43013032,43013030,43013031,43013033,43013034)and invalid_reason is null

) I
) C;


CREATE TEMP TABLE strataCohorts 
 (strata_sequence int,
  person_id bigint,
  event_id bigint
)
;
INSERT INTO strataCohorts (strata_sequence, person_id, event_id)
select 1 as strata_id, person_id, event_id
FROM 
(
  select pe.person_id, pe.event_id
  FROM analysis_events pe
  
JOIN (
 -- Begin Criteria Group
select 0 as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM analysis_events E
  LEFT JOIN
  (
    -- Begin Correlated Criteria
SELECT 0 as index_id, p.person_id, p.event_id
FROM analysis_events P
LEFT JOIN
(
  -- Begin Dose Era Criteria
select C.person_id, C.dose_era_id as event_id, C.dose_era_start_date as start_date, C.dose_era_end_date as end_date, C.drug_concept_id as TARGET_CONCEPT_ID, NULL as visit_occurrence_id
from 
(
  select de.*, row_number() over (PARTITION BY de.person_id ORDER BY de.dose_era_start_date, de.dose_era_id) as ordinal
  FROM ohdsi.DOSE_ERA de
where de.drug_concept_id in (SELECT concept_id from  Codesets where codeset_id = 0)
) C


-- End Dose Era Criteria

) A on A.person_id = P.person_id and A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE AND A.START_DATE >= P.OP_START_DATE and A.START_DATE <= P.OP_END_DATE AND A.END_DATE >= P.OP_END_DATE AND A.END_DATE <= P.OP_END_DATE
GROUP BY p.person_id, p.event_id
HAVING COUNT(A.TARGET_CONCEPT_ID) >= 1
-- End Correlated Criteria

  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  HAVING COUNT(index_id) >= 0
) G
-- End Criteria Group
 ) AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id
) Results
;


-- join back the followup to the stratification and write to the results page.
DELETE FROM webapi.ir_analysis_result where analysis_id = 3112;

INSERT INTO webapi.ir_analysis_result (analysis_id, target_id, outcome_id, strata_mask, person_count, time_at_risk, cases)
select 3112 as analysis_id, T.target_id, T.outcome_id, E.strata_mask,
  COUNT(subject_id) as person_count, 
  sum(1.0 * time_at_risk / 365.25) as time_at_risk, 
  sum(is_case) as cases
from time_at_risk T
JOIN (
  select E.event_id, E.person_id, E.start_date, E.end_date, SUM(coalesce(POWER(cast(2 as bigint), SC.strata_sequence), 0)) as strata_mask
  FROM analysis_events E
  LEFT JOIN strataCohorts SC on SC.person_id = E.person_id and SC.event_id = E.event_id
  group by E.event_id, E.person_id, E.start_date, E.end_date
) E on T.subject_id = E.person_id and T.cohort_start_date = E.start_date and T.cohort_end_date = E.end_date
GROUP BY T.target_id, T.outcome_id, E.strata_mask
;
-- note in the case of no stratification (no rows in strataCohorts temp table), everyone will have strata_mask of 0.

-- calculate the individual strata counts from the raw person data. Rows from #strataCohorts are used to find counts for each strata
delete from webapi.ir_analysis_strata_stats where analysis_id = 3112;
insert into webapi.ir_analysis_strata_stats (analysis_id, target_id, outcome_id, strata_sequence, person_count, time_at_risk, cases)
select irs.analysis_id, combos.target_id, combos.outcome_id, irs.strata_sequence, coalesce(T.person_count, 0) as person_count, coalesce(T.time_at_risk, 0) as time_at_risk, coalesce(T.cases, 0) as cases
from webapi.ir_strata irs
cross join (
  select t.cohort_id as target_id, o.cohort_id as outcome_id
  FROM cohorts t
  CROSS JOIN cohorts o
  where t.is_outcome = 0 and o.is_outcome = 1 
) combos
left join
(
  select T.target_id, T.outcome_id, S.strata_sequence, count(S.event_id) as person_count, sum(1.0 * T.time_at_risk / 365.25) as time_at_risk, sum(T.is_case) as cases
  from analysis_events E
  JOIN strataCohorts S on S.person_id = E.person_id and E.event_id = S.event_id
  join time_at_risk T on T.subject_id = E.person_id and T.cohort_start_date = E.start_date and T.cohort_end_date = E.end_date
  group by T.target_id, T.outcome_id, S.strata_sequence
) T on irs.strata_sequence = T.strata_sequence and T.target_id = combos.target_id and T.outcome_id = combos.outcome_id
WHERE irs.analysis_id = 3112
;

-- calculate distributions for TAR and TTO by strata

DELETE FROM webapi.ir_analysis_dist where analysis_id = 3112;

-- dist_type 1: time at risk

CREATE TEMP TABLE tempTARDist

AS
WITH cteRawData (target_id, outcome_id, strata_sequence, count_value)  AS (
  select T.target_id, T.outcome_id, -1 as strata_sequence, T.time_at_risk as count_value
  from time_at_risk T 
  
  UNION ALL

  select T.target_id, T.outcome_id, S.strata_sequence, T.time_at_risk as count_value
  from analysis_events E
  JOIN strataCohorts S on E.person_id = S.person_id and E.event_id = S.event_id
  join time_at_risk T on T.subject_id = E.person_id and T.cohort_start_date = E.start_date and T.cohort_end_date = E.end_date
)
, overallStats (target_id, outcome_id, strata_sequence, avg_value, stdev_value, min_value, max_value, total) as
(
  select target_id, 
    outcome_id, 
    strata_sequence, 
    avg(1.0 * count_value) as avg_value,
    STDDEV(count_value) as stdev_value,
    min(count_value) as min_value,
    max(count_value) as max_value,
    COUNT(*) as total
  from cteRawData
  group by target_id, outcome_id, strata_sequence
),
stats (target_id, outcome_id, strata_sequence, count_value, total, rn) as
(
  select target_id, outcome_id, strata_sequence, count_value, COUNT(*) as total, row_number() over (partition by target_id, outcome_id, strata_sequence order by count_value) as rn
  FROM cteRawData
  group by target_id, outcome_id, strata_sequence, count_value
),
priorStats (target_id, outcome_id, strata_sequence, count_value, total, accumulated) as
(
  select s.target_id, s.outcome_id, s.strata_sequence, s.count_value, s.total, sum(p.total) as accumulated
  from stats s
  join stats p on s.target_id = p.target_id and s.outcome_id = p.outcome_id and s.strata_sequence = p.strata_sequence and p.rn <= s.rn
  group by s.target_id, s.outcome_id, s.strata_sequence, s.count_value, s.total, s.rn
)
 SELECT
o.target_id,
  o.outcome_id,
  o.strata_sequence,
  o.total,
  o.avg_value,
  coalesce(o.stdev_value, 0.0) as stdev_value,
  o.min_value,
  MIN(case when p.accumulated >= .10 * o.total then count_value else o.max_value end) as p10_value,
  MIN(case when p.accumulated >= .25 * o.total then count_value else o.max_value end) as p25_value,
  MIN(case when p.accumulated >= .50 * o.total then count_value else o.max_value end) as median_value,
  MIN(case when p.accumulated >= .75 * o.total then count_value else o.max_value end) as p75_value,
  MIN(case when p.accumulated >= .90 * o.total then count_value else o.max_value end) as p90_value,
  o.max_value

FROM
priorStats p
join overallStats o on p.target_id = o.target_id and p.outcome_id = o.outcome_id and p.strata_sequence = o.strata_sequence 
GROUP BY o.target_id, o.outcome_id, o.strata_sequence, o.total, o.min_value, o.max_value, o.avg_value, o.stdev_value
;

INSERT INTO webapi.ir_analysis_dist (analysis_id, dist_type, target_id, outcome_id, strata_sequence, total, avg_value, std_dev,min_value, p10_value, p25_value, median_value, p75_value, p90_value,max_value)
select 3112 as analysis_id, 1 as dist_type, combos.target_id, combos.outcome_id, 
  case when d.strata_sequence = -1 then null else d.strata_sequence end as strata_sequence, 
  d.total, d.avg_value, d.stdev_value, d.min_value, d.p10_value, d.p25_value, d.median_value, d.p75_value, d.p90_value, d.max_value
FROM 
(
  select t.cohort_id as target_id, o.cohort_id as outcome_id
  FROM cohorts t
  CROSS JOIN cohorts o
  where t.is_outcome = 0 and o.is_outcome = 1 
) combos
JOIN tempTARDist d on combos.target_id = d.target_id and combos.outcome_id = d.outcome_id
;

TRUNCATE TABLE tempTARDist;
DROP TABLE tempTARDist;

-- dist_type 2: TTO (time to outcome)

CREATE TEMP TABLE tempTTODist

AS
WITH cteRawData (target_id, outcome_id, strata_sequence, count_value)  AS (
  select T.target_id, T.outcome_id, -1 as strata_sequence, T.time_at_risk as count_value
  from time_at_risk T 
  where T.is_case = 1

  UNION ALL

  select T.target_id, T.outcome_id, S.strata_sequence, T.time_at_risk as count_value
  from analysis_events E
  JOIN strataCohorts S on E.person_id = S.person_id and E.event_id = S.event_id
  join time_at_risk T on T.subject_id = E.person_id and T.cohort_start_date = E.start_date and T.cohort_end_date = E.end_date
  where T.is_case = 1
)
, overallStats (target_id, outcome_id, strata_sequence, avg_value, stdev_value, min_value, max_value, total) as
(
  select target_id, 
    outcome_id, 
    strata_sequence, 
    avg(1.0 * count_value) as avg_value,
    STDDEV(count_value) as stdev_value,
    min(count_value) as min_value,
    max(count_value) as max_value,
    COUNT(*) as total
  from cteRawData
  group by target_id, outcome_id, strata_sequence
),
stats (target_id, outcome_id, strata_sequence, count_value, total, rn) as
(
  select target_id, outcome_id, strata_sequence, count_value, COUNT(*) as total, row_number() over (partition by target_id, outcome_id, strata_sequence order by count_value) as rn
  FROM cteRawData
  group by target_id, outcome_id, strata_sequence, count_value
),
priorStats (target_id, outcome_id, strata_sequence, count_value, total, accumulated) as
(
  select s.target_id, s.outcome_id, s.strata_sequence, s.count_value, s.total, sum(p.total) as accumulated
  from stats s
  join stats p on s.target_id = p.target_id and s.outcome_id = p.outcome_id and s.strata_sequence = p.strata_sequence and p.rn <= s.rn
  group by s.target_id, s.outcome_id, s.strata_sequence, s.count_value, s.total, s.rn
)
 SELECT
o.target_id,
  o.outcome_id,
  o.strata_sequence,
  o.total,
  o.avg_value,
  coalesce(o.stdev_value, 0.0) as stdev_value,
  o.min_value,
  MIN(case when p.accumulated >= .10 * o.total then count_value else o.max_value end) as p10_value,
  MIN(case when p.accumulated >= .25 * o.total then count_value else o.max_value end) as p25_value,
  MIN(case when p.accumulated >= .50 * o.total then count_value else o.max_value end) as median_value,
  MIN(case when p.accumulated >= .75 * o.total then count_value else o.max_value end) as p75_value,
  MIN(case when p.accumulated >= .90 * o.total then count_value else o.max_value end) as p90_value,
  o.max_value

FROM
priorStats p
join overallStats o on p.target_id = o.target_id and p.outcome_id = o.outcome_id and p.strata_sequence = o.strata_sequence 
GROUP BY o.target_id, o.outcome_id, o.strata_sequence, o.total, o.min_value, o.max_value, o.avg_value, o.stdev_value
;

INSERT INTO webapi.ir_analysis_dist (analysis_id, dist_type, target_id, outcome_id, strata_sequence, total, avg_value, std_dev,min_value, p10_value, p25_value, median_value, p75_value, p90_value,max_value)
select 3112 as analysis_id, 2 as dist_type, combos.target_id, combos.outcome_id, 
  case when d.strata_sequence = -1 then null else d.strata_sequence end as strata_sequence, 
  d.total, d.avg_value, d.stdev_value, d.min_value, d.p10_value, d.p25_value, d.median_value, d.p75_value, d.p90_value, d.max_value
FROM 
(
  select t.cohort_id as target_id, o.cohort_id as outcome_id
  FROM cohorts t
  CROSS JOIN cohorts o
  where t.is_outcome = 0 and o.is_outcome = 1 
) combos
JOIN tempTTODist d on combos.target_id = d.target_id and combos.outcome_id = d.outcome_id
;

TRUNCATE TABLE tempTTODist;
DROP TABLE tempTTODist;

TRUNCATE TABLE Codesets;
DROP TABLE Codesets;

TRUNCATE TABLE strataCohorts;
DROP TABLE strataCohorts;

TRUNCATE TABLE cohorts;
DROP TABLE cohorts;

TRUNCATE TABLE time_at_risk;
DROP TABLE time_at_risk;


