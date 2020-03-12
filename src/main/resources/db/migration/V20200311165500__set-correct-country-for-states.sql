-- The order of the queries is critically important, so please do not change it.

-- Step 1.
-- find states that related to the wrong country(Austria instead of USA)
-- 1.1 set USA country for these states in users_data table
update users_data
set country_id = (SELECT max(id) FROM countries WHERE name = 'United States of America (the)')
from (select sp_austria.id
      from states_provinces sp_austria
         where 0=0
         and name in ('Alabama', 'Alaska', 'Arizona',
                      'Arkansas', 'California', 'Colorado',
                      'Connecticut', 'Delaware', 'Florida')
         and country_id = (SELECT max(id) FROM countries WHERE name = 'Austria')
         and not exists(
                        select *
                        from states_provinces sp_usa
                        where country_id = (SELECT max(id) FROM countries WHERE name = 'United States of America (the)')
                        and sp_austria.name = sp_usa.name)
     ) austria_ids
where state_province_id = austria_ids.id;

--1.2 move states to the USA
update states_provinces sp_austria set
    country_id = (SELECT max(id) FROM countries WHERE name = 'United States of America (the)')
where 0=0
  and name in ('Alabama', 'Alaska', 'Arizona',
               'Arkansas', 'California', 'Colorado',
               'Connecticut', 'Delaware', 'Florida')
  and country_id = (SELECT max(id) FROM countries WHERE name = 'Austria')
  and not exists(
        select *
        from states_provinces sp_usa
        where country_id = (SELECT max(id) FROM countries WHERE name = 'United States of America (the)')
          and sp_austria.name = sp_usa.name
    );

--Step 2. find duplicate states(the states that should be under USA but also mistakenly exists in Austria)
--2.1 fix state_id and country_id in the users_data table
update users_data
set state_province_id = pair_of_austria_and_usa_ids.usa_id,
    country_id = (SELECT max(id) FROM countries WHERE name = 'United States of America (the)')
from (select sp_austria.id austria_id,
             sp_usa.id     usa_id
      from states_provinces sp_austria
      join states_provinces sp_usa
        on trim(upper(sp_austria.name)) = trim(upper(sp_usa.name))
       where sp_austria.country_id = (SELECT max(id) FROM countries WHERE name = 'Austria')
         and sp_usa.country_id = (SELECT max(id) FROM countries WHERE name = 'United States of America (the)')
         and sp_austria.name in ('Alabama', 'Alaska', 'Arizona',
                          'Arkansas', 'California', 'Colorado',
                          'Connecticut', 'Delaware', 'Florida')
    ) pair_of_austria_and_usa_ids
where state_province_id = pair_of_austria_and_usa_ids.austria_id;


--2.2 remove duplicate states from Austria
delete from states_provinces
where country_id = (SELECT max(id) FROM countries WHERE name = 'Austria')
  and name in ('Alabama', 'Alaska', 'Arizona',
               'Arkansas', 'California', 'Colorado',
               'Connecticut', 'Delaware', 'Florida');