-- The order of the queries is critically important, so please do not change it.

-- Step 1.
-- find states that related to the wrong country(Austria instead of USA)
-- 1.1 set USA country for these states in users_data table

WITH
  usa_contry_id AS (SELECT id FROM countries WHERE name = 'United States of America (the)' limit 1),
  austria_contry_id AS (SELECT id FROM countries WHERE name = 'Austria' limit 1)

update users_data
set country_id = usa_contry_id.id
from usa_contry_id, (select sp_austria.id
      from states_provinces sp_austria,
           austria_contry_id
      where name in ('Alabama', 'Alaska', 'Arizona',
                     'Arkansas', 'California', 'Colorado',
                     'Connecticut', 'Delaware', 'Florida')
        and country_id = austria_contry_id.id
        and not exists(
              select *
              from states_provinces sp_usa, usa_contry_id
              where country_id = usa_contry_id.id
                and trim(upper(sp_austria.name)) = trim(upper(sp_usa.name)))
     ) austria_ids
where state_province_id = austria_ids.id;

--1.2 move states to the USA
WITH
    usa_contry_id AS (SELECT id FROM countries WHERE name = 'United States of America (the)' limit 1),
    austria_contry_id AS (SELECT id FROM countries WHERE name = 'Austria' limit 1)
update states_provinces sp_austria set
    country_id = usa_contry_id.id
from usa_contry_id, austria_contry_id
where name in ('Alabama', 'Alaska', 'Arizona',
               'Arkansas', 'California', 'Colorado',
               'Connecticut', 'Delaware', 'Florida')
  and country_id = austria_contry_id.id
  and not exists(
        select *
        from states_provinces sp_usa, usa_contry_id
        where country_id = usa_contry_id.id
          and trim(upper(sp_austria.name)) = trim(upper(sp_usa.name))
    );

--Step 2. find duplicate states(the states that should be under USA but also mistakenly exists in Austria)
--2.1 fix state_id and country_id in the users_data table
WITH
    usa_contry_id AS (SELECT id FROM countries WHERE name = 'United States of America (the)' limit 1),
    austria_contry_id AS (SELECT id FROM countries WHERE name = 'Austria' limit 1)
update users_data
set state_province_id = pair_of_austria_and_usa_ids.usa_id,
    country_id = usa_contry_id.id
from usa_contry_id, (select sp_austria.id austria_id,
             sp_usa.id     usa_id
      from usa_contry_id, austria_contry_id, states_provinces sp_austria
               join states_provinces sp_usa
                    on trim(upper(sp_austria.name)) = trim(upper(sp_usa.name))
      where sp_austria.country_id = austria_contry_id.id
        and sp_usa.country_id = usa_contry_id.id
        and sp_austria.name in ('Alabama', 'Alaska', 'Arizona',
                                'Arkansas', 'California', 'Colorado',
                                'Connecticut', 'Delaware', 'Florida')
     ) pair_of_austria_and_usa_ids
where state_province_id = pair_of_austria_and_usa_ids.austria_id;


--2.2 remove duplicate states from Austria
WITH
    austria_contry_id AS (SELECT id FROM countries WHERE name = 'Austria' limit 1)
delete from states_provinces
using austria_contry_id
where country_id = austria_contry_id.id
  and name in ('Alabama', 'Alaska', 'Arizona',
               'Arkansas', 'California', 'Colorado',
               'Connecticut', 'Delaware', 'Florida');