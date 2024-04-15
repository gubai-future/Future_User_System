package com.mysystem.futuresystemhd.common;

import com.mysystem.futuresystemhd.exception.BusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "Result",description = "全局响应体")
public class Result<T> {

    /**
     * 状态码
     */
    @ApiModelProperty(value = "状态码")
    private Integer code;

    /**
     * 状态信息
     */
    @ApiModelProperty(value = "状态信息")
    private String msg;

    /**
     * 返回数据
     */
    @ApiModelProperty(value = "返回数据")
    private T data;

    /**
     * 详情
     */
    @ApiModelProperty(value = "详情")
    private String description;


    /**
     * 无参成功返回响应体
     * @return
     * @param <T>
     */
    @ApiOperation(value = "无参成功返回响应体")
    public  static <T> Result<T> success(){
        return new Result<>(200,"success",null,"");
    }

    /**
     * 成功返回响应体(数据)
     * @param data
     * @return
     * @param <T>
     */
    @ApiOperation("成功返回响应体(数据)")
    public static <T> Result<T> success(T data){
        return new Result<>(200,"success",data,"");
    }

    /**
     * 成功返回响应体(数据,详情)
     * @param data
     * @param description
     * @return
     * @param <T>
     */
    @ApiOperation("成功返回响应体(数据,详情)")
    public static <T> Result<T> success(T data,String description){
        return new Result<>(200,"success",data,description);
    }

    /**
     * 错误返回响应体(状态信息)
     * @param msg
     * @return
     */
    @ApiOperation("错误返回响应体(状态信息)")
    public static Result Error(String msg){
        return new Result(50001,msg,null,"");
    }

    /**
     * 错误返回响应体
     * @param errorCode
     * @return
     */
    public static Result Error(ErrorCode errorCode){
        return new Result(errorCode.getCode(),errorCode.getMsg(),null,errorCode.getDescription());
    }

    public static Result Error(Integer code,String msg,String description){
        return new Result(code,msg,null,description);
    }
}
