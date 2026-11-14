-- P1-2：为 sys_user.email 增加唯一索引
-- 先清理历史重复数据：同一邮箱保留 id 最小的一条，其余记录的邮箱置空（不删除账号）
-- MySQL 的唯一索引允许多个 NULL 值，因此置空后不会冲突

UPDATE `sys_user` u
JOIN (
    SELECT `email`, MIN(`id`) AS keep_id
    FROM `sys_user`
    WHERE `email` IS NOT NULL AND `email` <> ''
    GROUP BY `email`
    HAVING COUNT(*) > 1
) d ON u.`email` = d.`email` AND u.`id` <> d.keep_id
SET u.`email` = NULL;

-- 并发注册时由数据库保证邮箱唯一
ALTER TABLE `sys_user`
    ADD UNIQUE INDEX `uk_email` (`email` ASC) USING BTREE;
