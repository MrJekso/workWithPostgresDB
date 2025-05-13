 package db;

import org.postgresql.util.PSQLException;

import java.sql.*;
import java.util.Properties;

public class Postgres implements DB{
    private String nameDB;
    private String url;
    private Properties authorization;
    private Connection connection;
    private Statement statement;
    static public Postgres postgres;

    @Override
    public DB initDB(String nameDB, String host, String port, String user, String password) {
        return Postgres.initPostgres(nameDB, host, port, user,password);
    }
    @Override
    public void closeDB(){
        Postgres.closePostgres();
    }
    @Override
    public void addStudent(Students student) {
        String query = "SELECT EXISTS(SELECT name FROM students WHERE pasport_series = " + student.getPassportSeries() + " AND pasport_number = " + student.getPassportNumber() + ")";
        try (ResultSet table = statement.executeQuery(query)){
            table.first();
            if(table.getBoolean(1) == false) {
                query = "INSERT INTO students (name, pasport_series, pasport_number) values ('" + student.getName() + "'," + student.getPassportSeries() + "," + student.getPassportNumber() + ")";
                statement.executeQuery(query);
            }
        }catch (Exception e){
            if(!e.toString().equalsIgnoreCase("org.postgresql.util.PSQLException: Запрос не вернул результатов."))
                System.err.println(e);
        }
    }
    @Override
    public void deleteStudent(Students student){
        int studentId = -1;
        String query = "SELECT EXISTS(SELECT name FROM students WHERE pasport_series = " + student.getPassportSeries() + " AND pasport_number = " + student.getPassportNumber() + ")";
        try (ResultSet table = statement.executeQuery(query)){
            table.first();
            if(table.getBoolean(1)){
                query = "SELECT id FROM students WHERE pasport_series = " + student.getPassportSeries() + " AND pasport_number = " + student.getPassportNumber();
                ResultSet requestStudentId = statement.executeQuery(query);
                requestStudentId.first();
                studentId = requestStudentId.getInt("id");
                if(requestStudentId!=null){ requestStudentId.close();}

                query = "DELETE FROM students WHERE pasport_series = " + student.getPassportSeries() + " AND pasport_number = " + student.getPassportNumber();
                ResultSet  deleteStudentRequest = statement.executeQuery(query);
                deleteStudentRequest.first();
                if(deleteStudentRequest!=null){deleteStudentRequest.close();}
            }
        }catch (Exception e){
            if(!e.toString().equalsIgnoreCase("org.postgresql.util.PSQLException: Запрос не вернул результатов."))
                System.err.println(e);
        }
        if(studentId != -1) {
            query = "DELETE FROM progress WHERE name_students = " + studentId;
            try (ResultSet deleteProgressRequest = statement.executeQuery(query)) {
            } catch (Exception e) {
                if (!e.toString().equalsIgnoreCase("org.postgresql.util.PSQLException: Запрос не вернул результатов."))
                    System.err.println(e);
            }
        }
    }
    @Override
    public void addGrade(Students student, Subjects subject, int grade){
        String query = "SELECT EXISTS(SELECT name FROM students WHERE pasport_series = " + student.getPassportSeries() + " AND pasport_number = " + student.getPassportNumber() + ")";
        ResultSet studentRequestId;
        try (ResultSet table = statement.executeQuery(query)){
            table.first();
            if(table.getBoolean(1)){
                query = "SELECT id FROM students WHERE pasport_series = " + student.getPassportSeries() + " AND pasport_number = " + student.getPassportNumber();
                studentRequestId = statement.executeQuery(query);
                studentRequestId.first();
                int studentId = studentRequestId.getInt("id");

                query = "SELECT EXISTS (SELECT * FROM subjects WHERE name_subject = '" +  subject + "')";
                ResultSet subjectRequestId = statement.executeQuery(query);
                subjectRequestId.first();
                int subjectId = -1;
                if(subjectRequestId.getBoolean(1)){
                    query = "SELECT id FROM subjects WHERE name_subject = '" + subject + "'";
                    subjectRequestId = statement.executeQuery(query);
                    subjectRequestId.first();
                    subjectId = subjectRequestId.getInt("id");
                }

                if(subjectId != -1){
                    query = "INSERT INTO progress (name_students,subject_name,grade) VALUES (" + studentId + "," + subjectId + "," + grade + ")";
                    statement.executeQuery(query);
                }
                if(subjectRequestId != null){ studentRequestId.close();}
                if(studentRequestId != null){ studentRequestId.close();}
            }
        }catch (Exception e) {
            if (!e.toString().equalsIgnoreCase("org.postgresql.util.PSQLException: Запрос не вернул результатов.")){
                System.err.println(e);
            }
        }
    }
    @Override
    public void showTopSubjects() {
        String query = "SELECT subjects.name_subject \n" +
                "FROM subjects, students, progress \n" +
                "WHERE subjects.id = progress.subject_name \n" +
                "AND progress.name_students = students.id\n" +
                "AND grade >= 3 GROUP BY progress.grade, subjects.name_subject \n" +
                "ORDER BY COUNT(*) DESC LIMIT 3";
        try (ResultSet table = statement.executeQuery(query)){
            if(table.first()) {
                do {
                    System.out.println(table.getString("name_subject"));
                } while (table.next());
            }
        }catch (Exception e){
            if(!e.toString().equalsIgnoreCase("org.postgresql.util.PSQLException: Запрос не вернул результатов."))
                System.err.println(e);
        }
    }


