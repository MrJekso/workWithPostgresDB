import db.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class App {
    public static void main(String[] args) throws Exception {

            Configure configure = new Configure();

            DB db = Postgres.initPostgres(configure.getNameDB(), configure.getHost(),configure.getPort(), configure.getUser(), configure.getPassword());

            List<Students> students = new ArrayList<>();
            students.add(new Students("Дима",1234, 4321));
            students.add(new Students("Вова",1114, 5521));
            students.add(new Students("Лиза",4324, 5551));
            students.add(new Students("Саша",1111, 1111));
            students.add(new Students("Вадим",2222, 2222));

            students.forEach(i->db.addStudent(i));
            //Пробуем добавить ещё раз
            students.forEach(i->db.addStudent(i));

            //Добавляем оценки
            Random rd = new Random();
            students.forEach(i->db.addGrade(i, Subjects.GEOMETRY,rd.nextInt(6-2)+2));
            students.forEach(i->db.addGrade(i, Subjects.PHYSICS,rd.nextInt(6-2)+2));
            students.forEach(i->db.addGrade(i, Subjects.BIOLOGY,rd.nextInt(6-2)+2));
            students.forEach(i->db.addGrade(i, Subjects.ALGEBRA,rd.nextInt(6-2)+2));

            //students.forEach(i->db.addGrade(i, Subjects.GEOMETRY,1)); // Выдаст ошибку из за того что оценка вне диапозона 2-5

            //Удаляем студента
            db.deleteStudent(new Students("Вова",1114, 5521));

            //Студенты преуспевающие по предмету Алгебра
            db.getAverageScoreSubject(Subjects.ALGEBRA);

            //Топ 3 предмета по успеваемости
            db.showTopSubjects();

            //Студенты сдавшие предмет Геометрия
            db.getListStudentsSurrenderingItem(Subjects.GEOMETRY);

            //Закрываем соединение
            db.closeDB();

    }
}
