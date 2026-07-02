package com.foodtime.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.foodtime.constant.MessageConstant;
import com.foodtime.constant.PasswordConstant;
import com.foodtime.constant.StatusConstant;
import com.foodtime.dto.EmployeeDTO;
import com.foodtime.dto.EmployeeLoginDTO;
import com.foodtime.dto.EmployeePageQueryDTO;
import com.foodtime.entity.Employee;
import com.foodtime.exception.AccountLockedException;
import com.foodtime.exception.AccountNotFoundException;
import com.foodtime.exception.PasswordErrorException;
import com.foodtime.mapper.EmployeeMapper;
import com.foodtime.result.PageResult;
import com.foodtime.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(passwordEncoder.encode(PasswordConstant.DEFAULT_PASSWORD));

        employeeMapper.insert(employee);
    }

    /**
     * 员工登录
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        Employee employee = employeeMapper.getByUsername(username);

        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        String storedPassword = employee.getPassword();

        // BCrypt密码比对
        if (passwordEncoder.matches(password, storedPassword)) {
            if (employee.getStatus() == StatusConstant.DISABLE) {
                throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
            }
            return employee;
        }

        // 兼容旧MD5密码：验证通过后自动升级为BCrypt
        if (storedPassword != null && storedPassword.length() == 32) {
            String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
            if (md5Password.equals(storedPassword)) {
                log.info("旧MD5密码验证通过，正在升级为BCrypt：{}", username);
                employee.setPassword(passwordEncoder.encode(password));
                employeeMapper.update(employee);

                if (employee.getStatus() == StatusConstant.DISABLE) {
                    throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
                }
                return employee;
            }
        }

        throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
    }

    @Override
    public PageResult page(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        long total = page.getTotal();
        List<Employee> records = page.getResult();
        return new PageResult(total, records);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();
        employeeMapper.update(employee);
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        employee.setPassword("*****");
        return employee;
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employeeMapper.update(employee);
    }

}
