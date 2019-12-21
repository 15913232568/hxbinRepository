package com.ygsoft.lwh.entity;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class MyResultVO implements Serializable {

    private static final long serialVersionUID = 8019746576304509855L;

    private Integer code;

    //事项编号
    private MySqlVO data;

    private String msg;

}