    @Override
    public void getAverageScoreStudent(Students student) {
        String query = "SELECT students.name,subjects.name_subject, ROUND(AVG(progress.grade),0) as AverageScore\n" +
                "FROM subjects,progress, students\n" +
                "where subjects.id = progress.subject_name  \n" +
                "and progress.name_students = students.id  \n" +
                "and students.name = '" + student.getName() + "'" +
                "GROUP BY students.name, subjects.name_subject";
        try (ResultSet table = statement.executeQuery(query)){
            System.out.println("Average score : " + student.getName());
            if(table.first()) {
                do {
                    System.out.printf("%s:\t%s \n", table.getString("name_subject"), table.getString("averagescore"));
                } while (table.next());
            }
        }catch (Exception e){
            if(!e.toString().equalsIgnoreCase("org.postgresql.util.PSQLException: Запрос не вернул результатов."))
                System.err.println(e);
        }
    }

    @Override
    public void getAverageScoreSubject(Subjects subject) {
        String query = "SELECT subjects.name_subject ,ROUND(AVG(progress.grade),0) as AverageScore\n" +
                "FROM students, progress, subjects \n" +
                "WHERE subjects.id = progress.subject_name  \n" +
                "AND progress.name_students = students.id  \n" +
                "AND subjects.name_subject = '" + subject + "' \n" +
                "GROUP BY subjects.name_subject ORDER BY AverageScore";
        try (ResultSet table = statement.executeQuery(query)){
            if(table.first()) {
                do {
                    System.out.printf("Average score %s:\t%s \n", table.getString("name_subject"), table.getString("averagescore"));
                } while (table.next());
            }
        }catch (Exception e){
            if(!e.toString().equalsIgnoreCase("org.postgresql.util.PSQLException: Запрос не вернул результатов."))
                System.err.println(e);
        }
    }

    @Override
    public void getListStudentsSurrenderingItem(Subjects subject) {
        String query = "SELECT students.name, ROUND(AVG(progress.grade)) as AverageScore\n" +
                "FROM subjects, progress, students\n" +
                "WHERE subjects.id = progress.subject_name\n" +
                "AND progress.name_students = students.id\n" +
                "AND subjects.name_subject = '" + subject + "'\n" +
                "AND subjects.id = progress.subject_name\n" +
                "GROUP BY students.name \n" +
                "HAVING ROUND(AVG(progress.grade)) >= 3\n" +
                "ORDER BY AverageScore;";
        try (ResultSet table = statement.executeQuery(query)){
            System.out.println("Surrendering item: " + subject);
            if(table.first()) {
                do {
                    System.out.printf("%s:\t%s \n", table.getString("name"), table.getString("averagescore"));
                } while (table.next());
            }
        }catch (Exception e){
            if(!e.toString().equalsIgnoreCase("org.postgresql.util.PSQLException: Запрос не вернул результатов."))
                System.err.println(e);
        }
    }

