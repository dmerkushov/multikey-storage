create table mks_tag_tags (
    tag_id bigint not null primary key generated always as identity,
    tag_txt varchar(100)
);
create index mks_tag_txt_idx on mks_tag_tags (tag_txt);

create table mks_obj_objects (
    obj_id bigint primary key generated always as identity,
    obj_mime varchar(200),
    obj_keepuntil timestamp with time zone,
    obj_content BLOB
);

create table mks_lnk_links_obj_tag (
    lnk_id bigint primary key generated always as identity,
    lnk_tag_id bigint not null,
    lnk_obj_id bigint not null,
    constraint (lnk_tag_fk) foreign key (lnk_tag_id) references mks_tag_tags (tag_id),
    constraint (lnk_obj_fk) foreign key (lnk_obj_id) references mks_obj_objectss (obj_id)
);
create index mks_lnk_tag_idx on mks_lnk_links_obj_tag (lnk_tag_id);
