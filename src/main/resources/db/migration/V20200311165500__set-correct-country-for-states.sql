update states_provinces set
country_id = (SELECT max(id) FROM countries WHERE name = 'United States of America (the)')
where name in ('Alabama',
               'Alaska',
               'Arizona',
               'Arkansas',
               'California',
               'Colorado',
               'Connecticut',
               'Delaware',
               'Florida');