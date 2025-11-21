package it.unicas.project.template.address.model.dao.mysql;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DAOMySQLSettings {

    public final static String DRIVERNAME = "com.mysql.cj.jdbc.Driver";
    public final static String HOST = "localhost";
    public final static String USERNAME = "amici";
    public final static String PWD = "$$Amici2024$$";
    public final static String SCHEMA = "amici";
    public final static String PARAMETERS = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";


    //String url = "jdbc:mysql://localhost:3306/amici?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";



    //private String driverName = "com.mysql.cj.jdbc.Driver";
    private String host = "localhost";
    private String userName = "amici";
    private String pwd = "$Prova2022$";
    private String schema = "amici";


    public String getHost() {
        return host;
    }

    public String getUserName() {
        return userName;
    }

    public String getPwd() {
        return pwd;
    }

    public String getSchema() {
        return schema;
    }


    public void setHost(String host) {
        this.host = host;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    static{
        try {
            Class.forName(DRIVERNAME);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static DAOMySQLSettings currentDAOMySQLSettings = null;

    public static DAOMySQLSettings getCurrentDAOMySQLSettings(){
        if (currentDAOMySQLSettings == null){
            currentDAOMySQLSettings = getDefaultDAOSettings();
        }
        return currentDAOMySQLSettings;
    }

    public static DAOMySQLSettings getDefaultDAOSettings(){
        DAOMySQLSettings daoMySQLSettings = new DAOMySQLSettings();
        daoMySQLSettings.host = HOST;
        daoMySQLSettings.userName = USERNAME;
        daoMySQLSettings.schema = SCHEMA;
        daoMySQLSettings.pwd = PWD;
        return daoMySQLSettings;
    }

    public static void setCurrentDAOMySQLSettings(DAOMySQLSettings daoMySQLSettings){
        currentDAOMySQLSettings = daoMySQLSettings;
    }


    public static Statement getStatement() throws SQLException{
        if (currentDAOMySQLSettings == null){
            currentDAOMySQLSettings = getDefaultDAOSettings();
        }
        return DriverManager.getConnection("jdbc:mysql://" + currentDAOMySQLSettings.host  + "/" + currentDAOMySQLSettings.schema + PARAMETERS, currentDAOMySQLSettings.userName, currentDAOMySQLSettings.pwd).createStatement();
    }

    public static void closeStatement(Statement st) throws SQLException{
        st.getConnection().close();
        st.close();
    }

}
