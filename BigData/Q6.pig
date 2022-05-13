popcsv = LOAD '/data/pop.csv' USING PigStorage(',') AS (country:CHARARRAY, population:INT);
  
vmetacsv = LOAD '/data/vaccination-metadata.csv' USING PigStorage(',') AS (iso3:CHARARRAY, vaccine_name:CHARARRAY, product_name:CHARARRAY, company_name:CHARARRAY);

vdatacsv = LOAD '/data/vaccination-data.csv' USING PigStorage(',') AS (country:CHARARRAY, iso3:CHARARRAY, who_region:CHARARRAY, persons_fully_vaccinated:INT);

--count companies first
companies_grouped = GROUP vmetacsv BY iso3;
companies_counted = FOREACH companies_grouped GENERATE $0 AS comp, COUNT(vmetacsv.company_name) as numcompanies;

--join the tables

join_vacc = JOIN vdatacsv by iso3, companies_counted by $0;

join_pop = JOIN popcsv by country, join_vacc by $0;


--desired output
tbl = FOREACH join_pop GENERATE $0, $1, ($5/$1) * 0.1, $7;

--consider only high population countries
tbl_filter = FILTER tbl by $1 > 10000;

--order by desc num of pop

tbl_ordered = ORDER tbl_filter by $1 desc;


dump tbl_ordered;







