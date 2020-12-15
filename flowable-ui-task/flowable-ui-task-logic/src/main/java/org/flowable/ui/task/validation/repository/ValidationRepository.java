package org.flowable.ui.task.validation.repository;



import org.flowable.ui.task.validation.model.Validation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ValidationRepository {

    public List<Validation> getRules() {
        Connection conn = null;
        List<Validation> validations = new ArrayList<Validation>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/flowable", "root", "111111");
            Statement stat = conn.createStatement();
            ResultSet rst = stat.executeQuery("select * from act_proc_validation");
            while (rst.next()) {
                Validation validation = new Validation();
                validation.setId(rst.getInt(1));
                validation.setType(rst.getInt(2));
                if(rst.getInt(2) == 1){  // 两个节点之间的约束
                    validation.setActAName(rst.getString(4));
                    validation.setActBName(rst.getString(5));
                } else {  // 关键节点
                    validation.setKeyActName(rst.getString(3));
                }
                validations.add(validation);
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return validations;
    }
}

