"""
태그를 정리한 txt파일을 이용하여, 파이어베이스의 stlye collection내의 옷 사진들의 태그를 업데이트합니다.
"""

import firebase_admin
from firebase_admin import firestore
from firebase_admin import credentials


cred = credentials.ApplicationDefault() # auth 파일을 엽니다.
firebase_admin.initialize_app(cred, {
  'projectId': 'fashionistagram-66015',
}) # 저희의 프로젝트 이름입니다.

db = firestore.client() # db client입니다.

image_feature = dict() # 태그를 저장할 dict 객체입니다.

with open('태그0010~1050.txt', 'r') as f:
    lines = f.readlines()[2:] # 한글 속성 줄 제거
    for line in lines:
        line = line[:-1] # \n 제거.
        line = line.split('\t')
        num = int(line[0])
        tag = ('').join(line[1:])
        image_feature[num] = tag

print(image_feature.keys()) # 태그 파일을 잘 읽어왔는지 확인합니다.

for image_num in image_feature.keys():
    if image_num < 1000:
        image_num = '00' + str(image_num)
    image = db.collection('style').document(str(image_num) + '.png')
    image.update({'feature' : image_feature[int(image_num)]}
    )
