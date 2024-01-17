package com.harvey.review_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@TableName("tb_shop")
public class Shop implements Serializable, Entity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    Long id;

    /**
     * 商铺名称
     */
    @Getter
    private String name;

    /**
     * 商铺类型的id
     */
    @Getter
    private Long typeId;

    /**
     * 商铺图片，多个图片以','隔开
     */
    @Getter
    private String images;

    /**
     * 商圈，例如陆家嘴
     */
    @Getter
    private String area;

    /**
     * 地址
     */
    @Getter
    private String address;

    /**
     * 经度
     */
    @Getter
    private Double x;

    /**
     * 维度
     */
    @Getter
    private Double y;

    /**
     * 均价，取整数
     */
    @Getter
    private Long avgPrice;

    /**
     * 销量
     */
    @Getter
    private Integer sold;

    /**
     * 评论数量
     */
    @Getter
    private Integer comments;

    /**
     * 评分，1~5分，乘10保存，避免小数
     */
    @Getter
    private Integer score;

    /**
     * 营业时间，例如 10:00-22:00
     */
    @Getter
    private String openHours;

    /**
     * 创建时间
     */
    @Getter
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Getter
    private LocalDateTime updateTime;
    @Getter
    @TableField(exist = false)
    private Double distance;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Shop{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", typeId=" + typeId +
                ", images='" + images + '\'' +
                ", area='" + area + '\'' +
                ", address='" + address + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", avgPrice=" + avgPrice +
                ", sold=" + sold +
                ", comments=" + comments +
                ", score=" + score +
                ", openHours='" + openHours + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", distance=" + distance +
                '}';
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public void setAvgPrice(Long avgPrice) {
        this.avgPrice = avgPrice;
    }

    public void setSold(Integer sold) {
        this.sold = sold;
    }

    public void setComments(Integer comments) {
        this.comments = comments;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setOpenHours(String openHours) {
        this.openHours = openHours;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

}
