# Broker-Server

MiniStock의 Trade-Server가 주문을 보내는 바깥 세상(현지 브로커 + 거래소)을 흉내 내는 서버입니다.

- 주문을 받으면 수량을 여러 조각으로 나눠 부분 체결합니다.
- 체결가는 지정가 이하에서 정합니다.
- 체결마다 Trade-Server에 체결을 통보합니다.
- DB 없이 메모리만 씁니다. 장부는 Trade-Server에만 있습니다.

## 실행

```bash
./gradlew bootRun
```

포트 8081, Trade-Server 주소는 `application.yml`의 `trade-server.base-url`입니다.
