package com.foodtime.mapper;

import com.github.pagehelper.Page;
import com.foodtime.annotation.AutoFill;
import com.foodtime.dto.EmployeePageQueryDTO;
import com.foodtime.entity.Employee;
import com.foodtime.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);
    @AutoFill(value = OperationType.INSERT)
   @Insert("insert into employee (username, name, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
   "values "+" (#{username}, #{name}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Employee employee);

/*
*
* 分页查询方法*/
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);


    /*
    *
    * 启用和禁用操作*/
    @AutoFill(value = OperationType.UPDATE)
    void update(Employee employee);
    /*
    * 根据id查找信息*/
@Select("select * from employee where id = #{id}")
    Employee getById(Long id);
}
