package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
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
@Slf4j
@Api(tags = "员工控制接口")
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
    @ApiOperation("员工登录请求")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

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
    @ApiOperation("员工登出请求")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增
     *
     * @param employeeDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增员工请求")
    public Result add(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工{}", employeeDTO);
        employeeService.add(employeeDTO);

        return Result.success();
    }

    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("员工分页查询请求")
    public Result<PageResult> query(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("查询信息为{}",employeePageQueryDTO);

        PageResult pageResult = employeeService.query(employeePageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 启用或禁用员工账号
     *
     * @param status
     * @return
     */
    @ApiOperation("启用/禁用员工账号")
    @PostMapping("/status/{status}")
    public Result adjustStatus(String id, @PathVariable String status) {
        log.info("要将状态设为{}", status);

        employeeService.adjustStatus(id, status);

        return Result.success();
    }

    /**
     * 根据ID查询员工信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询员工信息")
    public Result<Employee> queryById(@PathVariable String id) {
        log.info("需要查询的id为{}", id);

        Employee employee = employeeService.queryById(id);

        return Result.success(employee);
    }

    /**
     * 编辑员工
     *
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("编辑员工信息")
    public Result editEmployee(@RequestBody EmployeeDTO employeeDTO) {
        log.info("接受到的员工信息为{}", employeeDTO);
        log.info("接受到的员工ID为{}", employeeDTO.getId());

        employeeService.editEmployee(employeeDTO);

        return Result.success();
    }

    /**
     * 编辑密码
     *
     * @param passwordEditDTO
     * @return
     */
    @PutMapping("/editPssword")
    @ApiOperation("编辑密码")
    public Result editPassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        employeeService.editPassword(passwordEditDTO);

        return Result.success();
    }

}
