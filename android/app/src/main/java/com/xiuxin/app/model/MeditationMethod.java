package com.xiuxin.app.model;

import java.io.Serializable;

public class MeditationMethod implements Serializable {
    public String id;
    public String name;
    public String category; // 专注类/观照类/慈心类/身体类/导引类
    public String briefMethod; // 简短方法描述
    public String detailedMethod; // 详细方法
    public String benefits; // 功效好处
    public String difficulty; // 入门/进阶/高级
    public int duration; // 建议时长（分钟）
    public boolean isPinned; // 是否置顶
    public String videoUrl; // 视频 URL（导引类）
    public int iconRes; // 图标资源

    public MeditationMethod(String id, String name, String category, 
                           String briefMethod, String detailedMethod, 
                           String benefits, String difficulty, int duration) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.briefMethod = briefMethod;
        this.detailedMethod = detailedMethod;
        this.benefits = benefits;
        this.difficulty = difficulty;
        this.duration = duration;
        this.isPinned = false;
        this.videoUrl = null;
    }
}
