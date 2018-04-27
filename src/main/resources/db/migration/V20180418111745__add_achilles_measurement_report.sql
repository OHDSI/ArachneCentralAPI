INSERT INTO achilles_reports (label, name, sort_order) VALUES ('measurement', 'Measurement', 140) ON CONFLICT (name) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Measurement'), 'measurement_treemap.json') ON CONFLICT (pattern) DO NOTHING;
INSERT INTO achilles_report_matchers (achilles_report_id, pattern) VALUES ((SELECT id FROM achilles_reports WHERE name = 'Measurement'), 'measurements/measurement_*.json') ON CONFLICT (pattern) DO NOTHING;

