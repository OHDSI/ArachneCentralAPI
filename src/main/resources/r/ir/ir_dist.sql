select 
  target_id, 
  outcome_id, 
  strata_sequence, 
  dist_type, 
  total, 
  avg_value, 
  std_dev, 
  min_value, 
  p10_value, 
  p25_value, 
  median_value, 
  p75_value, 
  p90_value, 
  max_value 
from 
  @resultsSchema.ir_analysis_dist 
where 
  analysis_id = @analysisId