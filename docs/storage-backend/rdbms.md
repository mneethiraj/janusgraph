# RDBMS using EclipseLink JPA

![](https://www.eclipse.org/eclipselink/images/jpa.png)

> The EclipseLink JPA provides developers with a standards based Object-Relational persistence solution with additional support for many advanced features. EclipseLink JPA provides advanced support for leading relational databases and Java containers.
> [EclipseLink Homepage](https://www.eclipse.org/eclipselink/)

## RDBMS Setup

Ensure that database specified in jdbc.url configuration exists, and username specified in jdbc.user configuration has all permissions on this database.

JanusGraph RDBMS storage creates following tables and sequences, if they don't already exist:

Sequences:
```
janus_store_seq
janus_key_seq
janus_column_seq
```

Tables:
```
janus_store
janus_key
janus_column
```

## RDBMS Specific Configuration

Here is sample configuration for RDBMS storage backend that uses Postgres:

```
storage.backend.rdbms
storage.rdbms.jpa.javax.persistence.jdbc.dialect=org.eclipse.persistence.platform.database.PostgreSQLPlatform
storage.rdbms.jpa.javax.persistence.jdbc.driver=org.postgresql.Driver
storage.rdbms.jpa.javax.persistence.jdbc.url=jdbc:postgresql://dbhost/janus
storage.rdbms.jpa.javax.persistence.jdbc.user=janus
storage.rdbms.jpa.javax.persistence.jdbc.password=janusR0cks!
storage.rdbms.jpa.javax.persistence.schema-generation.database.action=create
storage.rdbms.jpa.javax.persistence.schema-generation.create-database-schemas=true
storage.rdbms.jpa.javax.persistence.schema-generation.create-source=metadata
```
