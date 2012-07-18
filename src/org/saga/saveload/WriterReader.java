package org.saga.saveload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import org.saga.SagaLogger;
import org.saga.abilities.Ability;
import org.saga.buildings.Building;
import org.saga.buildings.signs.BuildingSign;
import org.saga.chunkGroups.ChunkGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WriterReader {

	
	/**
	 * Substitution string for name.
	 */
	public static String NAME_SUBS = "#";
	
	
	/**
	 * Writes a file.
	 * 
	 * @param dir directory
	 * @param name name
	 * @throws IOException thrown when read fails
	 */
	public static void write(Directory dir, String name, Object obj) throws IOException {
		
		
		// Gson:
		GsonBuilder gsonBuilder= new GsonBuilder();
		
		Gson gson = gsonBuilder.create();
		
		String objStr = gson.toJson(obj);
		
		if(objStr == null || objStr.equals("null")){
			SagaLogger.severe(WriterReader.class, "null config for " + obj);
			return;
		}
		
		// Directory:
		File directory = new File(dir.getDirectory());
		if(!directory.exists()){
			directory.mkdirs();
			SagaLogger.info("Creating " + directory + " directory.");
		}
		
		// File:
		File filedir = new File(dir.getDirectory() + dir.getFilename().replace(NAME_SUBS, name));
		if(!filedir.exists()){
			filedir.createNewFile();
			SagaLogger.info("Creating " + filedir + " file.");
		}
        
		// Write:
		BufferedWriter out = new BufferedWriter(new FileWriter(filedir));
		out.write(objStr);
		out.close();

         
	}

	/**
	 * Reads a file.
	 * 
	 * @param writeType write type
	 * @param name name
	 * @param configType configuration type
	 * @throws IOException thrown when read fails
	 */
	public static <T> T read(Directory dir, String name, Class<T> type) throws IOException {

		
		// Gson:
        GsonBuilder gsonBuilder= new GsonBuilder();
        
		gsonBuilder.registerTypeAdapter(ChunkGroup.class, new SagaCustomSerializer());
		gsonBuilder.registerTypeAdapter(Building.class, new SagaCustomSerializer());
		gsonBuilder.registerTypeAdapter(BuildingSign.class, new SagaCustomSerializer());
		
		gsonBuilder.registerTypeAdapter(Ability.class, new SagaCustomSerializer());
		
		Gson gson = gsonBuilder.create();
	    

		// Directory:
		File directory = new File(dir.getDirectory());
		if(!directory.exists()){
			directory.mkdirs();
			SagaLogger.info("Creating " + directory + " directory.");
		}
		
		// File:
		File filedir = new File(dir.getDirectory() + dir.getFilename().replace(NAME_SUBS, name));
		if(!filedir.exists()){
			filedir.createNewFile();
			SagaLogger.info("Creating " + filedir + " file.");
		}
		
		// Read:
        int ch;
        StringBuffer objStr = new StringBuffer("");
        FileInputStream fin = null;
        fin = new FileInputStream(filedir);
        while ((ch = fin.read()) != -1){
            objStr.append((char) ch);
        }
        fin.close();

        // Fix null and empty:
        if(objStr.length() == 0 || objStr.toString().equals("null")){
        	SagaLogger.severe(WriterReader.class, "null or empty config for " + dir);
        	objStr = new StringBuffer("{ }");
        }
          
        return gson.fromJson(objStr.toString(), type);

         
	}
	

	/**
	 * Writes a file.
	 * 
	 * @param dir directory
	 * @throws IOException thrown when read fails
	 */
	public static void write(Directory dir, Object obj) throws IOException {
		
		write(dir, "", obj);
         
	}

	/**
	 * Reads a file.
	 * 
	 * @param writeType write type
	 * @param configType configuration type
	 * @throws IOException thrown when read fails
	 */
	public static <T> T read(Directory dir, Class<T> type) throws IOException {
		
        return read(dir, "", type);
         
	}
	

	
	
	/**
	 * Checks if the file exists.
	 * 
	 * @param dir file directory
	 * @param name file name
	 * @return true if exists
	 */
	public static boolean checkExists(Directory dir, String name){
		
		return new File(dir.getDirectory() + dir.getFilename().replace(NAME_SUBS, name)).exists();
		
	}

	/**
	 * Gets all IDs.
	 * 
	 * @param directory directory
	 * @return all IDs
	 */
	public static String[] getAllIds(Directory directory) {

		
		File dir = new File(directory.getDirectory());
		FilenameFilter filter = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(Directory.FILE_EXTENTENSION);
			}
			
		};
		
		if(!dir.exists()){
			dir.mkdirs();
			SagaLogger.info("Creating "+dir+" directory.");
		}
		
		String[] names = dir.list(filter);
		
		if(names == null){
			SagaLogger.severe(WriterReader.class, "could not retrieve faction names");
			names = new String[0];
		}
		
		// Remove extensions:
		for (int i = 0; i < names.length; i++) {
			names[i] = names[i].replaceAll(Directory.FILE_EXTENTENSION, "");
		}
		
		return names;

		
	}

	/**
	 * Moves the file to the directory for deleted files.
	 * 
	 * @param dir path
	 * @param name file name
	 */
	public static void delete(Directory dir, String name) {
		
		
		// Create folders:
		File newDir = new File(dir.getDeletedDirectory());
		File oldDir = new File(dir.getDirectory());
		File newFile = new File(dir.getDeletedDirectory() + dir.getFilename().replace(NAME_SUBS, name));
		File oldFile = new File(dir.getDirectory() + dir.getFilename().replace(NAME_SUBS, name));

		
		if(!newDir.exists()){
			newDir.mkdirs();
			SagaLogger.info("Creating " + newDir + " directory.");
		}
		
		if(!oldDir.exists()){
			oldDir.mkdirs();
			SagaLogger.info("Creating " + oldDir + " directory.");
		}
		
		// Check if exists.
		if(!oldFile.exists()){
			SagaLogger.severe(WriterReader.class, "failed to move " + oldFile + ", because it doesent exist");
		}
		
		// Rename if target exists:
		for (int i = 1; i < 1000; i++) {
			if (newFile.exists()) {
				newFile.renameTo(new File(dir.getDeletedDirectory() + dir.getFilename().replace(NAME_SUBS, name + " (" + i + ")")));
			}else{
				break;
			}
			
		}
		
		// Move file to deleted folder:
		boolean success = oldFile.renameTo(newFile);
		
		// Notify on failure:
		if(success){
			SagaLogger.info("Moved " + oldFile.getName() + " file to " + newDir + ".");
		}else{
			SagaLogger.info("Failed to move " + oldFile.getName() + " file to " + newDir + ".");
		}
		

	}
	
	
}