    static public Postgres initPostgres(String nameDB, String host, String port,String user, String password){
        if(Postgres.postgres == null){
            Postgres.postgres = new Postgres(nameDB, host, port, user,password);
        }
        return Postgres.postgres;
    }

    static public void closePostgres() {
        try {
            if (Postgres.postgres.statement != null) {
                Postgres.postgres.statement.close();
            }
            if (Postgres.postgres.connection != null) {
                Postgres.postgres.connection.close();
            }
            Postgres.postgres = null;
        }catch (Exception e){
            System.err.println(e);
        }
    }

    private Postgres(String nameDB, String host, String port, String user, String password) {
        try {
            this.nameDB = nameDB;
            Class.forName("org.postgresql.Driver");
            this.url = "jdbc:postgresql://" + host + ":" + port + "/" + nameDB;
            this.authorization = new Properties();
            authorization.put("user", user);
            authorization.put("password", password);
            this.connection = DriverManager.getConnection(url, authorization);
            this.statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            ResultSet table = statement.executeQuery("SELECT EXISTS ( SELECT 1 FROM pg_tables WHERE schemaname = 'public' AND tablename = 'students')");
            table.first();
            if(table.getBoolean(1) == false){
                try {
                    statement.executeQuery("CREATE TABLE students (id serial, name text, pasport_series integer, pasport_number integer, primary key(id))");
                }catch (PSQLException e){
                    if(e.toString().equalsIgnoreCase("org.postgresql.util.PSQLException: Запрос не вернул результатов.")){
                        System.out.println("create students");
                    }else{
                        System.err.println(e);
                    }
                }
            }

            table = statement.executeQuery("SELECT EXISTS ( SELECT 1 FROM pg_tables WHERE schemaname = 'public' AND tablename = 'subjects')");
            table.first();
            if(table.getBoolean(1) == false){
                try {
                    statement.executeQuery("CREATE TABLE subjects (id serial, name_subject text, primary key(id))");
                }catch (PSQLException e){
                    if(e.toString().equalsIgnoreCase("org.postgresql.util.PSQLException: Запрос не вернул результатов.")){
                        System.out.println("create subjects");
                    }else{
                        System.err.println(e);
                    }
                }

                StringBuilder subjects = new StringBuilder();
                for(Subjects s : Subjects.values()){
                    subjects.append("('" + String.valueOf(s) +"'),");
                }

                try {
                    statement.executeQuery("INSERT INTO  subjects (name_subject) VALUES "+ subjects.toString().substring(0, subjects.length()-1) +"");
                }catch (PSQLException e){
                    if(!e.toString().equalsIgnoreCase("org.postgresql.util.PSQLException: Запрос не вернул результатов.")){
                        System.err.println(e);
                    }
                }
            }

            table = statement.executeQuery("SELECT EXISTS ( SELECT 1 FROM pg_tables WHERE schemaname = 'public' AND tablename = 'progress')");
            table.first();
            if(table.getBoolean(1) == false){
                try {
                    statement.executeQuery("CREATE TABLE progress (id serial, name_students integer, subject_name integer, grade integer CHECK (grade>=2 AND grade<=5),primary key(id))");
                }catch (PSQLException e){
                    if(e.toString().equalsIgnoreCase("org.postgresql.util.PSQLException: Запрос не вернул результатов.")){
                        System.out.println("create progress");
                    }else{
                        System.err.println(e);
                    }
                }
            }

            if (table != null) {
                table.close();
            }

        } catch (ClassNotFoundException | SQLException e ) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.err.println("Error accessing database!");
            e.printStackTrace();
        }
    }
}
