package cn.crisp.crispmaintenanceorder.service;

import cn.crisp.crispmaintenanceorder.dto.*;
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
     * 根据维修工程师的位置获取待处理列表，从近到远
     * @param dispatchDto
     * @return
     */
    PagingVo<Indent> listUnprocessed(DispatchDto dispatchDto);

    /**
     * 根据 id 获取订单信息
     * @param id
     * @return
     */
    Indent getById(Long id);

    /**
     * 故障报修
     * @param indentDto
     * @return
     */
    Indent applyForFault(IndentDto indentDto, HttpServletRequest request);

    /**
     * 接单
     * @param request
     * @param receiveDto 接单所需要的参数
     * @return
     */
    boolean receiveIndent(HttpServletRequest request, ReceiveDto receiveDto);

    /**
     * 确认维修成功
     * @param request
     * @param repairDto
     * @return
     */
    boolean repair(HttpServletRequest request, RepairDto repairDto);

    /**
     * 评价
     * @param request
     * @param evaluateDto
     * @return
     */
    boolean evaluate(HttpServletRequest request, EvaluateDto evaluateDto);
}