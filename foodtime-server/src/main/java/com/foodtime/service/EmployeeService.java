package com.foodtime.service;

import com.foodtime.dto.EmployeeDTO;
import com.foodtime.dto.EmployeeLoginDTO;
import com.foodtime.dto.EmployeePageQueryDTO;
import com.foodtime.entity.Employee;
import com.foodtime.result.PageResult;

public interface EmployeeService {
//新增员工业务方法

   void save(EmployeeDTO employeeDTO) ;

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

/*
* 分页查询*/
    PageResult page(EmployeePageQueryDTO employeePageQueryDTO);

    void startOrStop(Integer status, Long id);



    Employee getById(Long id);

    void update(EmployeeDTO employeeDTO);
}
