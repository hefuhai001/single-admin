package com.hfh.api.modules.system.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class RouteVO {
    private Long id;
    private String path;
    private String name;
    private String component;
    private String redirect;
    private MetaVO meta;
    private List<RouteVO> children;

    @Data
    public static class MetaVO {
        private String title;
        private String icon;
        private Boolean hidden;
        private Boolean keepAlive;
        private List<String> permissions;
    }
}
