package com.sky.mapper;

import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 添加员工
     * @param employee
     */
    @Insert("insert into employee(name, username, password, phone, sex, id_number, create_time, update_time, create_user, update_user, status) value (#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser}, #{status})")
    void insert(Employee employee);

    /**
     * 根据姓名查询员工
     *
     * @param name
     * @return
     */
    List<Employee> list(String name);

    /**
     * 启用指定id的员工账号
     * @param id
     */
    @Update("update employee set status = 1 where id = #{id}")
    void enable(String id);

    /**
     * 禁用指定id的员工账号
     * @param id
     */
    @Update("update employee set status = 0 where id = #{id}")
    void disable(String id);

    /**
     * 更新员工
     * @param employee
     */
    void update(Employee employee);

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @Select("select * from employee where id = #{id}")
    Employee selectById(Long id);

    /**
     * 编辑密码
     *
     * @param password
     * @param id
     */
    @Update("update employee set password = #{password} where id = #{id}")
    void updatePassword(String password, Long id);
}
