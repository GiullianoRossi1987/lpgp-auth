package datacore;

import java.lang.Exception;
import java.sql.*;
import java.lang.ClassNotFoundException;
import java.util.ArrayList;
import config.SocketConfig;
import org.json.*;
import org.jetbrains.annotations.*;

public class ClientsController extends Connector{

    public static final String DELIMITER = "/";

    public static class UnmaskingError extends Exception{

        public UnmaskingError(String message){
            super("Unmasking error: " + message);
        }
    }

    public static class ClientTokenAuthenticationError extends Exception{

        public ClientTokenAuthenticationError(String authError){
            super("Client token authentication error: " + authError);
        }
    }

    public static class ClientNotFoundError extends Exception{

        public ClientNotFoundError(String clientRef){
            super("Couldn't find client, using name reference: " + clientRef);
        }
    }

    public ClientsController(String configurationsFile, String host, String db) throws AlreadyConnectedError,
            ExternalDriverError, ClassNotFoundException, SocketConfig.fetchingError, SocketConfig.ConfigurationsAlreadyLoaded{
        super(configurationsFile, host, db);
    }

    private boolean checkClientExists(String nameReference) throws NotConnectedError, SQLException{
        if(!this.gotConnection) throw new NotConnectedError();
        Statement cursor = this.connectionMain.createStatement();
        cursor.setMaxRows(1);
        ResultSet totalClients = cursor.executeQuery("SELECT COUNT(cd_client) AS \"countage\" FROM tb_clients WHERE nm_client = \"" + nameReference + "\" LIMIT 1;");
        int exists;
        if(totalClients.next()) {
            exists = totalClients.getInt("countage");
        }
        else {
            exists = 0;
        }
        return exists > 0;
    }

    @NotNull
    private String unmaskRoot(@NotNull String rootMask) throws UnmaskingError{
        try{
            if(!rootMask.contains(DELIMITER)) throw new UnmaskingError("Delimiter not used in it");
            String[] delimiterSplot = rootMask.split(DELIMITER);
            StringBuilder readableContentBuilder = new StringBuilder();
            for(String chr : delimiterSplot){
                int numRef = Integer.parseInt(chr);
                char chrValue = (char)numRef;
                readableContentBuilder.append(chrValue);
            }
            return readableContentBuilder.toString();
        }
        catch(Exception error){ throw new UnmaskingError("Couldn't decode the mask code!");}
    }

    @NotNull
    private JSONObject convertSchema(@NotNull String rootMaskered) throws UnmaskingError{
        try{
            String unmaskedContent = this.unmaskRoot(rootMaskered);
            return new JSONObject(unmaskedContent);
        }
        catch(Exception error){
            throw new UnmaskingError(error.getMessage());
        }
    }


}