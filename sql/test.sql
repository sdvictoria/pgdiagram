drop table if exists TestB;
drop table if exists TestA;
drop index if exists some_idx1;
drop index if exists some_idx2;

create table TestA (
a1 integer,
a2 integer,
a3 integer,
a4 integer,
a_unique1 integer,
a_unique2 integer,
a_unique3 integer,
PRIMARY KEY (a1),
UNIQUE (a2),
UNIQUE (a3, a4),
UNIQUE (a_unique1),
UNIQUE (a_unique2, a_unique3)
);


create table TestB (
b1 integer,
b2 integer,
b3 integer,
b4 integer,
b5 integer not null,
b6 integer,
FOREIGN KEY (b1) REFERENCES TestA(a1),
FOREIGN KEY (b2) REFERENCES TestA(a2),
FOREIGN KEY (b3, b4) REFERENCES TestA(a4, a3)
);

CREATE INDEX some_idx1 ON TestB(b6);
CREATE INDEX some_idx2 ON TestB(b5, b6);

drop table if exists TestOID;
drop table if exists TestComposite;
drop type  if exists type1;
drop table if exists TestArrays;
drop table if exists TestBitString;
drop table if exists TestNetwork;
drop table if exists TestGeom;
drop table if exists TestBoolean;
drop table if exists TestDateTime;
drop table if exists TestBinary;
drop table if exists TestTypeChar;
drop table if exists TestTypeNumeric;

create table TestTypeNumeric (
f_smallint smallint,
f_integer integer,
f_bigint bigint,
f_decimal decimal,
f_numeric numeric,
f_real real,
f_double_precision double precision,
f_serial serial,
f_bigserial bigserial,
f_money money
);

create table TestTypeChar (
f_varchar varchar(10), 
f_character character(10),
f_text text
);

create table TestBinary (
f_bytea bytea
);

create table TestDateTime (
t_timestamp_wo_tz timestamp without time zone,
t_timestamp_w_tz  timestamp with time zone,
t_interval interval,
t_date date,
t_time_wo_tz time without time zone,
t_time_w_tz time with time zone
);

create table TestBoolean (
boolean boolean
);

create table TestGeom (
f_point point,
f_line line,
f_lseg lseg,
f_box box,
f_path_closed path,
f_path_open path,
f_polygon polygon,
f_circle circle
);


create table TestNetwork (
f_cidr cidr,
f_inet inet,
f_macaddr macaddr
);

create table TestBitString (
f_bit bit (10),
f_bit_varying bit varying(10)
);

create table TestArrays (
f_onedim text[],
f_twodim integer[][],
f_threedim text[][][],
f_fourdim text[][][][]
);

create type type1 as (
    t_1a  integer,
    t_1b  text
);

CREATE TABLE TestComposite (
    t1      type1
);

CREATE TABLE TestOID (
    a    integer
) WITH OIDS


insert into TestTypeNumeric values (0,0,0,0,0,0,0,0,0,'$1');

insert into TestArrays values ('{"str1", "str2"}', null, null, null);
insert into TestArrays values (null, '{{1,2},{3,4}}', null, null);
INSERT INTO TestComposite VALUES (ROW(4, 'four'));
INSERT INTO TestOID values (99);

select * from TestOID;

select * from TestTypeNumeric;


select distinct(schema_name) from information_schema.schemata where catalog_name = 'postgres' 
and schema_name != 'information_schema' and schema_name not like 'pg_%'
order by schema_name;

select * from information_schema.referential_constraints;

select constraint_name, match_option, update_rule, delete_rule from information_schema.referential_constraints where constraint_catalog = 'postgres' and constraint_schema = 'public';

select * from information_schema.key_column_usage order by constraint_name, ordinal_position



select distinct(catalog_name) from information_schema.schemata order by catalog_name;

select distinct(schema_name) from information_schema.schemata where catalog_name = 'postgres' order by schema_name;

select * from information_schema.key_column_usage order BY constraint_name;

select * from information_schema.constraint_column_usage;

select * from information_schema.constraint_table_usage;

select * from information_schema.check_constraint_routine_usage;


select * from information_schema.referential_constraints;


select * from information_schema.key_column_usage
-- where constraint_name = 'a_a3_key'
order by ordinal_position;


select table_catalog, table_schema, table_name from information_schema.key_column_usage
 where constraint_catalog = 'postgres' and constraint_schema = 'public' and constraint_name = 'postgres' group by table_catalog, table_schema, table_name;

select c.conname, contype from 
 pg_authid a
 join pg_namespace n on n.nspowner = a.oid
 join pg_constraint c on c.connamespace = n.oid
 where a.rolname = 'postgres' and n.nspname = 'public' and contype = 'p';


select attr.attname from pg_attribute attr
join pg_class c2 on c2.oid = attr.attrelid
join pg_namespace n2 on n2.oid = c2.relnamespace
join pg_authid a2 on a2.oid = n2.nspowner
where attnum in (5, 6)
and a2.rolname = 'postgres' and n2.nspname = 'public' and c2.relname = 'a'
 

