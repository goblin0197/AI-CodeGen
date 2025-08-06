package com.goblin.aicodergenerater.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.goblin.aicodergenerater.ai.core.AiCodeGeneratorFacade;
import com.goblin.aicodergenerater.ai.enums.CodeGenTypeEnum;
import com.goblin.aicodergenerater.constant.AppConstant;
import com.goblin.aicodergenerater.exception.BusinessException;
import com.goblin.aicodergenerater.exception.ErrorCode;
import com.goblin.aicodergenerater.exception.ThrowUtils;
import com.goblin.aicodergenerater.model.dto.AppQueryRequest;
import com.goblin.aicodergenerater.model.entity.App;
import com.goblin.aicodergenerater.model.entity.User;
import com.goblin.aicodergenerater.model.vo.AppVO;
import com.goblin.aicodergenerater.model.vo.UserVO;
import com.goblin.aicodergenerater.service.AppService;
import com.goblin.aicodergenerater.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.goblin.aicodergenerater.mapper.AppMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null ,ErrorCode.PARAMS_ERROR, "请求参数为空");

        QueryWrapper queryWrapper = new QueryWrapper();
        // 根据id查询
        Long id = appQueryRequest.getId();
        if (id != null && id > 0) {
            queryWrapper.eq("id", id);
        }
        // 根据应用名称模糊查询
        String appName = appQueryRequest.getAppName();
        if (StrUtil.isNotBlank(appName)) {
            queryWrapper.like("appName", appName);
        }
        // 根据应用名称模糊查询
        String cover = appQueryRequest.getCover();
        if (StrUtil.isNotBlank(cover)) {
            queryWrapper.like("cover", cover);
        }
        String initPrompt = appQueryRequest.getInitPrompt();
        if (StrUtil.isNotBlank(cover)) {
            queryWrapper.like("initPrompt", initPrompt);
        }
        // 根据代码生成类型查询
        String codeGenType = appQueryRequest.getCodeGenType();
        if (StrUtil.isNotBlank(codeGenType)) {
            queryWrapper.eq("codeGenType", codeGenType);
        }
        // 根据部署标识查询
        String deployKey = appQueryRequest.getDeployKey();
        if (StrUtil.isNotBlank(deployKey)) {
            queryWrapper.eq("deployKey", deployKey);
        }
        // 根据优先级查询
        Integer priority = appQueryRequest.getPriority();
        if (priority != null) {
            queryWrapper.eq("priority", priority);
        }
        // 根据用户id查询
        Long userId = appQueryRequest.getUserId();
        if (userId != null && userId > 0) {
            queryWrapper.eq("userId", userId);
        }
        // 排序
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        queryWrapper.orderBy(sortField,"ascend".equals(sortOrder));
//        // 按优先级降序、创建时间降序排序
//        queryWrapper.orderBy("priority", false)
//                   .orderBy("createTime", false);
        return queryWrapper;
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "appId不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message) , ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()) ,ErrorCode.NO_AUTH_ERROR,"无权限访问该应用");
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        ThrowUtils.throwIf(codeGenTypeEnum == null ,ErrorCode.SYSTEM_ERROR ,"不支持的代码生成类型");
        // 5. 调用 AI 生成代码
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
    }

    /**
     * 部署应用
     * 支‍持重复部署。如果应用已经有 deployKey，就直接使用现有的；如果没有，就生成一个新的
     * @param appId
     * @param loginUser
     * @return
     */
    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用Id不能为空");
        ThrowUtils.throwIf(loginUser == null ,ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以部署
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()) ,ErrorCode.NO_AUTH_ERROR,"无权限部署该应用");
        // 4. 检查是否已有deployKey ， 没有则生成6位deployKey（大小写字母 + 数字）
        String deployKey = app.getDeployKey();
        if(StrUtil.isBlank(deployKey)){
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if(!sourceDir.exists() || !sourceDir.isDirectory()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        // 8. 更新应用的deployKey和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 9. 返回可访问的 URL
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }

    @Override
    public boolean isAppBelongToUser(Long appId, Long userId) {
        if (appId == null || appId <= 0 || userId == null || userId <= 0) {
            return false;
        }
        App app = this.getById(appId);
        return app != null && userId.equals(app.getUserId());
    }
}
