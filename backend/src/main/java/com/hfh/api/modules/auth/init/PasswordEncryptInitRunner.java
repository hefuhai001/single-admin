package com.hfh.api.modules.auth.init;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hfh.api.modules.system.entity.UserEntity;
import com.hfh.api.modules.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 密码加密初始化脚本
 * <p>
 * 项目启动时自动执行，将数据库中所有长度小于 60 的明文密码批量加密为 BCrypt 密文。
 * BCrypt 密文长度固定为 60 字符，因此可以通过密码长度判断是否已加密。
 * </p>
 * <p>
 * 注意：该脚本只需执行一次，所有存量密码加密完成后，后续启动不会再修改。
 * 如果未来新增用户，密码应在注册/新增接口中直接加密保存。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordEncryptInitRunner implements ApplicationRunner {

    private final UserMapper userMapper;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * BCrypt 加密后的密码长度固定为 60
     */
    private static final int BCRYPT_PASSWORD_LENGTH = 60;

    @Override
    public void run(ApplicationArguments args) {
        List<UserEntity> users = userMapper.selectList(
                new QueryWrapper<UserEntity>().lt("LENGTH(password)", BCRYPT_PASSWORD_LENGTH)
        );

        if (users.isEmpty()) {
            log.info("【密码加密初始化】未发现明文密码，跳过处理");
            return;
        }

        int count = 0;
        for (UserEntity user : users) {
            String rawPassword = user.getPassword();
            if (rawPassword == null || rawPassword.length() >= BCRYPT_PASSWORD_LENGTH) {
                continue;
            }

            String encodedPassword = PASSWORD_ENCODER.encode(rawPassword);
            userMapper.update(
                    null,
                    new UpdateWrapper<UserEntity>()
                            .eq("id", user.getId())
                            .set("password", encodedPassword)
            );
            count++;
            log.info("【密码加密初始化】用户 {} 的密码已加密", user.getUsername());
        }

        log.info("【密码加密初始化】共完成 {} 个用户的密码加密", count);
    }
}
