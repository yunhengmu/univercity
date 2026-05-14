package com.ruoyi.system.domain;

import lombok.Data;
import java.util.List;

/**
 * 室内数据
 */
@Data
public class IndoorData {
    /** CMS ID */
    private List<String> cmsid;
    /** 真实楼层 */
    private List<String> truefloor;
    /** CP ID */
    private List<String> cpid;
    /** 楼层 */
    private List<String> floor;
}