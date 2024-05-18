-- Издательства
CREATE TABLE IF NOT EXISTS public."Publishers"
(
    "id" uuid NOT NULL,
    "name" character varying(50) NOT NULL,
    "country" character varying(20) NOT NULL,

    CONSTRAINT "PublisherPK" PRIMARY KEY ("id")
);

-- Авторы
CREATE TABLE IF NOT EXISTS public."Authors"
(
    "id" uuid NOT NULL,
    "name" character varying(50) NOT NULL,
    "country" character varying(20),

    CONSTRAINT "AuthorPK" PRIMARY KEY ("id")
);

-- Книги
CREATE TABLE IF NOT EXISTS public."Books"
(
    "id" uuid NOT NULL,
    "title" character varying(50) NOT NULL,
    "isbn" character varying(20),
    "year" integer,
    "publisherId" uuid,

    CONSTRAINT "BookPK" PRIMARY KEY ("id"),
    CONSTRAINT "PublisherFK" FOREIGN KEY ("publisherId")
        REFERENCES public."Publishers" ("id") MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
);

-- Книги-Авторы
CREATE TABLE IF NOT EXISTS public."BooksAuthors"
(
    "bookId" uuid NOT NULL,
    "authorId" uuid NOT NULL,

    CONSTRAINT "AuthorFK" FOREIGN KEY ("authorId")
        REFERENCES public."Authors" ("id") MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID,
    CONSTRAINT "BookFK" FOREIGN KEY ("bookId")
        REFERENCES public."Books" ("id") MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
);

-- Пользователи
CREATE TABLE IF NOT EXISTS public."Users"
(
    "id" uuid NOT NULL,
    "name" character varying(50) NOT NULL,
    "email" character varying(50) NOT NULL,
    "password" character varying(50) NOT NULL,

    CONSTRAINT "UsersPK" PRIMARY KEY ("id")
);

-- Пользователи-Книги
CREATE TABLE IF NOT EXISTS public."UsersBooks"
(
    "userId" uuid NOT NULL,
    "bookId" uuid NOT NULL,

    CONSTRAINT "BookFK" FOREIGN KEY ("bookId")
        REFERENCES public."Books" ("id") MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT "UserFK" FOREIGN KEY ("userId")
        REFERENCES public."Users" ("id") MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
