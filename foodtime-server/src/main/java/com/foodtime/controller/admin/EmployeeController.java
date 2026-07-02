package com.foodtime.controller.admin;

import com.foodtime.constant.JwtClaimsConstant;
import com.foodtime.dto.EmployeeDTO;
import com.foodtime.dto.EmployeeLoginDTO;
import com.foodtime.dto.EmployeePageQueryDTO;
import com.foodtime.entity.Employee;
import com.foodtime.properties.JwtProperties;
import com.foodtime.result.PageResult;
import com.foodtime.result.Result;
import com.foodtime.service.EmployeeService;
import com.foodtime.utils.JwtUtil;
import com.foodtime.vo.EmployeeLoginVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@ApiOperation(value = "员工管理")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO.getUsername());

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation(value = "员工退出")
    public Result<String> logout() {
        return Result.success();
    }
@PostMapping
@ApiOperation(value = "添加员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
log.info("新增员工：{}", employeeDTO.getUsername());
    System.out.println("当前线程的id："+ Thread.currentThread().getId());

employeeService.save(employeeDTO);
return  Result.success();
    }

    @GetMapping("/page")
    @ApiOperation(value = "员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询{}", employeePageQueryDTO);
        PageResult pageResult =employeeService.page(employeePageQueryDTO);
        return Result.success(pageResult);
    }
    @PostMapping("/status/{status}")
    @ApiOperation(value = "员工状态禁用/启用")
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("员工状态禁用/启用：{}", id);
        employeeService.startOrStop(status, id);
        return Result.success();
    }
/*
* 根据id查询员工*/
    @GetMapping({"/{id}"})
    @ApiOperation(value = "根据员工查询id")
    public Result<Employee> getById(@PathVariable Long id) {
        log.info("根据员工id查找{}", id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }
    @PutMapping
    @ApiOperation(value = "员工信息修改")
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("员工信息修改：{}", employeeDTO.getId());
        employeeService.update(employeeDTO);
        return Result.success();
         }
}
