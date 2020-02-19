import config
import csv
import requests
import pymysql
import datetime
import random

headers = { 'Content-Type': 'application/json'}

# 닉네임
def pushNickName() :
    noun, adjective = [], []

    with open('./seed/noun.csv',  'rt', encoding='UTF8', newline='') as csvfile:
        spamreader = csv.reader(csvfile, delimiter=' ', quotechar='|')
        for row in spamreader:
            noun = ', '.join(row).split(',')

    with open('./seed/adjective.csv', 'rt', encoding='UTF8', newline='') as csvfile:
        spamreader = csv.reader(csvfile, delimiter=' ', quotechar='|')
        for row in spamreader:
            adjective = ', '.join(row).split(',')

    nickname_pull = []
    for n in noun :
        for a in adjective :
            # 닉네임 만들기
            nickname_pull.append(a+" "+n)

    random.shuffle(nickname_pull)
    print("닉네임을 삽입합니다.")
    for nickname in nickname_pull :
        # 닉네임 삽입
        curs = conn.cursor()
        sql = """INSERT INTO nickname_bible(nickname)
            VALUES (%s)"""
        curs.execute(sql, nickname)
        conn.commit()
    print("닉네임 삽입이 완료되었습니다.")


# 직무, 질문
def get_questions(employment_id) :
    url = "https://jasoseol.com/employment/employment_question.json"
    payload = "{\n\t\"employment_id\": "+str(employment_id)+"\n}"

    response = requests.request("POST", url, headers=headers, data = payload)
    data = response.json()

    return data["employment_question"] or []

# 공고 상세
def get_recruit_detail(recruit_id) :
    url = "https://jasoseol.com/employment/get.json"
    payload = "{\n\t\"employment_company_id\": "+str(recruit_id)+"\n}"

    response = requests.request("POST", url, headers=headers, data = payload)
    data = response.json()
    
    return { "content": data["content"],
                "employment_page_url" : data["employment_page_url"],
                "employments" : data["employments"],
                "recruit_type" : data["recruit_type"]
            }

# 채용공고 
def get_recruits() :
    url = "https://jasoseol.com/employment/calendar_list.json"
    payload = "{\n\t\"start_time\": \"2014-01-01T15:00:00.000Z\",\n\t\"end_time\": \"2020-03-01T15:00:00.000Z\"\n}"

    response = requests.request("POST", url, headers=headers, data = payload)
    recuit = response.json()

    print("채용공고 크롤링을 시작합니다.")
    global_id = 0
    for data in recuit["employment"] :
        recruit_id = data["id"]
        name = data["name"]
        logo_url = data["image_file_name"]

        # company id 중복 조회
        curs = conn.cursor()
        sql = """select id from company where name = %s"""
        curs.execute(sql,(name.replace(" ", "") ))
        rows = curs.fetchall()
        if len(rows) :
            if len(rows) > 1 :
                print("미친듯")

            for row in rows :
              company_id = int(rows[0]['id'])
              print(name, company_id)
        else :
            # 존재하지 않으면 삽입
            """ company table """
            curs = conn.cursor()
            sql = """INSERT INTO company(name, logo_url)
                VALUES (%s, %s)"""
            curs.execute(sql, (name, logo_url))
            conn.commit()

            global_id +=1
            company_id = global_id


        _start_time = data["start_time"].split('T')
        _date = list(map(int, _start_time[0].split("-")))
        _time = list(map(int, _start_time[1].split(".")[0].split(":")))
        start_time = datetime.datetime(_date[0], _date[1], _date[2], _time[0], _time[1], _time[2])

        _end_time = data["end_time"].split('T')
        _date = list(map(int, _end_time[0].split("-")))
        _time = list(map(int, _end_time[1].split(".")[0].split(":")))
        end_time = datetime.datetime(_date[0], _date[1], _date[2], _time[0], _time[1], _time[2])
        
        detail = get_recruit_detail(recruit_id)
        employment_page_url = detail["employment_page_url" ]
        content = detail["content"]
        recruit_type = detail["recruit_type"]
        view_count = detail["view_count"]

        """ company table """
        curs = conn.cursor()
        sql = """INSERT INTO recruit(id,company_id,start_time,end_time,content,view_count,employment_page_url,recruit_type)
            VALUES (%s, %s, %s, %s, %s, %s, %s)"""
        curs.execute(sql, (recruit_id, company_id, start_time, end_time, content, view_count, employment_page_url, recruit_type))
        conn.commit()

        for employment in detail["employments"] :
            position_id = employment["id"]
            field = employment["field"]
            division= employment["division"]

            """ position table """
            curs = conn.cursor()
            sql = """INSERT INTO position VALUES (%s, %s, %s, %s)"""
            curs.execute(sql,(position_id, recruit_id, field, division))
            conn.commit()

        
            questions = get_questions(position_id)
            for question in questions :
                question_id = question["id"]
                question_content = question["question"] or '자유양식'
                question_limit = question["total_count"] or 0

                """ question table """
                curs = conn.cursor()
                sql = """INSERT INTO question VALUES (%s, %s, %s, %s)"""
                curs.execute(sql, (question_id, position_id, question_content, question_limit))
                conn.commit()

    print("완료!")

conn = pymysql.connect(host=config.HOST, port=config.PORT, user=config.USER, passwd=config.PASSWORD,
                        db=config.DATABASE, charset='utf8')


# pushNickName() 
get_recruits()
conn.close()
