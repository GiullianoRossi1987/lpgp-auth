package datacore;

import java.lang.Exception;
import java.sql.*;
import java.lang.ClassNotFoundException;
import java.util.ArrayList;
import config.SocketConfig;
import org.json.*;
import org.jetbrains.annotations.*;
import java.io.IOException;
import control.ClientsDownloadController;


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

    public static class AuthenticationError extends Exception{

        public AuthenticationError(String msg) { super("Authentication error: " + msg);}
    }

    public static class ClientNotFoundError extends Exception{

        public ClientNotFoundError(String clientRef){
            super("Couldn't find client, using name reference: " + clientRef);
        }
    }

    public static class ProprietaryReferenceError extends Exception{

        public ProprietaryReferenceError(int propRef){ super("Invalid proprietary " + propRef);}
    }

    public ClientsController(String configurationsFile, String host, String db) throws AlreadyConnectedError,
            ExternalDriverError, ClassNotFoundException, SocketConfig.fetchingError, SocketConfig.ConfigurationsAlreadyLoaded{
        super(configurationsFile, host, db);
    }

    public boolean checkClientExists(String nameReference) throws NotConnectedError, SQLException{
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

    public boolean checkClientExists(int ref) throws NotConnectedError, SQLException{
        if(!this.gotConnection) throw new NotConnectedError();
        Statement cursor = this.connectionMain.createStatement();
        cursor.setMaxRows(1);
        ResultSet totalClients = cursor.executeQuery("SELECT COUNT(cd_client) AS \"countage\" FROM tb_clients WHERE cd_client =" + ref + " LIMIT 1;");
        int exists;
        if(totalClients.next()) {
            exists = totalClients.getInt("countage");
        }
        else {
            exists = 0;
        }
        return exists > 0;
    }

    private boolean checkProprietaryReference(int prop) throws NotConnectedError, SQLException{
        if(!this.gotConnection) throw new NotConnectedError();
        Statement cursor = this.connectionMain.createStatement();
        cursor.setMaxRows(1);
        ResultSet countage = cursor.executeQuery("SELECT COUNT(cd_proprietary) AS 'countage' FROM tb_proprietaries WHERE cd_proprietary = " + prop);
        return countage.next() && countage.getInt("countage") > 0;
    }

    public boolean authClientData(int clientId, String date, String cdtk, String tk, int prop) throws NotConnectedError,
        SQLException, ClientNotFoundError, ProprietaryReferenceError,  AuthenticationError{
        if(!this.gotConnection) throw new NotConnectedError();
        if(!this.checkClientExists(clientId)) throw new ClientNotFoundError("" + clientId);
        if(!this.checkProprietaryReference(prop)) throw new ProprietaryReferenceError(prop);
        try{
            ClientsDownloadController cdc = new ClientsDownloadController(this, this.configurationsLocal.getRecorder());
            return cdc.authClientsData(date, cdtk, clientId);
        }
        catch (Exception e) {throw new AuthenticationError(e.getMessage()); }
    }

    public int getPkByName(String name) throws NotConnectedError, SQLException, ClientNotFoundError{
        if(!this.gotConnection) throw new NotConnectedError();
        if(!this.checkClientExists(name)) throw new ClientNotFoundError(name);
        Statement stmt = this.connectionMain.createStatement();
        stmt.setMaxRows(1);
        ResultSet rt = stmt.executeQuery("SELECT cd_client FROM tb_clients WHERE nm_client = \"" + name +"\";");
        if(rt.next()) return rt.getInt("cd_client");
        else return 0;
    }
}