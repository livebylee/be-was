--------
#### 0106 Goal
 - [x] http method Enum 으로 분리
 - [x] url parsing logic update
 - [] 테스트 케이스 추가
 - [] 4단계 
 - [] 상태코드 공부하고, 적절한 상태코드 적용하기
 
 - [] jvm 공부하기 

 ----
### RequestHandler 에서의 url 처리 로직 리팩토링
> - 경로에 따른 실행 로직 선택을 if문으로 처리하고 있었는데 이건 로직이 커지면 if문이 매우 많아지고
비효율적이라는 생각이 들었다. 이러한 역할은 스프링이 수행한다고 해서, 스프링이 어떤 방식으로 처리해주고 있는지
확인해보았다. 
> 수동으로 분기 처리하는 걸 스프링에서는 자동화하여 만들어두었다고 한다. `DispatcherServlet` 을 Front Controller라고 정의한다.
`DispatcherServelt`은 `HttpServlet`을 상속받은 클래스이다. 모든 경로를 분석하여 해시맵에 담아둔다. (URL이 키, 메서드 정보를 값으로)
> + Scanning & Reflection : 더 알아보기

> **결과**
> 스프링의 처리 방식처럼 수정하였다. `Controller interface`를 만들고, 이걸 기반으로
회원가입 로직을 처리하는 `CreateUserController`을 만들었다. (기존의 UserHandler 기반)
> `RequestMapping`에서 url과 controller를 map으로 관리하도록 구현하였다. 
> 
> 현재 실행 로직 : `RequestHandler` 가 `RequestMapping.getController(path)`호출하고,
핸들러를 찾아 `process()`메서드로 해당하는 핸들러의 실제 로직이 실행된다. Map에 등록된 
Controller 가 없다면, 정적[WebServer.java](src/main/java/webserver/WebServer.java)리소스 처리를 위해 `StaticResourceProcessor`로 넘어간다.

### HTTP status code
```
1xx (Informational)	: 요청을 받았으며 작업을 계속 진행 중임
2xx (Successful)	: 요청을 성공적으로 처리함	
3xx (Redirection)	: 요청을 완료하려면 추가 동작이 필요함	
4xx (Client Error)	: 클라이언트의 요청에 문제가 있음
5xx (Server Error)	: 서버가 유효한 요청을 처리하지 못함
```

### 에러 처리 
```
private void parseRequestLine(String requestLine) {
  String[] tokens = requestLine.split(" ");
  if (tokens.length < 2) return; //method, path도 없는 경우
  this.method = from(tokens[0]);
  parseUrl(tokens[1]);
}
```
그룹 세션 진행 중, 잘못된 요청에 대해 그냥 return을 하고있는 문제를 발견했다. 그냥 리턴하는게 아니라
예외 처리 로직이 필요하고, 상태코드를 반환해줘야한다.


> ### 메서드를 파악하고 메서드에 따른 각각의 처리를 하기 위해선?
> 
> 메서드를 컨트롤러에 인자로 넘겨야할까? 
> -> 매핑 자체에 경로랑 메서드를 같이 넣자! 컨트롤러 결정 시에 메서드 확인해야하니까!
> 기존의 map에서 key를 (경로,메서드) 형태로 할 수 있을까? 이걸 어떤 문법으로? 어떻게 할 수 있는지, ai 활용 

> record class type을 보고 map의 key를 record로 만들어 처리하였다.



#### 0107 Goal
- map method add
- response refactoring
- step4 finish


> ### Java `Record` : class type
> 
> - immutable data 객체 쉽게 만들 수 있다
> - 간결성, 메서드 자동 생성, 생성자 자동 생성, 불변성
> - 응답 데이터 담을 때, 복합 키, 임시 데이터 구조 필요할때
> - 비즈니스 로직 없이 데이터만 전달하는!


> ### `RequestMapping` 에서 method와 url을 키로 사용해 handler를 매핑하기 위해 record 도입에 대해,
> 
> Q. `ReqeustMapping` 내부 메서드 `getController`에서 반환 시마다 record 객체를 매번 생성하는 것 같은데 비효율적이지 않은가?
> A. JVM은 객체 생성이 매우 빠르고, record와 같은 작은 객체는 memory의 `Eden` 영역에 할당된다. 여기서 생성된 record는 메소드 종료와 동시에
참조가 되지 않아 gc가 빠르게 정리한다. 하나의 객체를 재사용하면서 동기화 처리 등을 생각하는 거 보다 매번 생성하는 것이 낫다

> ### `RequestMapping` 의 구조
> 
> url 매핑의 예외처리를 생각해본다면,
```
if (exist url)
    if(method correct) sucess!
    else 405 method not allowed
else 404 not found
```
> 현재 방식으로 이 로직을 처리하면, map으로 찾은 후 method를 확인하기 위해 다시 전체를 뒤져야하기에 비효율적일 것이다.
> url로 메서드와 컨트롤러를 찾아주는 방식이 좀 더 이 예외처리 로직을 담기에 적합해보인다. -> map<url,map<method,controller>> 
> 
> 그러나 다시 생각해보니, url 존재 안하면 바로 405 반환이 아니라 정적리소스 찾으러 가야하기에.. 어차피 깔끔한 로직이 아닌 것 같아 그냥 직관적인
현재 구조로 가기로 했다. 
> url이 존재는 하는지 판단하는 메서드 추가 
> 
> 해당 부분에서 어느 메서드가 405,404를 반환해야하는지 고민이 있었다.



> ### HTTP Body ?
>
> - POST PUT PATCH : 보통 body가 포함
> - Content-Length, Transfer-Encoding 헤더 보고 판단
> - Empty Line(CRLF) 로 구성된 빈 줄로 header와 body를 구분한다


> ### HTTP parsing logic
> http request의 구조대로, requestline , header,body의 파서가 각각 존재하는게 좋다고 생각이 들어 그렇게 리팩토링했다.


> ### enum 내에서 조회 메서드의 효율성?
>
> 1. 기존 방식 
> from(String value) 메서드가 values()를 호출해 순회하는 식으로 구현되는 것 같아 그렇게 구현했다. (O(n))
> 그러나 enum 상수가 많아지는 상황을 고려하면 비효율적이다. 또한 values()는 copy본을 계속 생성해 메모리 오버헤드가 있을 수 있다는 생각이 들었다.
> (사실 지금 코드에서 enum상수가 많아질 일은 없다는 걸 알지만..)

> 2. 내 생각
> map으로 조희하면 O(1) 일텐데.. map이 낫지 않나? enum 내부에서 map을 또 쓰는건 어떤가?
>
> 3. 개선 방식
> Enum 내부에 static Map을 선언하고, 클래스 로딩 시점에 상수들을 미리 매핑해두는 방식 사용 
> from에서 map 사용 O(1)으로 조회 가능!
>
> +) 찾아보다가 `EnumMap` 을 발견했다. enum을 키로 데이터를 관리할때는 이걸 써보자..

> ### enum 내에서 NONE 처리까지 해주는게 맞을까?
> 
> Null Object Pattern...