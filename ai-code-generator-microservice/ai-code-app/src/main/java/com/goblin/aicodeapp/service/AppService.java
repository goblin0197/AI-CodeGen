package com.goblin.aicodeapp.service;


import com.goblin.aicodemodel.model.dto.AppAddRequest;
import com.goblin.aicodemodel.model.dto.AppQueryRequest;
import com.goblin.aicodemodel.model.entity.App;
import com.goblin.aicodemodel.model.entity.User;
import com.goblin.aicodemodel.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 */
public interface AppService extends IService<App> {

    /**
     * 获取脱敏后的应用信息
     *
     * @param app 应用信息
     * @return 脱敏后的应用信息
     */
    AppVO getAppVO(App app);

    /**
     * 获取脱敏后的应用信息（分页）
     *
     * @param appList 应用列表
     * @return 脱敏后的应用信息列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 根据查询条件构造数据查询参数
     *
     * @param appQueryRequest 应用查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 调用ai生成代码
     * @param appId
     * @param message
     * @param loginUser
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 将生成的应用部署
     * @param appId
     * @param loginUser
     * @return
     */
    String deployApp(Long appId, User loginUser);
    /**
     * 校验应用是否属于当前用户
     *
     * @param appId 应用id
     * @param userId 用户id
     * @return 是否属于
     */
    boolean isAppBelongToUser(Long appId, Long userId);

    /**
     * 异步生成应用截图并更新封面
     * @param appId
     * @param appUrl
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);

    Long createApp(AppAddRequest appAddRequest , User loginUser);
}
