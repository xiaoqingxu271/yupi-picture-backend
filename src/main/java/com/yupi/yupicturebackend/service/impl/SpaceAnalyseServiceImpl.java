package com.yupi.yupicturebackend.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupicturebackend.controller.SpaceController;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.mapper.SpaceMapper;
import com.yupi.yupicturebackend.model.dto.space.analyse.*;
import com.yupi.yupicturebackend.model.entity.Picture;
import com.yupi.yupicturebackend.model.entity.Space;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.analyse.*;
import com.yupi.yupicturebackend.service.PictureService;
import com.yupi.yupicturebackend.service.SpaceAnalyseService;
import com.yupi.yupicturebackend.service.SpaceService;
import com.yupi.yupicturebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chun0
 * @description 针对表【space(空间分析)】的数据库操作Service实现
 * @createDate 2025-11-22 14:23:07
 */
@Service
public class SpaceAnalyseServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceAnalyseService {

    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private PictureService pictureService;
    @Autowired
    private SpaceController spaceController;

    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(
            SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
            User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        boolean queryPublic = spaceUsageAnalyzeRequest.isQueryPublic();
        boolean queryAll = spaceUsageAnalyzeRequest.isQueryAll();
        // 全空间或者公共图库，查询picture表
        if (queryAll || queryPublic) {
            // 校验分析权限是否是管理员
            checkSpaceAnalyseAuth(spaceUsageAnalyzeRequest, loginUser);
            // 统计图库的使用范围
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            // 补充查询范围
            fillAnalyseQueryWrapper(spaceUsageAnalyzeRequest, queryWrapper);
            // 只查询出图片大小就可以完成需求，不需要查询出对象
            queryWrapper.select("picSize");
            // 查询
            List<Object> pictureObjList = pictureService.getBaseMapper().selectObjs(queryWrapper);
            // 统计图片的总大小和总数量
            long usedSize = pictureObjList.stream().mapToLong(picSize -> (Long) picSize).sum();
            long usedCount = pictureObjList.size();
            // 封装返回结果
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(usedSize);
            spaceUsageAnalyzeResponse.setUsedCount(usedCount);
            // 全空间和公共图库是没有最大限制
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setCountUsageRatio(null);
            return spaceUsageAnalyzeResponse;
        } else {
            // 特定空间，查询对应的space表
            // 校验分析权限
            Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
            spaceService.checkSpaceAuth(space, loginUser);
            long usedSize = space.getTotalSize();
            long usedCount = space.getTotalCount();
            long maxSize = space.getMaxSize();
            long maxCount = space.getMaxCount();
            // 封装返回结果
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(usedSize);
            spaceUsageAnalyzeResponse.setUsedCount(usedCount);
            spaceUsageAnalyzeResponse.setMaxSize(maxSize);
            spaceUsageAnalyzeResponse.setMaxCount(maxCount);
            // 计算比率
            double sizeUsageRatio = NumberUtil.round(usedSize * 100.0 / maxSize, 2).doubleValue();
            double countUsageRatio = NumberUtil.round(usedCount * 100.0 / maxCount, 2).doubleValue();
            spaceUsageAnalyzeResponse.setSizeUsageRatio(sizeUsageRatio);
            spaceUsageAnalyzeResponse.setCountUsageRatio(countUsageRatio);
            return spaceUsageAnalyzeResponse;
        }
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(
            SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
            User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 校验权限
        checkSpaceAnalyseAuth(spaceCategoryAnalyzeRequest, loginUser);
        // 构建查询
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 添加查询条件
        fillAnalyseQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);
        // 使用分组聚合查询
        queryWrapper.select("category", "count(*) as count", "sum(picSize) as totalSize")
                .groupBy("category");
        // 查询并构建返回结果
        return pictureService.getBaseMapper().selectMaps(queryWrapper)
                .stream()
                .map(result -> {
                    String category = (String) result.get("category");
                    Long count = ((Number) result.get("count")).longValue();
                    Long totalSize = ((Number) result.get("totalSize")).longValue();
                    return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest,
                                                            User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 校验权限
        checkSpaceAnalyseAuth(spaceTagAnalyzeRequest, loginUser);
        // 构建查询
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 添加查询条件
        fillAnalyseQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);
        queryWrapper.select("tags");
        // 获取标签列表
        List<String> tagJsonList = pictureService.getBaseMapper().selectObjs(queryWrapper).stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());
        // 解析标签
        Map<String, Long> tagCountMap = tagJsonList.stream()
                .flatMap(tagJson -> JSONUtil.toList(tagJson, String.class).stream())
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));
        // 构建返回结果，并且按照使用次数排序
        return tagCountMap.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest,
                                                              User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 校验权限
        checkSpaceAnalyseAuth(spaceSizeAnalyzeRequest, loginUser);
        // 构建查询
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 添加查询条件
        fillAnalyseQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);
        queryWrapper.select("picSize");
        // 解析图片大小
        Map<String, Long> sizeRanges = new LinkedHashMap<>();
        pictureService.getBaseMapper().selectObjs(queryWrapper).stream()
                .filter(ObjUtil::isNotNull)
                .map(size -> (Long) size)
                .forEach(picSize -> {
                    if (picSize < 1024 * 100) {
                        sizeRanges.put("<100KB", sizeRanges.getOrDefault("<100KB", 0L) + 1);
                    } else if (picSize < 1024 * 500) {
                        sizeRanges.put("100KB-500KB", sizeRanges.getOrDefault("100KB-500KB", 0L) + 1);
                    } else if (picSize < 1024 * 1024) {
                        sizeRanges.put("500KB-1MB", sizeRanges.getOrDefault("500KB-1MB", 0L) + 1);
                    } else {
                        sizeRanges.put(">1MB", sizeRanges.getOrDefault(">1MB", 0L) + 1);
                    }
                });
        // 返回结果
        return sizeRanges.entrySet().stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest,
                                                              User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 校验权限
        checkSpaceAnalyseAuth(spaceUserAnalyzeRequest, loginUser);
        // 构建查询
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 添加查询条件
        fillAnalyseQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);
        // 补充查询条件
        Long userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        // 补充时间维度查询
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m-%d') as period", "count(*) as count");
                break;
            case "week":
                queryWrapper.select("YEARWEEK(createTime) as period", "count(*) as count");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m') as period", "count(*) as count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度");
        }
        // 分组排序
        queryWrapper.groupBy("period").orderByAsc("period");
        // 构建返回结果
        return pictureService.getBaseMapper().selectMaps(queryWrapper).stream()
                .map(result -> {
                    String period = String.valueOf(result.get("period"));
                    Long count = ((Number) result.get("count")).longValue();
                    return new SpaceUserAnalyzeResponse(period, count);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 校验权限
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        // 构建查询
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "spaceName", "userId", "totalSize")
                .orderByDesc("totalSize")
                .last("LIMIT " + spaceRankAnalyzeRequest.getTopN());
        return spaceService.list(queryWrapper);
    }

    @Override
    public void checkSpaceAnalyseAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        // 全空间和公共图库校验
        if (queryAll || queryPublic) {
            // 校验用户是否为管理员
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        } else {
            // 特定空间的校验
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
            spaceService.checkSpaceAuth(space, loginUser);
        }
    }

    /**
     * 根据请求参数添加查询条件
     *
     * @param spaceAnalyzeRequest 空间分析请求参数
     * @param queryWrapper        图片查询包装器
     */
    private static void fillAnalyseQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest,
                                                QueryWrapper<Picture> queryWrapper) {
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        if (queryAll) {
            return;
        }
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        if (queryPublic) {
            queryWrapper.isNull("spaceId");
            return;
        }
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }
}




