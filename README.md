# CSVToMongo
Reads CSV files and pushes the data to MongoDB

To use simple execute the EatCSV java class 

parameters 
--mongodb-host  specifies the host uri
--path 			specifies where the CSV files reside
--database		specifies the name of the mongodb to push the CSVs to
--num-threads	specifies the number of threads to use (depends on how many cores your CPU has (don't throttle your CPU)
--batch-size	specifies the number of rows to push to mongodb at one time (1=means push each row as its read from CSV(slow))
--dry-run		specifies to connect to everything but DO NOT POST the data to mongodb
--wait-hrs		specifies how many hows to wait before we kill the threads (give up) (keep it high)

EXAMPLE
--mongodb-host=mongodb://mongodb-host.com:27017
--path=/home/user/mycsvs
--database-name=mydb
--batch-size=10000
--num-threads=2