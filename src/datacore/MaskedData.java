package datacore;

import org.json.*;
import java.lang.Exception;
import org.jetbrains.annotations.*;
import java.io.*;
import java.util.Scanner;


public class MaskedData {

	private String rawContent = null;
	private String decodedContent = null;
	private JSONObject parsedPure = null;
	private boolean gotData = false;

	public static final String MASK_DELIMITER = "/";

	public static class MaskDelimiterError extends Exception {

		public MaskDelimiterError(String usingDelimiter) {
			super("The data received is masked using other delimiter then '" + usingDelimiter + "'");
		}
	}

	public static class DataDecodingError extends Exception {

		public DataDecodingError(String message) {
			super("The JSONObject (org.json) couldn't decode the content received: " + message);
		}
	}

	public static class DataAlreadyLoaded extends Exception{

		public DataAlreadyLoaded(){
			super("There're data already loaded!");
		}
	}

	public static class DataNotLoaded extends Exception{

		public DataNotLoaded(){
			super("There're no data loaded yet");
		}
	}

	@NotNull
	public static String unmaskRoot(@NotNull String contentMaskered) throws MaskDelimiterError{
		if(!contentMaskered.contains(MASK_DELIMITER)) throw new MaskDelimiterError(MASK_DELIMITER);
		String[] splt = contentMaskered.split(MASK_DELIMITER);
		for(String cl : splt) System.out.print(cl);
		StringBuilder jsonContentBuilder = new StringBuilder();
		for(String chS : splt){
			int numRepresentation = Integer.parseInt(chS);
			char ascii = (char)numRepresentation;
			jsonContentBuilder.append(ascii);
		}
		return jsonContentBuilder.toString();
	}

	@NotNull
	private JSONObject readJSONData(@NotNull String data) throws DataDecodingError{
		try{
			return new JSONObject(this.unmaskRoot(data));
		}
		catch(Exception error){
			throw new DataDecodingError(error.getMessage());
		}
	}

	public MaskedData(String data) throws DataAlreadyLoaded, MaskDelimiterError, DataDecodingError{
		if(this.gotData) throw new DataAlreadyLoaded();
		this.rawContent = data;
		this.decodedContent = this.unmaskRoot(data);
		this.parsedPure = this.readJSONData(data);
		this.gotData = true;
	}

	public void loadData(String data) throws DataAlreadyLoaded, MaskDelimiterError, DataDecodingError{
		if(this.gotData) throw new DataAlreadyLoaded();
		this.rawContent = data;
		this.decodedContent = this.unmaskRoot(data);
		this.parsedPure = this.readJSONData(data);
		this.gotData = true;
	}

	public int getClientId() throws DataNotLoaded{
		if(!this.gotData) throw new DataNotLoaded();
		return this.parsedPure.getInt("ClientId");
	}

	public String getRawContent() throws DataNotLoaded{
		if(!this.gotData || this.rawContent.length() == 0) throw new DataNotLoaded();
		else return this.rawContent;
	}

	public String getDecodedContent() throws DataNotLoaded{
		if(!this.gotData || this.decodedContent.length() == 0) throw new DataNotLoaded();
		else return this.decodedContent;
	}

	public JSONObject getParsedPure() throws DataNotLoaded{
		if(!this.gotData) throw new DataNotLoaded();
		else return this.parsedPure;
	}

	public void readFile(@NotNull String path) throws DataAlreadyLoaded, DataDecodingError, MaskDelimiterError, IOException{
		File fl = new File(path);
		Scanner reader = new Scanner(fl);
		String content = reader.nextLine();  // only one line, like every LPGP content.
		// debug: System.out.println(content);
		this.loadData(content);
	}

	public MaskedData(){
		// empty constructor
		this.rawContent = null;
		this.decodedContent = null;
		this.parsedPure = null;
		this.gotData = false;
	}
}
