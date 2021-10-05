package core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class EatCSV {

	public static void main(String[] args) {
		final ArrayList<AppParameters> params = new ArrayList<AppParameters>();
		AppParameters numThreads = new AppParameters(params, false, false, "num-threads", "1");
		AppParameters mongoURL = new AppParameters(params, true, false, "mongodb-host", "1");
		
		AppParameters createCollection = new AppParameters(params, false, true, "create-collection");
		AppParameters dryRun = new AppParameters(params, false, true, "dry-run");
		AppParameters databaseName = new AppParameters(params, true, false, "database-name");
		AppParameters path = new AppParameters(params, true, false, "path");
		AppParameters waitHrs = new AppParameters(params, false, false, "wait-hrs", "10");
		AppParameters batchSize = new AppParameters(params, false, false, "batch-size", "10000");
		
		AppParameters.checkParam(args, params);
		
		try {
			createThreads(mongoURL.toString(), numThreads.toInt(), createCollection.toBool(), databaseName.toString(), path.toString(), waitHrs.toInt(), dryRun.toBool(), batchSize.toInt());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createThreads(final String mongoURL, final int threadCount, final boolean createCollection, final String dbname, final String pathName, long waitHrs, boolean dryRun, int batchSize) throws IOException {
		final ExecutorService service = Executors.newFixedThreadPool(threadCount);
		
		final Path dir = Paths.get(pathName);

		Collection<File> files = FileUtils.listFiles(dir.toFile(), new WildcardFileFilter("*.csv"),FileFilterUtils.trueFileFilter());
		if( files != null )
		{
			for( File file: files) {
				final String collectionName = FilenameUtils.getBaseName(file.toString());
				service.submit(new PostCSV(file, mongoURL, dbname, collectionName, createCollection, dryRun, batchSize));
			}
		}
			
		try {
			service.shutdown();
			service.awaitTermination(waitHrs, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
