# 🐮 LoopSNS (FrontEnd)

<br/>

## **Team & Project Details:**
- **Team Name:** HCI
- **Team Members:**
    - 🧑‍⚖️ 고건호 **(팀장)**
    - 🧑‍💻 김도환
    - 🧑‍💻 송주훈 *(Only BackEnd)*
    - 👩‍💻 김채리 *(Only BackEnd)*
    - 👩‍💻 한신영
    - 👩‍💻 오은결
- **Front Members:**
    - 🧑‍⚖️ 고건호 **(팀장)**
    - 🧑‍💻 김도환
    - 👩‍💻 한신영
    - 👩‍💻 오은결
- **Project Duration:** 2024.06.22 ~ 2024.08.12

<br/>

## **Overview:**
위치 기반 SNS.. 

<br/>

## **Key Features:**
- **카테고리 자동 분석:** 
- **KMeans 알고리즘을 이용한 클러스터링:** 

<br/>

## **How to Set Up:**
### 1. 필수 라이브러리 설치

```
pip install -r requirements.txt
```

### 2. 데이터베이스 생성
```
python src/data/make_dataset.py
python src/data/make_geo_data.py
```
위 코드를 실행하기 전에 `disease_status`, `geometry_data` 테이블이 생성되어 있어야 합니다.


### 3. .env 파일 생성

```
makefile create-env
```

### 4. .env 파일 정보 수정
생성된 env 파일의 형식은 다음과 같습니다.
```
MAFRA_KEY_API_KEY=default_api_key
KAKAO_API_KEY=default_api_key
VWORLD_API_KEY=default_api_key
OPENAI_API_KEY=default_api_key
SECRET_KEY=default_secret_key
SQLALCHEMY_DATABASE_URI=sqlite:///default.db
SQLALCHEMY_TRACK_MODIFICATIONS=False
JSON_AS_ASCII=False
SERVER_DOMAIN=localhost
SERVER_PORT=5000
CHATBOT_MODEL=your_model_name
CHATBOT_CHROMA_DB_PATH=yout_chroma_db_model
DB_USER=default_user
DB_HOST=default_host
DB_PASSWORD=default_password
DB_NAME=default_name
FIREBASE_ADMINSDK_PATH=default_path
```
안에 내용을 적절히 수정하여 사용하면 됩니다.

### 5. 크론탭 등록
```
sh setup_cron.sh
```

사용자의 대화 횟수를 초기화해주는 파이썬 코드를 크론탭에 등록합니다.

### 6. run.py 실행
```
python run.py
```

<br/>

## **Project Organization:**
```
├── app/
│   ├── routes/
│   │   ├── __init__.py
│   │   ├── chat.py
│   │   ├── get_disease_data.py
│   │   └── user.py
│   ├── utils/
│   │   ├── __init__.py
│   │   └── decorators.py
│   └── __init__.py
├── config/
│   ├── __init__.py
│   ├── api.py
│   ├── chatbot.py
│   ├── database.py
│   ├── default.py
│   ├── server.py
│   └── settings.py
├── data/
│   ├── emd.json
│   ├── hci-animal-chatbot-firebase-adminsdk.json
│   ├── PnuCode.csv
│   ├── sido.json
│   └── sig.json
├── src/
│   ├── data/
│   │   ├── api.py
│   │   ├── convert_code.py
│   │   ├── make_dataset.py
│   │   ├── make_geo_data.py
│   │   └── pnu_geolocation_lookup.py
│   ├── llm/
│   │   └── chatbot.py
│   └── reset_chat_counts.py
├── Makefile
├── README.md
├── requirements.txt
├── run.py
└── setup_cron.sh
```

