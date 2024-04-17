package com.example.workflow.controller;


import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.example.workflow.common.R;
import com.example.workflow.dto.DeptFormDto;
import com.example.workflow.entity.Dept;
import com.example.workflow.service.DeptService;
import com.example.workflow.vo.DeptVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 部门 前端控制器
 * </p>
 *
 * @author 黄历
 * @since 2024-03-12
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/dept")
public class DeptController {

    private final DeptService deptService;

    /**
     * 无条件返回全部部门*/
    @GetMapping("/all")
    public R all() {
        ArrayList<DeptVo> deptVos = new ArrayList<>();
        deptService.list().stream().filter(Objects::nonNull).forEach(dept -> {
            DeptVo deptVo = new DeptVo();
            Long deptId = dept.getId();
            String deptName = dept.getDeptName();
            deptVo.setId(deptId);
            deptVo.setDeptName(deptName);
            deptVos.add(deptVo);
        });
        return R.success(deptVos);
    }

    /** 获取该部门的全部岗位*/
    @GetMapping("/positions/{id}")
    public R positions(@PathVariable Long id) {
        if (id  == null) {
            return R.error("id不能为空");
        }
        return deptService.getAllPositionsById(id);
    }


    /**
     * 获取所有二级ceo、所有三级ceo、所有四级ceo的列表的接
     * id 部门级别口*/
    @GetMapping("/ceo/{id}")
    public R ceo(@PathVariable Integer id) {
        return deptService.getAllCeo(id);
    }

    /** 查询出这个岗位的全部上级
     * id  岗位id
     */
    @GetMapping("/tree/{id}")
    public R tree(@PathVariable Long id) {
        if (id == null) {
            return R.error("id 不能为空");
        }
        return deptService.getAllSuperiorDept(id);
    }


    @GetMapping("/list/{id}")
    public R<List<DeptVo>> getDeptTree(@PathVariable Integer id) {
        return R.success(deptService.getDeptTree(id));
    }

    /**
     * 添加部门
     */
    @PostMapping("/add")
    public R addDept(@RequestBody DeptFormDto deptFormDto) {
        // 不允许超过五级
        if (deptFormDto.getParentLevel() >= 4) {
            return R.error("不能新增五级部门");
        }

        Boolean bool = deptService.addDept(deptFormDto);

        if (bool) {
            return R.success();
        }
        else  {
            return  R.error("不能创建同名的部门信息");
        }
    }


    /** 更新部门*/
    @PutMapping("/update/{id}")
    public R updateDept(@RequestBody DeptFormDto deptFormDto, @PathVariable Long id) {
        if (deptFormDto == null || id == null) {
            return R.error("参数不能为空");
        }
        Boolean bool = deptService.updateDept(deptFormDto, id);

        if (bool) {
            return R.success();
        } else {
            return R.error("更新错误，不能跨级别更新");
        }
    }
    /** 删除部门*/
    @DeleteMapping("/delete/{ids}")
    public R deleteDept(@PathVariable List<Long> ids) {
        if (ids == null) {
            R.error("id 不能为空");
        }

        Boolean deleted = deptService.deleteDept(ids);
        if (deleted) {
            return  R.success();
        } else  {
            return R.error("有下级部门或该部门有员工删除失败");
        }
    }

    @GetMapping("/info/{id}")
    public R<DeptFormDto> getDeptInfo(@PathVariable Long id) {

        DeptFormDto deptFormDto = deptService.getDeptInfo(id);

        return R.success(deptFormDto);
    }

    @GetMapping("/validate")
    public R validate(String deptName,Integer parentLevel,Long id) throws UnsupportedEncodingException {
        String decodedStr = URLDecoder.decode(deptName, "UTF-8");
        if (parentLevel != null) parentLevel += 1;
        Dept dept = Db.lambdaQuery(Dept.class)
                .eq(parentLevel != null, Dept::getLevel,parentLevel)
                .eq(deptName != null, Dept::getDeptName,decodedStr)
                .ne(id != null, Dept::getId,id)
                .one();
        if (dept != null) return R.error("部门名称重复");
        return R.success();
    }
}
