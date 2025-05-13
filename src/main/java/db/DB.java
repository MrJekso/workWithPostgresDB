package db;

import java.util.List;

public interface DB {
    void closeDB();
    DB initDB(String nameDB, String host, String port, String user, String password);
    void addStudent(Students student);
    void deleteStudent(Students student);
    void addGrade(Students students,Subjects subjects,int grade);
    void showTopSubjects();
    void getAverageScoreStudent(Students student);
    void getAverageScoreSubject(Subjects subject);
    void getListStudentsSurrenderingItem(Subjects subject);
}
