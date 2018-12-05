import firebase_admin
from firebase_admin import firestore
from firebase_admin import credentials


cred = credentials.ApplicationDefault()
firebase_admin.initialize_app(cred, {
  'projectId': 'fashionistagram-66015',
})

db = firestore.client()

image_feature = dict()

with open('태그0010~1050.txt', 'r') as f:
    lines = f.readlines()[2:] # 한글 속성 줄 제거
    for line in lines:
        line = line[:-1] # \n 제거.
        line = line.split('\t')
        num = int(line[0])
        tag = ('').join(line[1:])
        image_feature[num] = tag

print(image_feature.keys())

for image_num in image_feature.keys():
    if image_num < 1000:
        image_num = '00' + str(image_num)
    image = db.collection('style').document(str(image_num) + '.png')
    image.update({'feature' : image_feature[int(image_num)]}
    )