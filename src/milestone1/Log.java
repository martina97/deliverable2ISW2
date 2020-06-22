package milestone1;
import java.util.logging.*;

public class Log {

		//Class which creates  2 log (console e file) 
		
		private Log() {
			throw new IllegalStateException("Log class");
		}
		
		private static final Logger myLog = Logger.getLogger( Logger.GLOBAL_LOGGER_NAME );     
	    
	    public static void setupLogger() {
	    	
	        LogManager.getLogManager().reset();
	        myLog.setLevel(Level.ALL);
	        
	        ConsoleHandler ch = new ConsoleHandler();
	        ch.setLevel(Level.FINE);
	        myLog.addHandler(ch);

	        try {
	            FileHandler fh = new FileHandler("Logger.log", true);
	            fh.setLevel(Level.FINE);
	            myLog.addHandler(fh);
	        } catch (java.io.IOException e) {            
	            // don't stop my program but log out to console.
	        	myLog.log(Level.SEVERE, "File logger not working.", e);
	        }
	         /* 
	         Different Levels in order.
	          OFF
	          SEVERE
	          WARNING
	          INFO
	          CONFIG
	          FINE
	          FINER
	          FINEST
	          ALL
	        */
	    } 
	    
	    public static void infoLog(String msg ) {
	    	
	    	myLog.info(msg);
	    }
	    
	    public static void errorLog(String msg ) {
	    	
	    	myLog.severe(msg);
	    }
	    
	   
	}


		//Class which creates  2 log (console e file) 
		/*
		private Log() {
			throw new IllegalStateException("Log class");
		}
		
		private static final Logger myLog = Logger.getLogger( Logger.GLOBAL_LOGGER_NAME );     
	    
	    public static void setupLogger() {
	    	
	        LogManager.getLogManager().reset();
	        myLog.setLevel(Level.ALL);
	        
	        // stampa su console solo i msg da FINE a SEVERE
	        ConsoleHandler ch = new ConsoleHandler();
	        ch.setLevel(Level.FINE);
	        myLog.addHandler(ch);

	        try {
	            FileHandler fh = new FileHandler("Logger.log", true);
	            fh.setLevel(Level.FINE);
	            myLog.addHandler(fh);
	        } catch (java.io.IOException e) {            
	            // don't stop my program but log out to console.
	        	myLog.log(Level.SEVERE, "File logger not working.", e);	//autoregistra eccexione se non riesce ad aprire il file
	        }
	         /* 
	         Different Levels in order.
	          OFF
	          SEVERE
	          WARNING
	          INFO
	          CONFIG
	          FINE
	          FINER
	          FINEST
	          ALL
	        */
	/*    } 
	    
	    public static void infoLog(String msg ) {	//scrive msg su logger
	    	
	    	myLog.info(msg);
	    }
	    
		public static void errorLog(String msg ) {	//scrive msg CRITICO su logger
	    	
	    	myLog.severe(msg);
	    	
	    }*/
	   
	

