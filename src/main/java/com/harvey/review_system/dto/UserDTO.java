package com.harvey.review_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 虎哥
 */
@Data
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String nickName;
    private String icon;

    public UserDTO() {

    }
}
