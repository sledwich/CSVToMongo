# CSVToMongo
Reads CSV files and pushes the data to MongoDB

To use simple execute the EatCSV java class 

<p>parameters</p>
<ul>
<li>--mongodb-host  specifies the host uri</li>
<li>--path 			specifies where the CSV files reside</li>
<li>--database		specifies the name of the mongodb to push the CSVs to</li>
<li>--num-threads	specifies the number of threads to use (depends on how many cores your CPU has (don't throttle your CPU)</li>
<li>--batch-size	specifies the number of rows to push to mongodb at one time (1=means push each row as its read from CSV(slow))</li>
<li>--dry-run		specifies to connect to everything but DO NOT POST the data to mongodb</li>
<li>--wait-hrs		specifies how many hows to wait before we kill the threads (give up) (keep it high)</li>
</ul>

<p>The importer will push the CSV into mongodb with the same name as the CSV</p>

<p>EXAMPLE</p>
--mongodb-host=mongodb://mongodb-host.com:27017
--path=/home/user/mycsvs
--database-name=mydb
--batch-size=10000
--num-threads=2