--load the data from HDFS and define the schema
-- vaccination data - not required for this script.
vdatacsv = LOAD '/data/vaccination-data.csv' USING PigStorage(',') AS (country:CHARARRAY, iso3:CHARARRAY, who_region:CHARARRAY, persons_fully_vaccinated:INT);

-- group by who region
grpd = GROUP vdatacsv BY who_region;

-- sum by who_region
smmd = foreach grpd generate ($0), COUNT(vdatacsv.country) as c, SUM(vdatacsv.persons_fully_vaccinated) as d;

--order
orderd = ORDER smmd by $0;

-- display the results.
DUMP orderd;
