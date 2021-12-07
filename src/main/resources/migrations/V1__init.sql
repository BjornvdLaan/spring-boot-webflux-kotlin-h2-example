CREATE TABLE cat
(
    id   bigint IDENTITY PRIMARY KEY,
    name varchar NOT NULL,
    type varchar NOT NULL,
    age  int     NOT NULL
);