package com.goblin.aicodeclient.innerService;


import com.goblin.aicodecommon.exception.BusinessException;
import com.goblin.aicodecommon.exception.ErrorCode;
import com.goblin.aicodemodel.model.entity.User;
import com.goblin.aicodemodel.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.goblin.aicodecommon.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 内部使用的用户服务。
 */
public interface InnerUserService {

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    // 静态方法，避免跨服务调用
    static User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户信息
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 通过id列表查询
     * @param ids
     * @return
     */
    List<User> listByIds(Collection<? extends Serializable> ids);

    /**
     * 通过id查询
     * @param id
     * @return
     */
    User getById(Serializable id);

}
