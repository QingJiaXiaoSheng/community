package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;


@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞

    /**
     * 点赞动作
     * 第一次点赞, 第二次取消赞
     * 根据userId是否在Redis中实体对应键的值集合中
     *
     * @param userId       点赞的用户
     * @param entityType   被点赞的实体类型
     * @param entityId     被点赞的实体Id
     * @param entityUserId 被点赞的实体的作者Id, 重构代码时新增参数
     */
    public void like(int userId, int entityType, int entityId, int entityUserId) {
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//
//        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if (isMember) {
//            redisTemplate.opsForSet().remove(entityLikeKey, userId);
//        } else {
//            redisTemplate.opsForSet().add(entityLikeKey, userId);
//        }
        /*
        重构, 需要添加对某个用户赞的数量的修改, 会涉及redis事务管理, 代码差异大
         */
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                // 查询redis数据库, 要放在事务的过程之外, 因此事务是延迟统一提交
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                operations.multi();

                if (isMember) {
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }

                return operations.exec();
            }
        });
    }

    // 查询实体点赞的数量

    /**
     * 查询实体点赞的数量
     *
     * @param entityType 实体类型
     * @param entityId   实体Id
     * @return 返回实体的点赞数量
     */
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    // 查询某人对某实体的点赞状态

    /**
     * 查询某人对某实体的点赞状态
     *
     * @param userId     某人Id
     * @param entityType 某实体类型
     * @param entityId   某实体Id
     * @return 点赞状态, 1:点赞, 0: 未点赞
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }


    // 查询某个用户获得的赞

    /**
     * 查询某用户获得的点赞数
     *
     * @param userId    用户Id
     * @return  某用户获得的点赞数
     */
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }
}
