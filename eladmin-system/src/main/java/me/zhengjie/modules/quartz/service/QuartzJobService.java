package me.zhengjie.modules.quartz.service;

import me.zhengjie.modules.quartz.domain.QuartzJob;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 * @author jie
 * @date 2019-01-07
 */
@CacheConfig(cacheNames = "quartzJob")
public interface QuartzJobService {

    /**
     * create
     * @param resources
     * @return
     * @CacheEvict要求指定一个或多个缓存，使之都受影响。此外，还提供了一个额外的参数allEntries 。表示是否需要清除缓存中的所有元素。默认为false，表示不需要。当指定了allEntries为true时，Spring Cache将忽略指定的key。有的时候我们需要Cache一下清除所有的元素。
     */
    @CacheEvict(allEntries = true)
    QuartzJob create(QuartzJob resources);

    /**
     * update
     * @param resources
     * @return
     */
    @CacheEvict(allEntries = true)
    void update(QuartzJob resources);

    /**
     * del
     * @param quartzJob
     */
    @CacheEvict(allEntries = true)
    void delete(QuartzJob quartzJob);

    /**
     * findById
     * @param id
     * @return
     */
    @Cacheable(key = "#p0")
    QuartzJob findById(Long id);

    /**
     * 更改定时任务状态
     * @param quartzJob
     */
    @CacheEvict(allEntries = true)
    void updateIsPause(QuartzJob quartzJob);

    /**
     * 立即执行定时任务
     * @param quartzJob
     */
    void execution(QuartzJob quartzJob);
}
