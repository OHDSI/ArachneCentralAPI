select 
  target_id, outcome_id, sum(person_count) as person_count, 
  sum(time_at_risk) as time_at_risk,
  sum(cases) as cases 
from 
  @resultsSchema.ir_analysis_result 
where 
  analysis_id = @id 
GROUP BY 
  target_id, outcome_id