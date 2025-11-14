package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.pojo.ExpertAnswer;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.vo.AnswerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ExpertAnswerMapper extends BaseMapper<ExpertAnswer> {

    /**
    * 5. 自定义分页关联查询方法
     *
     * @param page       
     * @param questionId 问题ID
     * @return 分页后的VO列表
     */
    @Select("SELECT " +
            "    a.answer_id, a.question_id, a.responder_id, a.content, a.is_accepted, " +
            "    a.create_time, a.update_time, u.name AS responder_name " +
            "FROM " +
            "    tb_expert_answers a " +
            "LEFT JOIN " +
            "    users u ON a.responder_id = u.user_id " +
            "WHERE " +
            "    a.question_id = #{questionId} " +
            "ORDER BY " +
            "    a.create_time ASC")
    Page<AnswerVO> findAnswersWithUserNameByQuestionId(Page<AnswerVO> page, @Param("questionId") Integer questionId);

} 