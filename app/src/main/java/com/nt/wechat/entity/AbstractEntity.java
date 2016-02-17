package com.nt.wechat.entity;

/**
 * Created by laoni on 2015/12/18.
 */
import android.content.ContentValues;

public abstract class AbstractEntity {

    public static final String UUID = "uuid";

    protected String uuid;

    public AbstractEntity() {
        this.uuid = java.util.UUID.randomUUID().toString();
    }

    public String getUuid() {
        return this.uuid;
    }

    public abstract ContentValues getContentValues();

    public boolean equals(AbstractEntity entity) {
        return this.getUuid().equals(entity.getUuid());
    }
}
