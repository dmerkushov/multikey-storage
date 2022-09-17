package ru.dmerkushov.mkstorage.backend.impl.sql;

public class SqlStorageObject {

    Long objId;
    byte[] objContent;
    String objMime;
    java.sql.Timestamp objKeepUntil;
}
