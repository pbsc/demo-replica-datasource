create table user
(
    id        bigint not null primary key auto_increment,
    name      varchar(255),
    email     varchar(255)
);

INSERT INTO user (id, name, email)
VALUES (1, 'test1', 'test1@test.com'),
       (2, 'test2', 'test2@test.com'),
       (3, 'test3', 'test3@test.com');

