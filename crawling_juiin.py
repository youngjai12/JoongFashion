import re
import urllib.request
from bs4 import BeautifulSoup
from google.cloud import storage

# Enable Storage
client = storage.Client(project='fashionistagram-66015')

# Reference an existing bucket.
bucket = client.get_bucket('fashionistagram-66015.appspot.com')

url = "https://www.juinn.co.kr/"
soup = BeautifulSoup(urllib.request.urlopen("https://juinn.co.kr/product/list.html?cate_no=25"), "html.parser")

i = 1000
for link in soup.findAll('img', attrs={'class':'thumb'}):
    img_src = link.attrs['src']
    img_url = url + img_src[14:]
    img_name = img_src[44:48] # 이미지 src에서 / 없애기
    img_type = img_src[-3:]
    savename = "./big/" + img_name
    if(img_type != 'gif'):
        print(i)
        urllib.request.urlretrieve(img_url, savename + '.png')
        zebraBlob = bucket.blob('styles/' + str(i) + '.png')
        zebraBlob.upload_from_filename(filename = savename + '.png')
        i = int(i)+1
