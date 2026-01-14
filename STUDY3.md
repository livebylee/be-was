> ### 동적 html 처리 로직 설계
> 1) GET /index.html
> 2) RequestHandler -> RequestMapping -> IndexController
> 3) IndexController 내부 동작
>    3-1) html 템플릿 파일 읽기
>    3-2) 사용자 인증 상태 확인 (쿠키에서 세션 id 추출하고 조회)
>    3-3) 인증 상태에 따른 동적 페이지? 생성
>    3-4) 템플릿 치환 
>    3-5) http response 전송 




> ### 동적 html 처리에 대한 고민
>
> 직접 html을 읽어 문자열을 찾아서 바꾸는 식으로 구현했다. 그렇지만 자바에서 직접적으로 html을 다루는게
이상하게 느껴졌다. 이에 관해 더 알아보다가 템플릿 엔진에 도달했다. 템플릿 엔진은 템플릿 (html)의 빈칸을 모델 데이터로 채워 html을 만들어내는 과정을 수행한다.



> ### RequestHandler 리팩토링?
> 
> 로그인 상태 확인 로직을 구현하다보니 requestHandler에 적합하지 않은 로그인 상태 확인이 이뤄지고 있다.
> 따라서 해당 로직을 분리하고자 리팩토링을 진행하였다.
> 



-----

> ### step7 loadmap
>  1. db 만들기
> 2. 게시글 조회 구현하기 (텍스트 게시판 완성)
> 3. 이미지 업로드 처리..
> 4. 이미지 출력 및 프로필 변경



> ### JDBC (Java Database Connectivity)
> 
> 데이터베이스에 연결하고 SQL 쿼리를 실행하는 표준 API



-----
#### 0114 TODO (우선 글로만 게시글 올리는 기능 구현)
- [x] 글쓰기 버튼 추가
- [x] 사용자에 따른 글쓰기 페이지 접근 제한 처리
- [x] 폼에 입력된 데이터 받아오기
- [x] ArticleController 
- [x] 데이터 타입 만들고 저장하기
- [x] controller mapping
- [x] 메인 화면에서 최신 게시글 보여주기
- [ ] db에 정렬된 상태로 들어가도록 수정하기

> #### `CopyOnWriteArrayList` 
> 
> 다수의 스레드에서 동시에 글이 작성된다면?
> - `Collections.synchronizedList` vs `CopyOnWriteArrayList` 
> - 인스타그램을 모티브로한 현재 서비스에선 읽기가 훨씬 많지 않나?

> #### 글을 쓰는 대로 리스트에 추가되는 현재 상황에서 굳이 정렬이 필요한가?
> 
> concurrency를 고려해보면 동시에 글을 올렸지만 네트워크 이슈 등의 문제로 작성 시간이 늦은 글이 더 먼저 추가될 수도 있을 듯
> 