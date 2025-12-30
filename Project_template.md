# Задание 1

1. Спроектируйте to be архитектуру КиноБездны, разделив всю систему на отдельные домены и организовав интеграционное взаимодействие и единую точку вызова сервисов.
Результат представлен в виде контейнерной диаграммы в нотации С4.

[диаграмма С4](./diagrams/container/system_kinobezdna.puml)

# Задание 2

### 1. Proxy
Реализован бесшовный переход с применением паттерна Strangler Fig в части реализации прокси-сервиса (API Gateway), с помощью которого можно постепенно переключать траффик, используя фиче-флаг.

Реализован сервис на языке программирования Java в ./src/microservices/proxy.

- запрос к API Gateway:
   ```bash
   curl http://localhost:8000/api/movies
   ```
  Запросы маршрутизировались в монолит либо в movies. В ответ всегда получал список фильмов из init.sql
- Меняя переменную окружения MOVIES_MIGRATION_PERCENT, можно постепенно перейти на 100% маршрутизацию в movies.


### 2. Kafka
Примененил Kafka в данной архитектуре.

Сделал MVP сервис events, который при вызове API создаваёт и сам же читает сообщения в топике Kafka.
    - Разработал сервис на Java с consumer'ами и producer'ами.
    - Реализовал простой API, при вызове которого создаваются события User/Payment/Movie и обрабатываются внутри сервиса с записью в лог

Необходимые тесты для проверки этого API вызываются при запуске npm run test:local из папки tests/postman 
Cкриншот тестов и скриншот состояния топиков Kafka из UI http://localhost:8090
[скриншоты](./screenshots/events/task2)

# Задание 3

Команда начала переезд в Kubernetes для лучшего масштабирования и повышения надежности. 
Вам, как архитектору осталось самое сложное:
 - реализовать CI/CD для сборки прокси сервиса
 - реализовать необходимые конфигурационные файлы для переключения трафика.


### CI/CD

 В папке .github/worflows доработал деплой новых сервисов proxy и events в docker-build-push.yml , чтобы api-tests при сборке отрабатывали корректно при отправке коммита в репозиторий.

