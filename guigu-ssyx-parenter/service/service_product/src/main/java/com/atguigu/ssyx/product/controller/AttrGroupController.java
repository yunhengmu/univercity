package com.atguigu.ssyx.product.controller;

import com.atguigu.ssyx.common.result.Result;
import com.atguigu.ssyx.model.product.AttrGroup;
import com.atguigu.ssyx.product.service.AttrGroupService;
import com.atguigu.ssyx.vo.product.AttrGroupQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: AttrGroupController
 * Package: com.atguigu.ssyx.product.controller
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/7/22 14:18
 * @Version 1.0
 */
@Api(tags = "平台属性分配接口")
@RestController
@RequestMapping("/admin/product/attrGroup/")
@CrossOrigin
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;

    @ApiOperation("得到菜单列表")
    @GetMapping("{page}/{limit}")
    public Result getAttrList(@PathVariable("page") Long page,
                              @PathVariable("limit") Long limit,
                              AttrGroupQueryVo attrGroupQueryVo){
        Page attrPage = new Page(page,limit);
        IPage<AttrGroup> attrGroupIPage = attrGroupService.getAttrGroupList(attrPage,attrGroupQueryVo);
        return Result.ok(attrGroupIPage);

    }

    @ApiOperation("根据id得到值")
    @GetMapping("get/{id}")
    public Result getById(@PathVariable("id") Long id){
        AttrGroup attrGroup = attrGroupService.getById(id);
        return Result.ok(attrGroup);
    }

    @ApiOperation("保存值")
    @GetMapping("save")
    public Result save(@RequestBody AttrGroup attrGroup){
        attrGroupService.save(attrGroup);
        return Result.ok(null);
    }

    @ApiOperation("更改值")
    @PutMapping("update")
    public Result update(@RequestBody AttrGroup attrGroup){
        attrGroupService.updateById(attrGroup);
        return Result.ok(null);
    }

    @ApiOperation("删除值")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable("id") Long id){
        attrGroupService.removeById(id);
        return Result.ok(null);
    }

    @ApiOperation("批量删除值")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> ids){
        attrGroupService.removeByIds(ids);
        return Result.ok(null);
    }

    @ApiOperation("找到所有值")
    @GetMapping("findAllList")
    public Result findAllList(){
        List<AttrGroup> list = attrGroupService.list();
        return Result.ok(list);
    }
}
