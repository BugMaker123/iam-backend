package com.gk.iam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gk.iam.entity.IamLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface IamLoginLogMapper extends BaseMapper<IamLoginLog> {

    @Select("SELECT DATE_FORMAT(login_time, '%Y-%m-%d') as day_str, " +
            "SUM(CASE WHEN login_status = 1 THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN login_status = 0 THEN 1 ELSE 0 END) as fail_count " +
            "FROM iam_login_log " +
            "WHERE login_time >= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 7 DAY) " +
            "GROUP BY DATE_FORMAT(login_time, '%Y-%m-%d') " +
            "ORDER BY day_str ASC")
    List<Map<String, Object>> getLoginTrendsLast7Days();
}

