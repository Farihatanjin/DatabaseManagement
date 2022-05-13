vmetacsv = LOAD '/data/vaccination-metadata.csv' USING PigStorage(',') AS (iso3:CHARARRAY, vaccine_name:CHARARRAY, product_name:CHARARRAY, company_name:CHARARRAY);

grpd = GROUP vmetacsv BY company_name;

smmd = FOREACH grpd GENERATE ($0), COUNT(vmetacsv.iso3) as numcountries;

ordered = ORDER smmd by $1 desc;

top = limit ordered 10;

DUMP top;

