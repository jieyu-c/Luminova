package cn.jieyuc.user.infrastructure.username;

import cn.jieyuc.user.infrastructure.persistence.entity.UserPO;
import cn.jieyuc.user.infrastructure.persistence.mapper.UserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsernameBloomFilterInitializer implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(UsernameBloomFilterInitializer.class);
    private static final int PRELOAD_BATCH_SIZE = 1000;

    @Autowired
    private UsernameBloomFilterSupport usernameBloomFilterSupport;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) {
        if (!usernameBloomFilterSupport.initIfAbsent()) {
            LOG.info("Username bloom filter already exists in Redis, skip DB preload");
            return;
        }

        int loadedCount = preloadUsernamesInBatches();
        LOG.info("Username bloom filter initialized, loaded {} usernames from DB", loadedCount);
    }

    private int preloadUsernamesInBatches() {
        int loadedCount = 0;
        Long lastId = 0L;

        while (true) {
            List<UserPO> batch = userMapper.selectList(
                    Wrappers.<UserPO>lambdaQuery()
                            .select(UserPO::getId, UserPO::getUsername)
                            .isNotNull(UserPO::getUsername)
                            .gt(UserPO::getId, lastId)
                            .orderByAsc(UserPO::getId)
                            .last("LIMIT " + PRELOAD_BATCH_SIZE)
            );
            if (batch.isEmpty()) {
                break;
            }

            for (UserPO user : batch) {
                usernameBloomFilterSupport.add(user.getUsername());
                loadedCount++;
            }
            lastId = batch.get(batch.size() - 1).getId();
        }

        return loadedCount;
    }
}
