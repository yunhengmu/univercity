package com.ruoyi.system.domain.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.system.mapper.UpLogMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 上架记录实体类
 */
@Data                     // 生成 getter/setter、toString、equals、hashCode
@NoArgsConstructor        // 生成无参构造
@AllArgsConstructor       // 生成全参构造
@TableName("up_log")      // 指定数据库表名(MyBatis-Plus 注解)
public class UpLog {

    @TableId(value = "id", type = IdType.AUTO)  // 主键自增
    private Integer id;          // 上架书目主键

    private Integer bookId;      // 书目 id（对应 book_id）

    private Integer userId;      // 用户 id（对应 user_id）

    private Integer status;      // 上架状态（0-未上架 / 1-已上架）

    private LocalDateTime createTime;  // 上架时间（对应 create_time）

    private LocalDateTime updateTime;  // 更新时间（对应 update_time）
}