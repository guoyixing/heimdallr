package com.github.guoyixing.test.dao;

import com.github.guoyixing.test.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author 敲代码的旺财
 * @date 2022/4/18 15:34
 */
@Repository
public interface StudentDao extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    @Query("from Student where id =:id")
    Student getByIdQueryJpql(@Param("id") Long id);

    @Query(value = "select * from test_student where id =:id", nativeQuery = true)
    Student getByIdQueryNative(@Param("id") Long id);

    List<Student> getBySex(String sex);

    List<Student> getBySexIn(List<String> sex);

    List<Student> getBySecurityLevel(Object securityLevel);
}
