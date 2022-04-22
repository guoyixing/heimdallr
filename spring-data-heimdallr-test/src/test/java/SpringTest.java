import io.github.guoyixing.Application;
import io.github.guoyixing.heimdallr.common.enums.SecurityLevelEnum;
import io.github.guoyixing.heimdallr.support.SecurityLevelField;
import io.github.guoyixing.heimdallr.support.SecurityLevelManager;
import io.github.guoyixing.test.dao.StudentDao;
import io.github.guoyixing.test.entity.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author 敲代码的旺财
 * @date 2022/4/18 18:37
 */
@SpringBootTest(classes = Application.class)
public class SpringTest {

    @Autowired
    private StudentDao studentDao;

    @Test
    public void queryJpql() {
        Student student = studentDao.getByIdQueryJpql(1L);
        System.out.println(student);
    }

    @Test
    public void queryNative() {
        Student student = studentDao.getByIdQueryNative(1L);
        System.out.println(student);
    }

    @Test
    public void getBySex() {
        List<Student> student = studentDao.getBySex("男");
        System.out.println(student);
    }

    @Test
    public void getBySexIn() {
        List<Student> student = studentDao.getBySexIn(Arrays.asList("男"));
        System.out.println(student);
    }

    @Test
    public void getBySecurityLevel() {
        List<Student> student = studentDao.getBySecurityLevel(SecurityLevelEnum.NON_SECRET);
        System.out.println(student);
    }

    @Test
    public void queryJpaRepository() {
        Optional<Student> student = studentDao.findById(1L);
        SecurityLevelField field = SecurityLevelManager.getInstance().get(Student.class);
        System.out.println(student);
    }

    @Test
    public void queryListJpaRepository() {
        SecurityLevelManager.getInstance().currentSecurityLevel(SecurityLevelEnum.CONFIDENTIAL);
        List<Student> all = studentDao.findAll();
        System.out.println(all);
    }

    @Test
    public void queryByIdsJpaRepository() {
        List<Student> all = studentDao.findAllById(Arrays.asList(1L, 2L));
        System.out.println(all);
    }

    @Test
    public void querySpecJpaRepository() {
        Specification<Student> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("sex").as(String.class), "男"));
            Predicate[] predicate = predicates.toArray(new Predicate[0]);
            return criteriaBuilder.and(predicate);
        };
        Student student = studentDao.findOne(spec).get();
        System.out.println(student);
    }

    @Test
    public void queryExampleJpaRepository() {
        SecurityLevelManager.getInstance().offSecurityLevel();
        Student student = new Student();
        student.setSex("男");
        Example<Student> example = Example.of(student);
        List<Student> all = studentDao.findAll(example);
        System.out.println(all);
    }

    @Test
    public void existsById() {
        boolean flag = studentDao.existsById(1L);
        System.out.println(flag);
    }


}
