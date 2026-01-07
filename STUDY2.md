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
> - 비즈니스 로직 없 데이터만 전달하는!
