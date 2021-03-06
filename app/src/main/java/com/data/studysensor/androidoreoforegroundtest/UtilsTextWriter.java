package com.data.studysensor.androidoreoforegroundtest;

import android.content.Context;
import android.os.Environment;
import android.text.format.Time;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UtilsTextWriter {
	private static final String TAG = "UtilsTextWriter";
	private static String FILENAME = "DEFAULT.txt";
	
	public static void setFileName(String filename)
	{
		FILENAME = String.format("%s_%s", getCurrentTimeStamp(), filename);
	}

	public static void write(String text)
	{
		Time now = new Time();
		now.setToNow();
		
		FileWriter mFilewriter;
		File mFile;

		try {
//			mFile = new File( Environment.getExternalStoragePublicDirectory("Documents/PageReader") ,	FILENAME);
			mFile = new File( Environment.getExternalStoragePublicDirectory("") ,	FILENAME);
			
			File parentFile = mFile.getParentFile();
			if (parentFile != null)
				parentFile.mkdirs();
			
			if(mFile.exists())
			{
				mFilewriter = new FileWriter(mFile, true);
			}
			else
			{
				mFilewriter = new FileWriter(mFile);
				mFile.createNewFile();
			}
				
			mFilewriter.write(text + "\r\n");
			
			mFilewriter.flush();
			mFilewriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeInternal(Context context, String text)
	{
		Time now = new Time();
		now.setToNow();

		FileWriter mFilewriter;
		File mFile;

		try {
//			mFile = new File( Environment.getExternalStoragePublicDirectory("Documents/PageReader") ,	FILENAME);
			mFile = new File( context.getFilesDir() ,	FILENAME);

			File parentFile = mFile.getParentFile();
			if (parentFile != null)
				parentFile.mkdirs();

			if(mFile.exists())
			{
				mFilewriter = new FileWriter(mFile, true);
			}
			else
			{
				mFilewriter = new FileWriter(mFile);
				mFile.createNewFile();
			}

			mFilewriter.write(text + "\r\n");

			mFilewriter.flush();
			mFilewriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String readInternal(Context context){
		//Find the directory for the SD Card using the API
//*Don't* hardcode "/sdcard"
		File sdcard = context.getFilesDir();

//Get the text file
		File file = new File(sdcard,FILENAME);

//Read text from file
		StringBuilder text = new StringBuilder();

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
			br.close();
		}
		catch (IOException e) {
			//You'll need to add proper error handling here
		}

		return text.toString();
	}
	
	/**
	 * 
	 * @return yyyy-MM-dd HH:mm:ss formate date as string
	 */
	public static String getCurrentTimeStamp(){
	    try {

	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH mm");
	        String currentTimeStamp = dateFormat.format(new Date()); // Find todays date

	        return currentTimeStamp;
	    } catch (Exception e) {
	        e.printStackTrace();

	        return null;
	    }
	}
}
