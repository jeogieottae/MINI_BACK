![bandicam 2024-07-23 01-24-12-768](https://github.com/user-attachments/assets/011031b6-acd2-4043-a063-b443bbc369f3)
# 프로젝트 소개 📖
저기어때 사이트는 다양한 숙박 검색 및 예약 서비스를 제공하는 종합 플랫폼입니다.    
사용자는 숙소와 객실을 손쉽게 예약할 수 있으며, 장바구니와 좋아요 기능을 통해 선호하는 숙소를 관리할 수 있습니다.  
카카오톡과 구글 계정을 통한 소셜 로그인 기능을 지원하여 간편하게 로그인 및 회원가입을 할 수 있습니다.    
또한, 숙소명과 지역별 및 체크인 날짜별로 세부적인 검색이 가능하며, 이용자 리뷰를 통해 신뢰할 수 있는 정보를 얻을 수 있습니다.

#### 개발 기간 2024.06.17 ~ 2024.07.07
#### <div>
  <a href="https://your-trip-pied.vercel.app/" target="_blank">
    <img src="https://img.shields.io/badge/저기어때 서비스  바로가기-3b82f6?style=for-the-badge&logoColor=white" alt="저기어때 바로가기"/>
  </a>
</div>

## 사용 기술 🔧

| **항목**                  | **내용**                                                                 |
|-----------------------|--------------------------------------------------------------------|
| **프레임워크 및 언어**    | Spring Boot, JDK 17, Gradle                                      |
| **DB/캐싱**              | MySQL, Redis(Lettuce,  Redisson)                                  |
| **검색 엔진**            | ElasticSearch                                                   |
| **배포 & 컨테이너**       | Amazon EC2, Github Actions, Docker(Docker Hub, Docker Compose) |
| **테스트 및 성능 모니터링** | Junit, K6                 

## ERD 구조 ⚙️
![그림1](https://github.com/leeshinbi/KDT_BE8_Mini-Project/assets/109641586/e78b15f7-36a2-4530-8f86-7649c0d51c7b)


## 서비스 아키텍처 ⚒️
![그림2](https://github.com/leeshinbi/KDT_BE8_Mini-Project/assets/109641586/021eca83-4f5c-4d21-8877-21fdaec490fe)

## 기능 구현 📌

### 🔸로그인/회원가입 
JWT, Spring Security, Cookie, Redis를 사용하여 해당 기능을 구현하였습니다.
RefreshToken의 특성상 반복적이고 빠른 읽기 요구, 만료 시간 관리, 로그아웃 시 AccessToken의 블랙리스트 관리 등을 종합적으로 고려할 때, 
`NoSQL`, `In-Memory`, `만료 시간 설정` 기능을 모두 갖춘 Redis를 사용하는 것이 최적이라고 판단하여 구현하였습니다.

### 🔸 sns 로그인 
### 🔸 예약
### 🔸 좋아요 
좋아요 기능은 잦은 조회가 발생하는 영역으로, 데이터가 (게시글 ID, 사용자 프로필 ID)와 같이 변할 확률이 낮고 하나의 데이터 쌍으로 구성되어 있어 삽입/삭제가 용이하기에 캐싱을 수행하기에 적합하다고 생각했습니다.    
이에 따라 **Redis**를 사용하여 캐싱을 수행하였고, 사용한 캐싱 기법으로는 **Read Through + Write Back** 패턴을,   
성능 최적화를 위해 **Spring Batch** 기법을 도입하여 비동기 처리하여 데이터 일괄 처리와 스케줄링을 효율적으로 관리하였습니다. 

![bandicam 2024-07-23 01-24-12-768](https://github.com/user-attachments/assets/455d5eb6-6bc5-4eb4-885f-61416437f36e)

### 🔸  검색 
### 🔸 배포 
배포는 GitHub Actions를 통해 자동화된 빌드 및 배포를 수행하고, Docker와 Docker Hub를 사용하여 컨테이너화된 애플리케이션 이미지를 관리하였습니다. 클라우드 인프라는 Amazon EC2를 사용하여 애플리케이션을 호스팅하였으며, HTTP를 HTTPS로 변환하기 위해 **Certbot**을 사용하여 SSL 인증서를 발급받아 보안을 강화하였습니다.

## 트러블 슈팅 ✨


## 구현 화면 일부 소개 🖥️

### 🔎회원가입/로그인 
![bandicam 2024-07-22 23-39-27-077](https://github.com/user-attachments/assets/6788d87f-eef9-4d4f-9e75-b2e0bc7b86bc)

### 🔎메인 페이지 
![bandicam 2024-07-22 23-39-39-317](https://github.com/user-attachments/assets/4c9e5e4c-36fd-4ee5-bd3f-2768f4a73833)

### 🔎숙소 상세
![bandicam 2024-07-22 23-40-31-470](https://github.com/user-attachments/assets/cfdc5ee3-a62b-48ed-b8e7-e9821bcb8956)

### 🔎 장바구니
![cart](https://github.com/user-attachments/assets/36f617e0-c9ee-4d19-9996-060e2727c7c2)

### 🔎 예약 목록 및 찜 목록 
![bandicam 2024-07-22 23-40-45-780](https://github.com/user-attachments/assets/24899560-f36d-4daa-838b-84cd734f777c)
![bandicam 2024-07-22 23-41-02-648](https://github.com/user-attachments/assets/534234a8-06b4-4ecd-9b2c-25737027ba6d)
![like](https://github.com/user-attachments/assets/63581986-1d40-4a42-b97c-ab369fe6cd8a) 


