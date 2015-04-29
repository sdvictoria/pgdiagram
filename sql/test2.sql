"SELECT (current_database())::information_schema.sql_identifier AS catalog_name, (n.nspname)::information_schema.sql_identifier AS schema_name, (u.rolname)::information_schema.sql_identifier AS schema_owner, (NULL::character varying)::information_schema.sql_identifier AS default_character_set_catalog, (NULL::character varying)::information_schema.sql_identifier AS default_character_set_schema, (NULL::character varying)::information_schema.sql_identifier AS default_character_set_name, (NULL::character varying)::information_schema.character_data AS sql_path FROM pg_namespace n, pg_authid u WHERE ((n.nspowner = u.oid) AND pg_has_role(n.nspowner, 'USAGE'::text));"




import java.util.UUID;

public class GenerateUUID {
  
  public static final void main(String... aArgs){
    //generate random UUIDs
    UUID idOne = UUID.randomUUID();
    UUID idTwo = UUID.randomUUID();

-- comment 
select * from pg_stat_activity;
	

"-- comment 
select * from pg_stat_activity;"

-- pgDiagram 123
select procpid from pg_stat_activity where current_query like '-- pgDiagram 123%';
select pg_cancel_backend(pid int);

-- pgDiagram% 
select pg_sleep(4000);
select * from pg_stat_activity;
select procpid from pg_stat_activity where current_query like '-- pgDiagram%';
select pg_cancel_backend(7332);

superuser / super
createuser --username postgres 

CREATE ROLE superuser LOGIN ENCRYPTED PASSWORD 'md5d7f543b836580d14a739f12c70c3ff91'
  SUPERUSER CREATEDB CREATEROLE
   VALID UNTIL 'infinity';

   
cd C:\Program Files (x86)\PostgreSQL\8.3\bin>
psql.bat -h localhost -p 5432 postgres "postgres" WIN1252
psql.bat -h localhost -p 5432 postgres "test" WIN1252
psql.bat -h localhost -p 5432 superuser "super" WIN1252