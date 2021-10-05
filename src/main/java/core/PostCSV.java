package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

public class PostCSV implements Runnable {

	private static final Logger logger = LogManager.getLogger(PostCSV.class);
	
	final File csvFile ;
	final String mongoUrl ;
	final String mongoDatabaseName;
	final String mongoCollectionName;
	final boolean isCreateCollection;
	final boolean isDryRun;
	final int batchSize;
	
	public PostCSV(File csvFilename, String mongoURL, String mongoDatabaseName, String mongoCollectionName, boolean isCreateCollection, boolean isDryRun, int batchSize) {
		this.csvFile = csvFilename;
		this.mongoUrl = mongoURL;
		this.mongoCollectionName = mongoCollectionName;
		this.mongoDatabaseName = mongoDatabaseName;
		this.isCreateCollection = isCreateCollection;
		this.isDryRun = isDryRun;
		this.batchSize = batchSize;
	}

	@Override
	public void run() {
		logger.info("Reading {}", csvFile);
		try(BufferedReader breader = Files.newBufferedReader( csvFile.toPath() ))
		{
			final CSVReader reader = new CSVReader( breader );
        	
			pushCSV(reader);
        	
        	reader.close();
        	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
		
	}

	private void pushCSV(CSVReader reader) throws IOException, CsvException {
		logger.info("Connecting to {}", mongoUrl);
		try (com.mongodb.client.MongoClient mongoClient = MongoClients.create(mongoUrl)) 
		{
			//GET THE DB
			final MongoDatabase database = mongoClient.getDatabase(mongoDatabaseName);
			
			//DO WE NEED TO CREATE THE COLLECTION?
			if( isCreateCollection )
				database.createCollection(mongoCollectionName);
			
			//GET THE COLLECTION
			final MongoCollection<Document> collection = database.getCollection(mongoCollectionName);
			
			//KEEP TRACK OF TIMING
			final StopWatch stopwatch = StopWatch.createStarted();
			
			//READ HEADERS
			final String[] headers = reader.readNext();
			
			//READ THE CONTENT (INTO MEMORY)
			final List<String[]> content = reader.readAll();

			//GET SOME DOCUMENTS READY TO PUSH TO MONGO
			final List<Document> docList = new ArrayList<>( content.size()>batchSize? batchSize:content.size() );

			content.forEach(row -> {
	        	
				final Document doc = new Document();
				
				int i=0;
				for( String item: row ) {
					if( i>=headers.length)
						break;
					doc.append(headers[i++], item);
				}
				docList.add(doc);
				
				if(!isDryRun && docList.size()>batchSize) {
					collection.insertMany(docList);
					docList.clear();
				}
	        });
			
			//ADD THE FINAL BATCH
			if( !docList.isEmpty() )
				collection.insertMany(docList);

			stopwatch.stop();
			
			double perf = (content.size()/ (double)stopwatch.getTime());
			
			logger.info("Finished {} {} inserted {} rows in {} performance {} records per millisecond.", mongoCollectionName, isDryRun?"would be":"", content.size(), stopwatch.formatTime(), String.format("%.2f", perf));			
		}
		
	}

}
