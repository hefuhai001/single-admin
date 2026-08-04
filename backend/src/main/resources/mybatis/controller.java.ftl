package ${package.Controller};

import ${package.Entity}.${entity};
import ${package.Service}.${table.serviceName};
import com.hfh.api.base.BaseController;
import cn.dev33.satoken.util.SaResult;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

<#if restControllerStyle>
@RestController(value = "${table.entityPath}Controller")
<#else>
@Controller
</#if>
@RequestMapping("<#if package.ModuleName?? && package.ModuleName != "">/${package.ModuleName}</#if>/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen?replace('-entity','')}<#else>${table.entityPath}</#if>")
@RequiredArgsConstructor
@Tag(name = "${table.comment!}管理", description = "${table.comment!}增删改查接口")
public class ${table.controllerName} extends BaseController {

    private final ${table.serviceName} ${table.serviceName?uncap_first};

    /**
     * 根据ID查询${table.comment!}
     */
    @SaCheckPermission("${table.entityPath?replace('Entity','')}:query")
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询${table.comment!}")
    public SaResult get${table.entityPath?cap_first?replace('Entity','')}ById(@PathVariable("id") Long id) {
        ${entity} entity = ${table.serviceName?uncap_first}.getById(id);
        if (entity == null) {
            return SaResult.error("${table.comment!}不存在");
        }
        return SaResult.data(entity);
    }

    /**
     * 查询所有${table.comment!}
     */
    @SaCheckPermission("${table.entityPath?replace('Entity','')}:query")
    @GetMapping("/list")
    @Operation(summary = "查询所有${table.comment!}")
    public SaResult list${table.entityPath?cap_first?replace('Entity','')}() {
        QueryWrapper<${entity}> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        return SaResult.data(${table.serviceName?uncap_first}.list(wrapper));
    }

    /**
     * 分页查询${table.comment!}
     */
    @SaCheckPermission("${table.entityPath?replace('Entity','')}:query")
    @GetMapping("/page")
    @Operation(summary = "分页查询${table.comment!}")
    public SaResult page${table.entityPath?cap_first?replace('Entity','')}(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size<#list table.fields as field><#if field.propertyType == "String" || field.propertyType == "Long" || field.propertyType == "Integer">,
            @Parameter(description = "${field.comment!field.propertyName}") @RequestParam(required = false) ${field.propertyType} ${field.propertyName}</#if></#list>
    ) {
        Page<${entity}> page = new Page<>(current, size);
        QueryWrapper<${entity}> wrapper = new QueryWrapper<>();
        <#list table.fields as field>
        <#if field.propertyType == "String">
        wrapper.like(${field.propertyName} != null && !${field.propertyName}.isEmpty(), "${field.name}", ${field.propertyName});
        <#elseif field.propertyType == "Long" || field.propertyType == "Integer">
        wrapper.eq(${field.propertyName} != null, "${field.name}", ${field.propertyName});
        </#if>
        </#list>
        wrapper.orderByDesc("<#list table.fields as field><#if field.keyFlag>${field.name}</#if></#list>");
        return SaResult.data(${table.serviceName?uncap_first}.page(page, wrapper));
    }

    /**
     * 新增${table.comment!}
     */
    @SaCheckPermission("${table.entityPath?replace('Entity','')}:add")
    @PostMapping
    @Operation(summary = "新增${table.comment!}")
    public SaResult save${table.entityPath?cap_first?replace('Entity','')}(@RequestBody ${entity} entity) {
        boolean success = ${table.serviceName?uncap_first}.save(entity);
        return success ? SaResult.ok("新增成功") : SaResult.error("新增失败");
    }

    /**
     * 更新${table.comment!}
     */
    @SaCheckPermission("${table.entityPath?replace('Entity','')}:update")
    @PutMapping
    @Operation(summary = "更新${table.comment!}")
    public SaResult update${table.entityPath?cap_first?replace('Entity','')}(@RequestBody ${entity} entity) {
        boolean success = ${table.serviceName?uncap_first}.updateById(entity);
        return success ? SaResult.ok("更新成功") : SaResult.error("更新失败");
    }

    /**
     * 删除${table.comment!}
     */
    @SaCheckPermission("${table.entityPath?replace('Entity','')}:delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除${table.comment!}")
    public SaResult delete${table.entityPath?cap_first?replace('Entity','')}(@PathVariable("id") Long id) {
        boolean success = ${table.serviceName?uncap_first}.removeById(id);
        return success ? SaResult.ok("删除成功") : SaResult.error("删除失败");
    }

    /**
     * 批量删除${table.comment!}
     */
    @SaCheckPermission("${table.entityPath?replace('Entity','')}:delete")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除${table.comment!}")
    public SaResult delete${table.entityPath?cap_first?replace('Entity','')}Batch(@RequestBody java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return SaResult.error("请选择要删除的数据");
        }
        boolean success = ${table.serviceName?uncap_first}.removeByIds(ids);
        return success ? SaResult.ok("批量删除成功") : SaResult.error("批量删除失败");
    }
}
