

## HTTP 

> ### when reading http request by one line, why need double check? ("" && null)
> 
> line.equals("") : 빈 문자열 , 헤더가 끝나고 본문 시작 전 정상적인 종료 시점
> line == null : 데이터 없음 , 데이터가 안 들어옴 (물리적 스트림 종료)
> 


> ### thread pool ?
> 
> - pre-allocation
> - use task queue
> - if too many thread ? context switching overhead, oom 
> - if too min thread ? cpu use down, throughout low


> ### why use thraed pool?
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
> if cpu bound task ) cpu core num + 1  : context switching mininum 
> 
> if I/O bound task ) n_cpu * u_cpu * (1 +wait time/compute time) '
> 
> constraint : memeory, database connection pool , file descriptor  => using load test!

> ### 스레드 풀이 꽉 차거나 거절당하면??
> 
> listenSocket.accept()를 통해 새로운 소켓 연결(connection)이 만들어짐 executorService.execute()에 던짐
> 