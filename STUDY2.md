--------
#### 0106 Goal
 - [x] http method Enum 으로 분리
 - [] url parsing logic update 예외 처리..
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
Controller 가 없다면, 정적리소스 처리를 위해 `StaticResourceProcessor`로 넘어간다.

