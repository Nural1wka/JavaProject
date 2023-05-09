package project.files.database;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import project.files.customer.Product;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static project.files.Helper.cypher;
import static project.files.database.DbConst.*;

public class DbHandler {
    static Connection dbConnection;

    public static Connection getDbConnection() throws SQLException {
        String connectionString = "jdbc:postgresql://" + DbConnectInfo.DB_HOST + ":"
                + DbConnectInfo.DB_PORT + "/" + DbConnectInfo.DB_NAME;

        // Class.forName("com.mysql.cj.jdbc.Driver");

        dbConnection = DriverManager.getConnection(connectionString, DbConnectInfo.DB_USER, DbConnectInfo.DB_PASS);

        return dbConnection;
    }

    public static void addCustomer(String login, String password, Double balance, String firstName, String lastName) {
        String insertQuery =
                "INSERT INTO " + CUSTOMER_TABLENAME + " ("
                        + CUSTOMER_LOGIN + ", "
                        + CUSTOMER_PASSWORD + ", "
                        + CUSTOMER_BALANCE + ", "
                        + CUSTOMER_FIRSTNAME + ", "
                        + CUSTOMER_LASTNAME + ") "
                        + "VALUES (?, ?, ?, ?, ?);";


        try (PreparedStatement prSt = getDbConnection().prepareStatement(insertQuery)) {
            int indCounter = 1;
            prSt.setString(indCounter++, login);
            prSt.setString(indCounter++, cypher(password));
            prSt.setDouble(indCounter++, balance);
            prSt.setString(indCounter++, firstName);
            prSt.setString(indCounter++, lastName);

            prSt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            System.out.println("Values into " + CUSTOMER_TABLENAME + " successfully added!");
        }

    }


    public static String getCustomerColumn(String login, String column) {
        String result = "";
        String selectFirstnameQuery =
                "SELECT " + column.strip() + " FROM " + CUSTOMER_TABLENAME +
                        " WHERE " + "login = '" + login.strip() + "';";

        try (PreparedStatement prSt = getDbConnection().prepareStatement(selectFirstnameQuery)) {
            ResultSet rs = prSt.executeQuery();

            rs.next();
            result = rs.getString(column);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public static Product getProductById(Integer id) throws SQLException {
//        Product prod = new Product();
        String selectQuery = "SELECT " + PRODUCT_ID + ", " + PRODUCT_TITLE + ", " + PRODUCT_PRICE + " " +
                " FROM " + PRODUCT_TABLENAME + " " +
                " WHERE " + PRODUCT_ID + " = " + id.toString() + ";";



        try (PreparedStatement prSt = getDbConnection().prepareStatement(selectQuery)) {
            ResultSet rs = prSt.executeQuery();
            while (rs.next()) {
                Product prod = new Product(rs.getInt(PRODUCT_ID), rs.getString(PRODUCT_TITLE), rs.getDouble(PRODUCT_PRICE));
//                prod.setId(rs.getInt(PRODUCT_ID));
//                prod.setTitle(rs.getString(PRODUCT_TITLE));
//                prod.setPrice(rs.getDouble(PRODUCT_PRICE));

                return prod;
            }
        }
        return new Product();
    }

    public static List<Product> getAllProductList() throws SQLException {
        List<Product> resultList = new ArrayList<>();
        for (int i = 1; i <= maxId(PRODUCT_TABLENAME, PRODUCT_ID); i++) {
            resultList.add(getProductById(i));
        }

        return resultList;
    }

//    public static ObservableList<Product> getAllProductList() throws SQLException {
//        ObservableList<Product> resultList = FXCollections.observableArrayList();
//        for (int i = 1; i <= maxId(PRODUCT_TABLENAME, PRODUCT_ID); i++) {
//            resultList.add(getProductById(i));
//        }
//
//        return resultList;
//    }

    public static int maxId(String tableName, String idName) throws SQLException {
        int res = -1;

        String selectQuery = "SELECT MAX(" + idName + ") FROM " + tableName + ";";
        PreparedStatement prSt = getDbConnection().prepareStatement(selectQuery);

        ResultSet rs = prSt.executeQuery();

        while (rs.next()) {
            res = rs.getInt("max");
        }

        return res;
    }



}




/*
CREATE TABLE Customers (
	customer_id SERIAL PRIMARY KEY,
	login TEXT NOT NULL,
	password TEXT NOT NULL,
	balance DECIMAL(20, 2) DEFAULT 0.0,
	first_name TEXT NOT NULL,
	last_name TEXT NOT NULL
);

CREATE TABLE Products (
	product_id SERIAL PRIMARY KEY,
	title TEXT NOT NULL UNIQUE,
	price DECIMAL(20, 2) NOT NULL
);

CREATE TABLE Orders (
	order_id SERIAL PRIMARY KEY,
	customer_id INT NOT NULL,
	product_id INT NOT NULL,
	is_bought BOOLEAN NOT NULL,
	FOREIGN KEY (customer_id) REFERENCES Customers (customer_id),
	FOREIGN KEY (product_id) REFERENCES Products (product_id)
);
*/