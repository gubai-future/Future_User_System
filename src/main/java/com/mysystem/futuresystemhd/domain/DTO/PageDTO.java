package com.mysystem.futuresystemhd.domain.DTO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mysystem.futuresystemhd.constant.SortConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "分页请求体")
public class PageDTO implements Serializable {


    @TableField(exist = false)
    private static final long serialVersionUID = -3742395754886361496L;

    /**
     * 当前页码
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "当前页码(默认第一页)")
    private Long current = 1L;


    /**
     * 当前页显示数据条数
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "页码数据条数(默认10条)")
    private Long pageSize = 10L;

    /**
     * 排序字段
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "排序字段")
    private String fieldName;


    /**
     * 排序方式（默认升序）
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "排序方式（默认升序）asc升序 或者 desc降序")
    private String sort = SortConstant.SORT_ASC;

}
