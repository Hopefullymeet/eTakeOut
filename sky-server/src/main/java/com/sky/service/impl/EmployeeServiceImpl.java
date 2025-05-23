package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.BaseException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private HttpServletRequest httpServletRequest;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //将前端传来的密码进行加密，随后再比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void add(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        /*employee.setName(employeeDTO.getName());
        employee.setSex(employeeDTO.getSex());
        employee.setIdNumber(employeeDTO.getIdNumber());

        if(employeeDTO.getPhone().length() != 11) {
            throw new BaseException("手机号码定义错误");
        }
        employee.setPhone(employeeDTO.getPhone());*/

        BeanUtils.copyProperties(employeeDTO, employee);

        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        String jwt = httpServletRequest.getHeader(jwtProperties.getAdminTokenName());
        Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), jwt);

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setCreateUser(Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString()));
//        employee.setUpdateUser(Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString()));

//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult query(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        List<Employee> list = employeeMapper.list(employeePageQueryDTO.getName());
        Page<Employee> p = (Page<Employee>) list;

        PageResult pageResult = new PageResult(p.getTotal(), list);

        return pageResult;
    }

    /**
     * 启用/禁用员工账号
     *
     * @param id
     * @param status
     */
    @Override
    public void adjustStatus(String id, String status) {
        Employee employee = new Employee();

        employee.setId(Long.valueOf(id));
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        if(status.equals("1")) {
            employee.setStatus(1);
        } else if(status.equals("0")) {
            employee.setStatus(0);
        }

        employeeMapper.update(employee);
    }

    /**
     * 根据ID查询员工信息
     *
     * @param id
     * @return
     */
    @Override
    public Employee queryById(String id) {
        Employee employee = employeeMapper.selectById(Long.valueOf(id));

        return employee;
    }

    /**
     * 编辑员工
     *
     * @param employeeDTO
     */
    @Override
    public void editEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.update(employee);
    }

    /**
     * 编辑密码
     * @param passwordEditDTO
     */
    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        String originalPassword = employeeMapper.selectById(passwordEditDTO.getEmpId()).getPassword();

        if(DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes()).equals(originalPassword)) {
            Employee employee = new Employee();
            String newPassword = DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes());

//            employee.setPassword(newPassword);
//            employee.setId(passwordEditDTO.getEmpId());
//            employee.setUpdateTime(LocalDateTime.now());
//            employee.setUpdateUser(BaseContext.getCurrentId());

            employeeMapper.update(employee);
        } else {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
    }
}
