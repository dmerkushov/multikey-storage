package ru.dmerkushov.mkstorage.backend.impl.sql;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SqlStorageMybatisMapper {

    @Select("SELECT mks_links.obj_id FROM mks_links,mks_tags WHERE mks_tags.tag_id=mks_links.tag_id and mks_tags.tag_txt=#{tagTxt}")
    List<Long> getObjIdsByTagTxt(String tagTxt);

    @Select("SELECT * FROM mks_objects WHERE obj_id=#{objId}")
    SqlStorageObject getObjectById(Long objId);
}