Сборка отработала и в [github registry](https://github.com/mironsv?tab=packages) появились образы.
В [Actions](https://github.com/mironsv/architecture-cinemaabyss/actions) "зеленая" сборка и "зеленые" тесты.


### Proxy в Kubernetes

#### Шаг 1
Для деплоя в kubernetes необходимо залогиниться в docker registry Github'а.
1. Создал Personal Access Token (PAT) https://github.com/settings/tokens с правом read:packages
2. В src/kubernetes/*.yaml (event-service, monolith, movies-service и proxy-service) обновил путь до образов 
3. Получил значение в base64
```bash
 echo -n имя_пользователя:токен | base64
```

В ~/.docker/config.json добавил значение для аутентификации
```json
{
        "auths": {
                "ghcr.io": {
                       "auth": "имя_пользователя:токен в base64"
                }
        }
}
```

```bash
cat .docker/config.json | base64
```

4. Добавил в секрет src/kubernetes/dockerconfigsecret.yaml в поле
```bash
 .dockerconfigjson: значение в base64 файла ~/.docker/config.json
```

#### Шаг 2

  В src/kubernetes/event-service.yaml и src/kubernetes/proxy-service.yaml

  - Создал kind: Deployment и Service
  - В ingress.yaml добавил Корневой путь, чтобы можно было с помощью тестов проверить создание событий
  - Дальше идут шаги для поднятия кластера:

  1. Создал namespace:
  ```bash
  kubectl apply -f src/kubernetes/namespace.yaml
  ```
  2. Создал секреты и переменные
  ```bash
  kubectl apply -f src/kubernetes/configmap.yaml
  kubectl apply -f src/kubernetes/secret.yaml
  kubectl apply -f src/kubernetes/dockerconfigsecret.yaml
  kubectl apply -f src/kubernetes/postgres-init-configmap.yaml
  ```

  3. Развернул базу данных:
  ```bash
  kubectl apply -f src/kubernetes/postgres.yaml
  ```

  На этом этапе смотрим поды:
  ```bash
  kubectl -n cinemaabyss get pod
  ```
  NAME         READY   STATUS    
  postgres-0   1/1     Running   

  4. Развернул Kafka:
  ```bash
  kubectl apply -f src/kubernetes/kafka/kafka.yaml
  ```
  Запущено 3 пода
    NAME          READY   STATUS    RESTARTS   AGE
    kafka-0       1/1     Running   0          4m29s
    postgres-0    1/1     Running   0          6m4s
    zookeeper-0   1/1     Running   0          4m29s

  Если что-то не так, то смотрим логи
  ```bash
  kubectl -n cinemaabyss logs имя_пода (например - kafka-0)
  ```
  5. Развернул монолит, микросервисы, прокси-сервис:
  ```bash
  kubectl apply -f src/kubernetes/monolith.yaml
  kubectl apply -f src/kubernetes/movies-service.yaml
  kubectl apply -f src/kubernetes/events-service.yaml
  kubectl apply -f src/kubernetes/proxy-service.yaml
  ```
  Смотрим поды. В Running и Ready 1/1 перейдет не сразу (нужные минуты) 
  ```bash
  kubectl -n cinemaabyss get pod
  ```

  Будет наподобие такого

```bash
  NAME                              READY   STATUS    

  events-service-7587c6dfd5-6whzx   1/1     Running  

  kafka-0                           1/1     Running   

  monolith-8476598495-wmtmw         1/1     Running  

  movies-service-6d5697c584-4qfqs   1/1     Running  

  postgres-0                        1/1     Running  

  proxy-service-577d6c549b-6qfcv    1/1     Running  

  zookeeper-0                       1/1     Running 
```

  8. Добавил аддон ingress и запустил его 
  ```bash
  minikube addons enable ingress
  kubectl apply -f src/kubernetes/ingress.yaml
  ```
  9. Добавил в /etc/hosts
  127.0.0.1 cinemaabyss.example.com

  10. Вызвал
  ```bash
  minikube tunnel
  ```
  11. Вызвал https://cinemaabyss.example.com/api/movies
  увидел вывод списка фильмов
  Можно поэкспериментировать со значением   MOVIES_MIGRATION_PERCENT в src/kubernetes/configmap.yaml и убедиться, что вызовы movies уходят полностью в новый сервис

  12. Запустил тесты из папки tests/postman
  ```bash
   npm run test:kubernetes
  ```

#### Шаг 3
скриншот вывода при вызове https://cinemaabyss.example.com/api/movies и скриншот вывода event-service после вызова тестов.
[скриншоты](./screenshots/events/task3)


# Задание 4
Для простоты дальнейшего обновления и развертывания вам как архитектуру необходимо так же реализовать helm-чарты для прокси-сервиса и проверить работу 

Для этого:
1. В директории helm отредактировал файл values.yaml
- Вместо ghcr.io/db-exp/cinemaabysstest/proxy-service написал свой путь до образа для всех сервисов:
напр, repository: ghcr.io/mironsv/architecture-cinemaabyss/proxy-service
- для imagePullSecret проставил свое значение (скопировал из файла конфигурации kubernetes/dockerconfigsecret.yaml)
  ```yaml
  imagePullSecrets:
      dockerconfigjson: ...
  ```

2. В папке ./templates/services заполнил шаблоны для proxy-service.yaml и events-service.yaml (опирался на свою kubernetes конфигурацию - смысл helm'а сделать шаблоны для быстрого обновления и установки)

```yaml
template:
    metadata:
      labels:
        app: proxy-service
    spec:
      containers:
       Тут ваша конфигурация
```

3. Проверил установку
Сначала удалил установку руками

```bash
kubectl delete all --all -n cinemaabyss
kubectl delete namespace cinemaabyss
```
Проверил что в /etc/hosts
127.0.0.1 cinemaabyss.example.com

Запустил
```bash
minikube start
minikube addons enable ingress
minikube tunnel
minikube dashboard
sudo snap install helm --classic
helm install cinemaabyss ./src/kubernetes/helm --namespace cinemaabyss --create-namespace
kubectl get pods -n cinemaabyss
```

Потом вызвал (http schema)
curl http://cinemaabyss.example.com/api/movies
[скриншоты](./screenshots/events/task4)
curl http://cinemaabyss.example.com/api/events/health

## Запустил тесты
```bash
cd tests/postman/
npm run test:kubernetes
```
[скриншоты](./screenshots/events/task4/tests)

## Удалил все

```bash
helm uninstall cinemaabyss
kubectl delete all --all -n cinemaabyss
kubectl delete namespace cinemaabyss
```

ПОЛЕЗНЫЕ КОМАНДЫ

Обновление helm:
helm upgrade cinemaabyss ./src/kubernetes/helm/ -n cinemaabyss

Остановка minikube:
minikube stop
minikube delete

kubectl apply -f src/kubernetes/ingress.yaml
cat /etc/hosts
kubectl port-forward service/proxy-service 7000:80 -n cinemaabyss

