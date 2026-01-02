

## HTTP 

> ### reading http request, why double check? ("" && null)
> 
> line.equals("") : 빈 문자열 , 헤더가 끝나고 본문 시작 전 정상적인 종료 시점
> line == null : 데이터 없음 , 데이터가 안 들어옴 (물리적 스트림 종료)
> 


> ### thread pool 
> 
> - pre-allocation
> - use task queue
> - risk of too many thread ? Leads to high context switching overhead and OOM
> - risk of too few thread ? cpu use down, throughput become slow


> ### why use thread pool?
> 
> thread per request 의 한계 해결
> 
> prevent oom, context switching overhead 조절
> 
> reuse thread
> 
> speed up (because no create thread)


> ### thread pool size?
> 
> if cpu bound task ) cpu core num + 1  : minimize context switching
> 
> if I/O bound task ) n_cpu * u_cpu * (1 +wait time/compute time) 
> 
> constraint : memeory, database connection pool , file descriptor  => using load test!

> ### 스레드 풀이 꽉 차거나 거절당하면??
> 
> listenSocket.accept()를 통해 새로운 소켓 연결(connection)이 만들어짐 executorService.execute()에 던짐
> 
> execute()를 try-catch로 감싸고, 거절당한 경우 catch 블록에서 직접 socket.close()를 수행

-----
> ###  MIME
> 
> Content type 


----
## step2
- [x] 확장자 파싱하기
- [x] MIME 타입 매핑하기  (ai: 확장자 map 만드는 과정)
- [x] 타입에 맞춰 응답 헤더 전송


> ### 확장자가 없는 요청에서 Content-Type 처리
> 
> - Dynamic HTML : `text/html`로 설정
> - data(json) 보내 줄 때 : `application/json`
> - redirection : body없거나 그러면 content-type 생략가능..?


