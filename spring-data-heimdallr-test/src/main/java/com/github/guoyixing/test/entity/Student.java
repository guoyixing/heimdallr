package com.github.guoyixing.test.entity;

import com.github.guoyixing.heimdallr.common.enums.SecurityLevelEnum;
import com.github.guoyixing.heimdallr.support.SecurityLevel;

import javax.persistence.*;

@Entity
@Table(name = "test_student")
public class Student {

    @Id
    public Long id;

    @Column(name = "stu_name")
    public String name;

    @Column(name = "stu_sex")
    public String sex;

    @SecurityLevel
    @Column(name = "security_level")
    @Enumerated(EnumType.STRING)
    public SecurityLevelEnum securityLevel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public SecurityLevelEnum getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(SecurityLevelEnum securityLevel) {
        this.securityLevel = securityLevel;
    }
}
