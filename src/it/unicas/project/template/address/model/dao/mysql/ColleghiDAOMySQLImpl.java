package it.unicas.project.template.address.model.dao.mysql;

import it.unicas.project.template.address.model.Amici;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.util.DateUtil;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ColleghiDAOMySQLImpl implements DAO<Amici> {

    private ColleghiDAOMySQLImpl(){}

    private static DAO dao = null;
    private static Logger logger = null;

    public static DAO getInstance(){
        if (dao == null){
            dao = new ColleghiDAOMySQLImpl();
            logger = Logger.getLogger(ColleghiDAOMySQLImpl.class.getName());
        }
        return dao;
    }

    public static void main(String args[]) throws DAOException {
        ColleghiDAOMySQLImpl c = new ColleghiDAOMySQLImpl();


        c.insert(new Amici("Mario", "Rossi", "0824981", "molinara@uni.it", "21-10-2017", null));
        c.insert(new Amici("Carlo", "Ciampi", "0824982", "ciampi@uni.it", "22-02-2017", null));
        c.insert(new Amici("Ornella", "Vaniglia", "0824983", "vaniglia@uni.it", "23-05-2017", null));
        c.insert(new Amici("Cornelia", "Crudelia", "0824984", "crudelia@uni.it", "24-05-2017", null));
        c.insert(new Amici("Franco", "Bertolucci", "0824985", "bertolucci@uni.it", "25-10-2017", null));
        c.insert(new Amici("Carmine", "Labagnara", "0824986", "lagbagnara@uni.it", "26-10-2017", null));
        c.insert(new Amici("Mauro", "Cresta", "0824987", "cresta@uni.it", "27-12-2017", null));
        c.insert(new Amici("Andrea", "Coluccio", "0824988", "coluccio@uni.it", "28-01-2017", null));


        List<Amici> list = c.select(null);
        for(int i = 0; i < list.size(); i++){
            System.out.println(list.get(i));
        }


        Amici toDelete = new Amici();
        toDelete.setNome("");
        toDelete.setCognome("");
        toDelete.setEmail("");
        toDelete.setTelefono("");
        toDelete.setIdAmici(7);

        c.delete(toDelete);

        list = c.select(null);

        for(int i = 0; i < list.size(); i++){
            System.out.println(list.get(i));
        }

    }

    @Override
    public List<Amici> select(Amici a) throws DAOException {

        if (a == null){
            a = new Amici("", "", "", "", "", null); // Cerca tutti gli elementi
        }

        ArrayList<Amici> lista = new ArrayList<>();
        try{

            if (a == null || a.getCognome() == null
                    || a.getNome() == null
                    || a.getEmail() == null
                    || a.getTelefono() == null){
                throw new DAOException("In select: any field can be null");
            }

            Statement st = DAOMySQLSettings.getStatement();

            String sql = "select * from amici where cognome like '";
            sql += a.getCognome() + "%' and nome like '" + a.getNome();
            sql += "%' and telefono like '" + a.getTelefono() + "%'";
            if (a.getCompleanno() != null){
                sql += " and compleanno like '" + a.getCompleanno() + "%'";
            }
            sql += " and email like '" + a.getEmail() + "%'";

            try{
                logger.info("SQL: " + sql);
            } catch(NullPointerException nullPointerException){
                logger.severe("SQL: " + sql);
            }
            ResultSet rs = st.executeQuery(sql);
            while(rs.next()){
                lista.add(new Amici(rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("compleanno"),
                        rs.getInt("idAmici")));
            }
            DAOMySQLSettings.closeStatement(st);

        } catch (SQLException sq){
            throw new DAOException("In select(): " + sq.getMessage());
        }
        return lista;
    }

    @Override
    public void delete(Amici a) throws DAOException {
        if (a == null || a.getIdAmici() == null){
            throw new DAOException("In delete: idAmici cannot be null");
        }
        String query = "DELETE FROM amici WHERE idAmici='" + a.getIdAmici() + "';";

        try{
          logger.info("SQL: " + query);
        } catch (NullPointerException nullPointerException){
          System.out.println("SQL: " + query);
        }

        executeUpdate(query);

    }


    @Override
    public void insert(Amici a) throws DAOException {


        verifyObject(a);


        String query = "INSERT INTO amici (nome, cognome, telefono, email, compleanno, idAmici) VALUES  ('" +
                a.getNome() + "', '" + a.getCognome() + "', '" +
                a.getTelefono() + "', '" + a.getEmail() + "', '" +
                a.getCompleanno() + "', NULL)";
        try {
          logger.info("SQL: " + query);
        } catch (NullPointerException nullPointerException){
          System.out.println("SQL: " + query);
        }
        executeUpdate(query);
    }


    @Override
    public void update(Amici a) throws DAOException {

        verifyObject(a);

        String query = "UPDATE amici SET nome = '" + a.getNome() + "', cognome = '" + a.getCognome() + "',  telefono = '" + a.getTelefono() + "', email = '" + a.getEmail() + "', compleanno = '" + a.getCompleanno() + "'";
        query = query + " WHERE idAmici = " + a.getIdAmici() + ";";
        logger.info("SQL: " + query);

        executeUpdate(query);

    }


    private void verifyObject(Amici a) throws DAOException {
      if (a == null || a.getCognome() == null
        || a.getNome() == null
        || a.getEmail() == null
        || a.getCompleanno() == null
        || a.getTelefono() == null){
        throw new DAOException("In select: any field can be null");
      }
    }

    private void executeUpdate(String query) throws DAOException{
      try {
        Statement st = DAOMySQLSettings.getStatement();
        int n = st.executeUpdate(query);

        DAOMySQLSettings.closeStatement(st);

      } catch (SQLException e) {
        throw new DAOException("In insert(): " + e.getMessage());
      }
    }


}
