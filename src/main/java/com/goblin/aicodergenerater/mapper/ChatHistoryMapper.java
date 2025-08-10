package com.goblin.aicodergenerater.mapper;

import com.mybatisflex.core.BaseMapper;
import com.goblin.aicodergenerater.model.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话历史 映射层。
 *
 * @author goblin
 */
@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

}
