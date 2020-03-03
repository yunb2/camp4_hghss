"""
월별 총 자기소개서 작성 수 / 공고 수 출력
"""
import requests
import datetime

headers = { 'Content-Type': 'application/json'}

# 공고 상세
def get_recruit_detail(recruit_id) :
    url = "https://jasoseol.com/employment/get.json"
    payload = "{\n\t\"employment_company_id\": "+str(recruit_id)+"\n}"

    response = requests.request("POST", url, headers=headers, data = payload)
    data = response.json()
    return { "employments" : data["employments"] }

# 채용공고 
def get_recruits(key,start,end) :
    url = "https://jasoseol.com/employment/calendar_list.json"
    payload = "{\n\t\"start_time\": \""+start+"T15:00:00.000Z\",\n\t\"end_time\": \""+end+"T15:00:00.000Z\"\n}"

    response = requests.request("POST", url, headers=headers, data = payload)
    recruit = response.json()
    for data in recruit["employment"] :
        recruit_id = data["id"]
        
        start_time = "-".join(data["start_time"].split('T')[0].split('-')[:2])
        if start_time == key :
            total_recruit[key] += 1
            print(data["name"], "의 이력서를 계산합니다.")

            detail = get_recruit_detail(recruit_id)
            for employment in detail["employments"] :
                resume_count = employment["resume_count"]
                total_resume[key] += resume_count

    print("완료!")


year = ["2019","2020"]
month = ["01","02","03","04","05","06","07","08","09","10","11","12"]

total_resume = {}
total_recruit = {}
for y in year :
    for i in range(len(month)) : 
        print(y+"-"+month[i],"시작")

        total_resume[y+"-"+month[i]] = 0
        total_recruit[y+"-"+month[i]] = 0

        get_recruits(y+"-"+month[i],y+"-"+month[i]+"-01", y+"-"+month[i]+"-31")

for y in year :
    for i in range(len(month)) : 
        key = y+"-"+month[i]
        print(key,end=' => ')
        print("자기소개서: ",total_resume[key],end=' | ')
        print("공고: ",total_recruit[key])

