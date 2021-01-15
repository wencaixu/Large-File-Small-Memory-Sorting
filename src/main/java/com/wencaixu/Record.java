package com.wencaixu;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Record {
    int id;
    byte gender;
    short depart;
    int age;
}
