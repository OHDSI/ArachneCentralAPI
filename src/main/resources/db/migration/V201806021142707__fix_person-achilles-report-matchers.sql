UPDATE achilles_report_matchers SET achilles_report_id = (SELECT id FROM achilles_reports WHERE name = 'Person') where pattern = 'person.json';

UPDATE achilles_report_matchers SET pattern = 'procedures/procedure_*.json' where pattern = ' procedures/procedure_*.json';