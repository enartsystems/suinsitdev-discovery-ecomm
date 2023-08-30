/**
 * 
 */
package com.suinsit.webapp.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.enartframework.core.shared.logger.EnartLoggerFactory;
import org.enartframework.core.shared.logger.IEnartLogger;
import org.springframework.core.env.Environment;

import com.sun.nio.file.SensitivityWatchEventModifier;

/**
 * @author manuel
 *
 */
public class Configura implements Serializable {
	org.springframework.core.env.Environment environment;
	
	public Configura(Environment environment) {
		super();
		this.environment = environment;
	}

	ExecutorService pool = Executors.newFixedThreadPool(10);
	private static final IEnartLogger logger = EnartLoggerFactory.getLogger(Configura.class, "CONFIGURA");

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
	private void copyDirectory() throws FileNotFoundException, IOException {
		if (System.getenv(ENVIROMENTS.SUINSIT_VERSIONS.name()) != null) {
			boolean copy = true;
			if (new File(System.getenv(ENVIROMENTS.SUINSIT_VERSIONS.name()) + "/BASE/release.properties").exists()) {
				if (new File(System.getenv(ENVIROMENTS.SUINSIT_HOME.name()) + "/BASE/" + "/release.properties")
						.exists()) {
					Properties propBase = new Properties();
					propBase.load(new FileInputStream(
							new File(System.getenv(ENVIROMENTS.SUINSIT_VERSIONS.name()) + "/BASE/release.properties")));
					logger.info("version base :" + propBase.getProperty("version"));
					Properties propVersion = new Properties();
					propVersion.load(new FileInputStream(new File(
							System.getenv(ENVIROMENTS.SUINSIT_HOME.name()) + "/BASE/" + "/release.properties")));
					if (propVersion.getProperty("version").equals(propBase.getProperty("version"))) {
						copy = false;
					}
				}
			}
			if (copy) {
				FileUtils.copyDirectory(new File(System.getenv(ENVIROMENTS.SUINSIT_VERSIONS.name()) + "/"),
						new File(System.getenv(ENVIROMENTS.SUINSIT_HOME.name())));
			}
			logger.info("USABLE SPACE :" + new File(System.getenv("SUINSIT_HOME")).getUsableSpace() / 1024);
		}
	}
	public void updateBaseFolder() {

	// copiamos directorio interno
		try {
			copyDirectory();
			pool.execute(new Runnable() {
				@Override
				public void run() {
					monitoringDirectory();
				}
			});
			
			pool.shutdown();
		} catch (IOException e) {
			logger.error(e);
		}

	}


	private File rootFolder;

    private WatchService watcher;

    private ExecutorService executor;
	private void startRecursiveWatcher() throws IOException {
           final Map<WatchKey, Path> keys = new HashMap<>();
           rootFolder = Paths.get(System.getenv(ENVIROMENTS.SUINSIT_VERSIONS.name())+"/BASE").toFile(); 
           watcher = FileSystems.getDefault().newWatchService();
           executor = Executors.newSingleThreadExecutor();
           
        Consumer<Path> register = p -> {
            if (!p.toFile().exists() || !p.toFile().isDirectory()) {
                throw new RuntimeException("folder " + p + " does not exist or is not a directory");
            }
            try {
                Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    	System.err.println("registering " + dir + " in watcher service");
                        WatchKey watchKey = dir.register(watcher, new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_CREATE, 
  			                StandardWatchEventKinds.ENTRY_MODIFY}, SensitivityWatchEventModifier.HIGH);
                        keys.put(watchKey, dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException("Error registering path " + p);
            }
        };

        register.accept(rootFolder.toPath());

        executor.submit(() -> {
            while (true) {
                final WatchKey key;
                try {
                    key = watcher.take(); // wait for a key to be available
                } catch (InterruptedException ex) {
                    return;
                }

                final Path dir = keys.get(key);
                if (dir == null) {
                    System.err.println("WatchKey " + key + " not recognized!");
                    continue;
                }
              
//					    for (WatchEvent<?> event : key.pollEvents()) {
//					    	System.out.println(event.kind());
//					    	System.out.println(dir.getFileName());
//					    	if(dir.toFile().isDirectory()) {
//					    		 register.accept(dir.getFileName());
//					    	}else {
//					    		System.out.println(dir.getFileName());
//						        System.out.println(
//						          "Event kind:" + event.kind() 
//						            + ". File affected: " + event.context() + ".");	
//					    	}
//					    	
//					    	
//					    }
//					    key.reset();
                Iterator itera = key.pollEvents().stream().filter(e -> e.kind()!=StandardWatchEventKinds.OVERFLOW).iterator();
                while(itera.hasNext()) {
                	
                	((WatchEvent<?> )itera.next()).context();
                }
        
        
                key.pollEvents().stream()
                        .filter(e -> (e.kind() != StandardWatchEventKinds.OVERFLOW))
                        .map(e -> ((WatchEvent<Path>) e).context())
                        .forEach(p -> {
                            final Path absPath = dir.resolve(p);
                            if (absPath.toFile().isDirectory()) {
                                register.accept(absPath);
                            } else {
                                final File f = absPath.toFile();
                                System.out.println("Detected file " + f.getAbsolutePath());
                            }
                        });

                boolean valid = key.reset(); // IMPORTANT: The key must be reset after processed
                if (!valid) {
                    break;
                }
            }
        });
    }
	private void monitoringDirectory(String dir) {
		
		
	}
	private void monitoringDirectory() {
		 try {
			 if(!new File(System.getenv(ENVIROMENTS.SUINSIT_VERSIONS.name())+"/BASE").exists())return;
			WatchService watchService = FileSystems.getDefault().newWatchService();
			Path p = Paths.get(System.getenv(ENVIROMENTS.SUINSIT_VERSIONS.name())+"/BASE");
			try {
                Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    	if(dir.toFile().isDirectory()) {
                    		WatchKey watchKey = dir.register(
                			          watchService, 
            			                StandardWatchEventKinds.ENTRY_MODIFY);
                              try {
                                WatchKey key;
          						while ((key = watchService.take()) != null) {
          						    for (WatchEvent<?> event : key.pollEvents()) {
          						    	System.out.println(dir.getFileName());
          						        System.out.println(
          						          "Event kind:" + event.kind() 
          						            + ". File affected: " + event.context() + ".");
          						        if(event.context().toString().startsWith("release")) {
          						        	copyDirectory();
          						        }
          						    }
          						    key.reset();
          						}
          					} catch (InterruptedException e) {
          						// TODO Auto-generated catch block
          						e.printStackTrace();
          					}	
                    		
                    	}
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException("Error registering path " + p);
            }
			
			
			
			
			 
			   
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	

}
