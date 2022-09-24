package io.github.leooowf.vips.model;

import lombok.Data;

@Data
public class Key {

    private final String id;
    private final String vip;
    private final long generated;
    private final long end;

}