select from information_schema.columns where table_catalog = 'cumulus2'

select distinct(table_schema) from information_schema.columns;


select distinct(schema_name) from information_schema.schemata where catalog_name = 'postgres'   

select distinct(schema_name) from information_schema.schemata where catalog_name = 'postgres'   and schema_name != 'information_schema' and schema_name not like 'pg_%'  and schema_name != 'dbo' and and schema_name != 'sys'  order by schema_name

select c2.*, c2.relkind, attr.attname from pg_attribute attr
--select c2.*, attr.* from pg_attribute attr
join pg_class c2 on c2.oid = attr.attrelid
join pg_namespace n2 on n2.oid = c2.relnamespace
join pg_authid a2 on a2.oid = n2.nspowner
where 
2=2
and a2.rolname = 'postgres' and n2.nspname = 'public' 
-- and c2.relkind = 'i'
--and c2.relname = 'testa'
and attr.attnum>0
order by c2.relname, attr.attnum;


-- select *
select authcon.rolname, nscon.nspname, con.conname, con.contype, con.confupdtype, con.confdeltype, con.confmatchtype, authtab.rolname, nstab.nspname, tab.relname, conkey, authftab.rolname, nsftab.nspname, ftab.relname, confkey
from pg_constraint con
join pg_class tab on tab.oid = con.conrelid
left join pg_class ftab on ftab.oid = con.confrelid
join pg_namespace nscon on nscon.oid = con.connamespace
join pg_namespace nstab on nstab.oid = tab.relnamespace
left join pg_namespace nsftab on nsftab.oid = ftab.relnamespace
join pg_authid authcon on authcon.oid = nscon.nspowner
join pg_authid authtab on authtab.oid = nstab.nspowner
left join pg_authid authftab on authftab.oid = nsftab.nspowner

select c1.relname, a2.rolname, n2.nspname, c2.relname, i.indkey from pg_index i
join pg_class c1 on c1.oid = i.indexrelid
join pg_class c2 on c2.oid = i.indrelid
join pg_namespace n1 on n1.oid = c1.relnamespace
join pg_authid a1 on a1.oid = n1.nspowner
join pg_namespace n2 on n2.oid = c2.relnamespace
join pg_authid a2 on a2.oid = n2.nspowner
where i.indisunique = 'f' and i.indisprimary = 'f'
 and a1.rolname = 'postgres' and n1.nspname = 'public'

select c1.relname, a2.rolname, n2.nspname, c2.relname, i.indkey from pg_index i
join pg_class c1 on c1.oid = i.indexrelid
join pg_class c2 on c2.oid = i.indrelid
join pg_namespace n1 on n1.oid = c1.relnamespace
join pg_authid a1 on a1.oid = n1.nspowner
join pg_namespace n2 on n2.oid = c2.relnamespace
join pg_authid a2 on a2.oid = n2.nspowner
where i.indisunique = 'f' and i.indisprimary = 'f'
 and a1.rolname = 'postgres' and n1.nspname = 'public'

select auth.rolname, nsidx.nspname, cidx.relname, nstab.nspname, ctab.relname, idx.indkey from pg_index idx
join pg_class cidx on cidx.oid = idx.indexrelid
join pg_class ctab on ctab.oid = idx.indrelid
join pg_namespace nsidx on nsidx.oid = cidx.relnamespace
join pg_namespace nstab on nstab.oid = ctab.relnamespace
join pg_authid auth on auth.oid = nsidx.nspowner
where idx.indisunique = 'f' and idx.indisprimary = 'f'
;

select current_database();

select c2.relname, attr.attname, attr.attnum from pg_attribute attr
join pg_class c2 on c2.oid = attr.attrelid
join pg_namespace n2 on n2.oid = c2.relnamespace
join pg_authid a2 on a2.oid = n2.nspowner
where 
2=2
and a2.rolname = 'postgres' and n2.nspname = 'public' 
and attr.attnum>0 and c2.relkind='r' 
order by c2.relname, attr.attnum
;

select c2.relname, attr.attname, attr.attnum, attr.attnotnull from pg_attribute attr
join pg_class c2 on c2.oid = attr.attrelid
join pg_namespace n2 on n2.oid = c2.relnamespace
join pg_authid a2 on a2.oid = n2.nspowner
where 
2=2
and a2.rolname = 'postgres' and n2.nspname = 'public' 
and attr.attnum>0 and c2.relkind='r' 
and attr.attisdropped = 'f'
order by c2.relname, attr.attnum

SHOW search_path;

select setting||'/global' from pg_settings where name='data_directory';

-- pgDiagram% 
select pg_sleep(4000);
select * from pg_stat_activity;

select procpid from pg_stat_activity where current_query like '-- pgDiagram%';


-- pdDiagram query

select pg_cancel_backend(6288);