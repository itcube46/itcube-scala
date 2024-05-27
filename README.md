# itcube-scala

## Пример backend-приложения на Scala и ZIO

## Примеры команд CURL

### Authors

```bash
curl -i -X POST -H 'Content-Type: application/json' -d '{"name":"Roman","country":"Russia"}' http://127.0.0.1:8080/authors
```

```bash
curl -i -X PATCH -H 'Content-Type: application/json' -d '{"id":"???","name":"Gvozdev Roman","country":"Russia"}' http://127.0.0.1:8080/authors
```

```bash
curl -i -X DELETE "http://127.0.0.1:8080/authors/???"
```

### Books

```bash
curl -i -X POST -H 'Content-Type: application/json' -d '{"title":"test","publisher":{"name":"test","country":"Russia"},"author":{"name":"Roman","country":"Russia"}}' http://127.0.0.1:8080/books
```

```bash
curl -i -X PATCH -H 'Content-Type: application/json' -d '{"id":"???","title":"test","publisher":{"id":"???","name":"pub","country":"Russia"},"author":{"id":"???","name":"Roma","country":"Russia"}},' http://127.0.0.1:8080/books
```

```bash
curl -i -X DELETE "http://127.0.0.1:8080/books/???"
```

### Publishers

```bash
curl -i -X POST -H 'Content-Type: application/json' -d '{"name":"BHV","country":"Russia"}' http://127.0.0.1:8080/publishers
```

```bash
curl -i -X PATCH -H 'Content-Type: application/json' -d '{"id":"???","name":"BHV","country":"Russia"}' http://127.0.0.1:8080/publishers
```

```bash
curl -i -X DELETE "http://127.0.0.1:8080/publishers/???"
```

### Users

```bash
curl -i -X POST -H 'Content-Type: application/json' -d '{"name":"itcube","email":"example@mail.ru","password":"123"}' http://127.0.0.1:8080/users
```

```bash
curl -i -X PATCH -H 'Content-Type: application/json' -d '{"id":"???","name":"itcube46","email":"example@mail.ru","password":"qwe"}' http://127.0.0.1:8080/users
```

```bash
curl -i -X DELETE "http://127.0.0.1:8080/users/???"
```
