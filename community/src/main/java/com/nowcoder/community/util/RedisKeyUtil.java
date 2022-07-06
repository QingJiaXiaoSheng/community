package com.nowcoder.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";

    /**
     * 优化登录模块, 提高之前频繁使用的功能的效率和性能
     *
     * 1、使用Redis存储验证码
     *  - 验证码需要频繁的访问与刷新, 对性能要求较高
     *  - 验证码不需永久保存, 通常在很短的时间内就会失效
     *  - 分布式部署时, 存在session共享问题
     *  - 之前实现验证码登录, 存在多个用户同时登录, 前一个验证码失效问题.
     * 2、使用Redis存储登录凭证
     *  - 处理每次请求时, 都要查询用户的登录凭证, 访问的频率非常高, 之前使用的拦截器, 凭证存放在MySQL数据库, 效率低
     * 3、使用Redis缓存用户信息
     *  - 处理每次请求时, 都要根据凭证查询用户信息, 访问的频率非常高
     */

    private static final String PREFIX_KAPTCHA = "kaptcha";

    // 某个实体的赞
    // like:entity:entityType:entityId -> set(userId)

    /**
     * Redis数据库存储
     * 拼接后的key：like:entity:entityType:entityId
     * 值得类型选用set集合, set(userId), 可以解决查看谁点赞的问题等。
     *
     * @param entityType 待拼接的实体类型变量
     * @param entityId   待拼接的实体Id
     * @return 某个实体的赞的key
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    // like:user:userId -> int

    /**
     * Redis 数据存储
     * 重构点赞, 添加用户获得的赞, 提高效率
     *
     * @param userId    待拼接的用户Id
     * @return          某个用户的赞的key
     */
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }


    // 若A关注了B, 则A是B的follower 粉丝, B是A的followee 目标
    // 某个用户关注的实体, 体现用户和实体的关系, 时间作为分数
    // followee:userId:entityType -> zset(entityId, now)
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个用户拥有的粉丝
    // follower:entityType:entityId -> zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }



    // 登录验证码

    /**
     *
     * @param owner     一个随机字符串, 发给未登录用户的凭证, 用于表示验证码, 很快失效.
     * @return          owner 凭证对应的验证码的 redis 的键
     */
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

}
