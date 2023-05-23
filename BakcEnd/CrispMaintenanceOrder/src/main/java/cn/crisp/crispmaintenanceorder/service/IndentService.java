package cn.crisp.crispmaintenanceorder.service;

import cn.crisp.crispmaintenanceorder.dto.IndentDto;
import cn.crisp.crispmaintenanceorder.dto.QueryDto;
import cn.crisp.crispmaintenanceorder.vo.PagingVo;
import cn.crisp.entity.Indent;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@SuppressWarnings({"all"})
public interface IndentService extends IService<Indent> {
    /**
     * 获取订单列表
     * @param condition 查询条件
     * @return
     */
    List<Indent> list(Indent condition);

    /**
     * 分页获取订单列表
     * @param queryDto
     * @return
     */
    PagingVo<Indent> listPage(QueryDto<Indent> queryDto);

    /**
     * 故障报修
     * @param indentDto
     * @return
     */
    Indent applyForFault(IndentDto indentDto, HttpServletRequest request);
}